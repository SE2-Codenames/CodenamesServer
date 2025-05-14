package Server;

import model.Card.WordBank;
import model.GameState;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gameprogress {

    private static final Logger LOGGER = Logger.getLogger(Gameprogress.class.getName());
    private List<UserManager> clients;
    private Communication communication;  // Referenz zur Communication-Klasse
    private Game game;

    public Gameprogress(List<UserManager> clients) {
        this.clients = clients;
    }

    private void startGame(PrintWriter out) throws GameException {
        communication = new Communication(out);
        LOGGER.log(Level.INFO, "START_GAME received.");
        WordBank wordBank = new WordBank();
        game = new Game(wordBank);
        checkState(out);
    }

    private void checkState(PrintWriter out) throws GameException {
        GameState state = game.getGamestate();
        if (state == GameState.LOBBY) {
            gameoverTurn(out);
        } else if (state == GameState.SPYMASTER_TURN) {
            spymasterTurn(out);
        } else if (state == GameState.OPERATIVE_TURN) {
            operativeTurn(out);
        } else if (state == GameState.GAME_OVER) {
            gameoverTurn(out);
        }
    }

    private void giveInformation(PrintWriter out) throws GameException {
        for (UserManager user : clients) {
            LOGGER.log(Level.INFO, "Sending GAME_STATE to client: " + user.getPlayerInfo());
            Communication comm = new Communication(user.getWriter());
            comm.giveGame(game.getGamestate(), game.getCurrentTeam(), game.getBoard(), game.getScore(), game.getHint(), game.getRemainingGuesses());
        }
        communication.giveGame(game.getGamestate(), game.getCurrentTeam(), game.getBoard(), game.getScore(), game.getHint(), game.getRemainingGuesses());
        checkState(out);
    }

    private void spymasterTurn(PrintWriter out) {
        String[] clue = communication.getHint();
        game.getClue(clue);
    }

    private void operativeTurn(PrintWriter out) throws GameException {
        int guess = communication.selectCard();
        game.guessCard(guess);
    }

    private void gameoverTurn(PrintWriter out) {
        communication.giveGame(game.getGamestate(), game.getCurrentTeam(), game.getBoard(), game.getScore(), game.getHint(), game.getRemainingGuesses());
    }

    public void processMessage(String input, PrintWriter out) {
        communication = new Communication(out);
        communication.setInput(input);
        try {
            if (communication.gameStart()) {
                startGame(out);
            } else {
                checkState(out);
            }
        } catch (GameException e) {
            out.println("MESSAGE:Server.Game Error: " + e.getMessage());
        } catch (Exception e) {
            out.println("MESSAGE:Unexpected Error: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error processing message: {0}", e.getMessage());
        }
    }
}
