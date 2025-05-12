package Server;

import model.Card.Card;
import model.Card.WordBank;
import model.GameState;
import model.Player.TeamColor;
import java.io.PrintWriter;
import java.util.List;

import static java.lang.System.out;


public class Gameprogress {

    Game game;
    Communication communication;
    private String new_input = "";


    private List<UserManager> clients;

    public Gameprogress(List<UserManager> clients) {
        this.clients = clients;
    }

    private void startGame() throws GameException {
        System.out.println("START_GAME received.");
        WordBank wordBank = new WordBank();
        game = new Game(wordBank);
        checkState();
    }

    private void checkState() throws GameException {
        GameState state = game.getGamestate();
        if(state == GameState.LOBBY){
            state = GameState.GAME_OVER;
            giveInformation();
        }
        else if(state == GameState.SPYMASTER_TURN){
            spymasterTurn();
        }else if(state == GameState.OPERATIVE_TURN){
            operativeTurn();
        }else if(state == GameState.GAME_OVER){
            gameoverTurn();
        }
    }

    private void giveInformation() throws GameException {

        for (UserManager user : clients) {
            System.out.println("Sending GAME_STATE to client: " + user.getPlayerInfo());
            Communication comm = new Communication(user.getWriter());
            comm.giveGame(game.getGamestate(), game.getCurrentTeam(), game.getBoard(), game.getScore());
        }
        communication.giveGame(game.getGamestate(), game.getCurrentTeam(), game.getBoard(), game.getScore());
        checkState();
    }

    private void spymasterTurn(){
        String[] clue = communication.getHint();
        game.getClue(clue);
    }

    private void operativeTurn() throws GameException {
        int guess = communication.selectCard();
        game.guessCard(guess);
    }

    private void gameoverTurn(){
        communication.giveGame(game.getGamestate(), game.getCurrentTeam(), game.getBoard(), game.getScore());
    }

    public void processMessage(String input, PrintWriter out) {
        communication = new Communication(out);
        communication.setInput(input);

        try {
            if (communication.gameStart(input)) {
                startGame();
            } else {
                checkState();
            }
        } catch (GameException e) {
            out.println("MESSAGE:Server.Game Error: " + e.getMessage());
        } catch (Exception e) {
            out.println("MESSAGE:Unexpected Error: " + e.getMessage());
        }
    }



}
