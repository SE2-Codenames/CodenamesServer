package server;

import com.google.gson.Gson;
import model.card.Card;
import model.card.WordBank;
import model.GameState;
import model.player.Player;
import model.player.TeamColor;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gameprogress {

    private static final Logger LOGGER = Logger.getLogger(Gameprogress.class.getName());

    public final Map<WebSocket, Player> sessions;
    private Game game;
    private Communication communication;

    //Getter-Setters
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setCommunication(Communication communication) {
        this.communication = communication;
    }

    public Gameprogress(Map<WebSocket, Player> sessions) {
        this.sessions = sessions;
    }

    public void processMessage(WebSocket conn, String input) {
        LOGGER.info(String.format("[processMessage] Eingehende Nachricht: %s", input));
        communication = new Communication(conn);
        communication.setInput(input);

        try {
            if (communication.isGameStartRequested()) {
                boolean redSpy = false;
                boolean blueSpy = false;
                boolean redOperative = false;
                boolean blueOperative = false;
                for (Player player : sessions.values()) {
                    if (player.getTeamColor() == TeamColor.BLUE && player.getSpymaster()){
                        blueSpy = true;
                    }
                    if (player.getTeamColor() == TeamColor.BLUE && !player.getSpymaster()){
                        blueSpy = true;
                    }
                    if (player.getTeamColor() == TeamColor.RED && player.getSpymaster()){
                        redSpy = true;
                    }
                    if (player.getTeamColor() == TeamColor.RED && !player.getSpymaster()){
                        redOperative = true;
                    }
                }

                if(redSpy && blueSpy && redOperative && blueOperative){
                    LOGGER.info("[processMessage] SPIELSTART angefordert durch Client " + conn.getRemoteSocketAddress());
                    startGame(conn);
                    for (WebSocket socket : sessions.keySet()) {
                        socket.send("SHOW_GAMEBOARD");
                    }
                }
                return;
            }
            if (communication.isExposeCommand()) {
                LOGGER.info("ExposeFunktion Start");
                handleExpose(conn);
                return;
            }
            checkState(conn);

        } catch (GameException e) {
            LOGGER.warning("[processMessage] GameException: " + e.getMessage());
            conn.send("MESSAGE:GameException: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[processMessage] Unerwarteter Fehler bei Nachrichtenverarbeitung", e);
            conn.send("MESSAGE:Unexpected error: " + e.getMessage());
        }
    }

    public void startGame(WebSocket initiator) {
        LOGGER.info("SPIELSTART angefordert");

        if (game != null && game.getGamestate() != GameState.LOBBY) {
            LOGGER.warning("Spiel wurde bereits gestartet oder läuft noch.");
            initiator.send("MESSAGE:Das Spiel läuft bereits.");
            return;
        }

        // Neues Spiel erzeugen mit WordBank
        this.game = new Game(new WordBank());
        this.game.setGamestate(GameState.SPYMASTER_TURN);

        LOGGER.info("Neues Spiel gestartet. Startteam: " + game.getCurrentTeam());
        broadcastGameState();
    }


    protected void checkState(WebSocket conn) throws GameException {
        if (game == null) {
            conn.send("MESSAGE:Spiel wurde noch nicht gestartet.");
            return;
        }

        switch (game.getGamestate()) {
            case LOBBY ->
                conn.send("MESSAGE:Warte auf Spielstart.");

            case SPYMASTER_TURN -> {
                if (communication.isHint()) {
                    spymasterTurn();
                } else {
                    conn.send("MESSAGE:Warte auf Hinweis des Spymasters.");
                }
            }
            case OPERATIVE_TURN -> {
                if (communication.isSkippedTurn()) {
                    skippTurn();
                }
                else if (communication.isCardSelection()) {
                    operativeTurn();
                }
                else if (communication.isCardMarked()) {
                    cardMarked();
                }
                else if (communication.clearMarksRequested()){
                    clearMarked();
                }
                else {
                    conn.send("MESSAGE:Operatives sind am Zug.");
                }
            }
            case GAME_OVER ->
                gameoverTurn();

        }
    }

    private void spymasterTurn() {
        String[] clue = communication.getHint();
        game.getClue(clue);

        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendHint(clue[0], Integer.parseInt(clue[1]));
        }

        broadcastMarkedCards();
        broadcastGameState();
    }

    private void cardMarked() {
        int marked = communication.getMarkedCard();
        game.toggleMark(marked);
        broadcastMarkedCards();
    }

    private void clearMarked() {
        game.clearMarks();
        broadcastMarkedCards();
    }

    private void operativeTurn() throws GameException {
        int guess = communication.getSelectedCard();
        game.guessCard(guess);

        Card selectedCard = game.getBoard().get(guess);
        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendCard(selectedCard);
        }

        broadcastGameState();
        broadcastMarkedCards();

        if(game.getGamestate()==GameState.GAME_OVER){
            gameoverTurn();
        }
    }

    private void skippTurn() {
        game.endTurn();
        game.clearMarks();

        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendMessage("Turn skipped");
        }

        broadcastMarkedCards();
        checkGameOver();
    }

    private void handleExpose(WebSocket conn) {
        String message;
        TeamColor targetTeam = game.checkExpose() ? game.getCurrentTeam() : (game.getCurrentTeam() == TeamColor.RED ? TeamColor.BLUE : TeamColor.RED);
        LOGGER.info(String.format("Target team: %s", targetTeam));
        boolean cardAdded = game.addTeamCard(targetTeam);
        if (!cardAdded) {
            LOGGER.info("No neutral cards left.");

            int[] score = game.getScore();
            if (targetTeam == TeamColor.RED) {
                score[0] = -1;
            } else {
                score[1] = -1;
            }
            game.setScore(score);

            message = "No cards left";

        } else {
            if (targetTeam == game.getCurrentTeam()) {
                game.clearMarks();
                game.endTurn();
                message = "Opponent used a forbidden word";
            }else{
                message = "No forbidden word detected";
            }
        }
        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendExpose(message);
        }
        game.checkScore();
        checkGameOver();
    }

    public void checkGameOver(){
        if (game.getGamestate() == GameState.GAME_OVER) {
            gameoverTurn();
        }else{
            broadcastGameState();
        }
    }

    private void gameoverTurn() {
        LOGGER.info("SPIELGAMEOVER");
        broadcastGameState();

        new Timer().schedule(new TimerTask() {
           @Override
           public void run() {
               gameReset();
           }
        }, 7000); //nach 7 Sekunden - Reset vom Spiel
    }

    public void gameReset() {
        LOGGER.info("SPIELGAMERESET");

        for(WebSocket socket : sessions.keySet()) {
            Communication comm = new Communication(socket);
            comm.sendWin(
                    game.getCurrentTeam(),
                    game.getScore(),
                    game.checkAssassin()
            );
        }

        for (WebSocket socket : new HashMap<>(sessions).keySet()) {
            try {
                socket.close(1000, "Game has been reset by the server");
            } catch (Exception e) {
                LOGGER.warning("Fehler beim Trennen der Verbindung: " + e.getMessage());
            }
        }

        sessions.clear();

        WordBank wordBank = new WordBank();
        game = new Game(wordBank);
        game.setGamestate(GameState.LOBBY);
    }

    public void broadcastGameState() {
        if (game == null) return;
        LOGGER.info("Spielstatus wird an alle Clients gesendet...");
        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendGameState(
                    game.getGamestate(),
                    game.getCurrentTeam(),
                    game.getBoard(),
                    game.getScore(),
                    game.getHint()
            );
        }
    }

    public void broadcastMarkedCards() {
        if (game == null) return;
        LOGGER.info("Markierte Karten werden an alle Clients gesendet...");
        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendMarked(game.getMarkedCards());
        }
    }
}
