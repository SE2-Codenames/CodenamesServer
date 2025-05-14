package Server;

import com.google.gson.Gson;
import model.Card.WordBank;
import model.GameState;
import model.Player.Player;
import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gameprogress {

    private static final Logger LOGGER = Logger.getLogger(Gameprogress.class.getName());

    private final Map<WebSocket, Player> sessions; // Map: WebSocket -> Player
    private final Gson gson = new Gson();
    public Game game;
    protected Communication communication;

    public Gameprogress(Map<WebSocket, Player> sessions) {
        this.sessions = sessions;
    }

    public void processMessage(WebSocket conn, String input) {
        communication = new Communication(conn);
        communication.setInput(input);

        try {
            if (communication.isGameStartRequested()) {
                startGame();
                for (WebSocket socket : sessions.keySet()) {
                    socket.send("SHOW_GAMEBOARD");
                }
            } else {
                checkState(conn);
            }
        } catch (GameException e) {
            conn.send("MESSAGE:GameException: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler bei Nachrichtenverarbeitung: ", e);
            conn.send("MESSAGE:Unexpected error: " + e.getMessage());
        }
    }

    private void startGame() throws GameException {
        LOGGER.info("SPIELSTART angefordert");
        WordBank wordBank = new WordBank();
        game = new Game(wordBank);
        game.setGamestate(GameState.SPYMASTER_TURN);
        broadcastGameState();
    }

    private void checkState(WebSocket conn) throws GameException {
        if (game == null) {
            conn.send("MESSAGE:Spiel wurde noch nicht gestartet.");
            return;
        }

        switch (game.getGamestate()) {
            case LOBBY -> gameoverTurn();
            case SPYMASTER_TURN -> spymasterTurn(conn);
            case OPERATIVE_TURN -> operativeTurn(conn);
            case GAME_OVER -> gameoverTurn();
        }
    }

    private void spymasterTurn(WebSocket conn) {
        String[] clue = communication.getHint();
        game.getClue(clue);
        broadcastGameState();
    }

    private void operativeTurn(WebSocket conn) throws GameException {
        int guess = communication.getSelectedCard();
        game.guessCard(guess);
        broadcastGameState();
    }

    private void gameoverTurn() {
        broadcastGameState();
    }

    protected void broadcastGameState() {
        if (game == null) return;
        LOGGER.info("Spielstatus wird an alle Clients gesendet...");
        for (WebSocket session : sessions.keySet()) {
            Communication comm = new Communication(session);
            comm.sendGameState(
                    game.getGamestate(),
                    game.getCurrentTeam(),
                    game.getBoard(),
                    game.getScore(),
                    game.getHint(),
                    game.getRemainingGuesses()
            );
        }
    }
}
