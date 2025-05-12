import model.Card.WordBank;
import model.GameState;

public class Gameprogress {

    Game game;
    Communication communication;

    private void startGame() throws GameException {
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


}
