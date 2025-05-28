package Server;

import model.Card.*;
import model.GameState;
import model.Player.TeamColor;

import java.security.SecureRandom;
import java.util.*;

import static javax.swing.UIManager.getInt;

public class Game {
    private final List<Card> board;
    private GameState state;            //Role Turn
    private TeamColor currentTurn;      //Team Turn
    private int remainingGuesses;
    private int[] score;                //score[0] = RED; score[1] = BLUE
    // für die Cheatfunktion nötig
    private String currentClue;
    private TeamColor currentClueTeam;
    private int totalRedCards = 0;
    private int totalBlueCards = 0;


    public Game(WordBank wordBank) {
        currentTurn = startingTeamRandom();
        this.state = GameState.LOBBY;
        this.score = new int[]{0, 0};
        this.board = createBoard(wordBank.getRandomWords(25));
        checkScore();
    }

    //getter Methoden
    public List<Card> getBoard(){return board;}
    public GameState getGamestate(){return state;}
    public TeamColor getCurrentTeam(){return currentTurn;}
    public int[] getScore(){return score;}
    public String getHint(){return currentClue;}
    public int getRemainingGuesses(){return remainingGuesses;}
    //setter Methoden
    public void setGamestate(GameState state){this.state = state;}

    // creat Cardboard
    private List<Card> createBoard(List<String> randomWords) {
        List<Card> board = new ArrayList<>();

        // 1. Assign card types (9 startingTeam, 8 !startingTeam, 7 neutral, 1 assassin)

        for (int i = 0; i < 25; i++) {
            CardRole cardType;
            if (i < 9){                                     // First 9 = Starting Team
                if(currentTurn == TeamColor.RED){
                    cardType = CardRole.RED;
                }else{
                    cardType = CardRole.BLUE;
                }
            }
            else if (i < 17){                               // Next 8 = !Starting Team
                if(currentTurn == TeamColor.RED){
                    cardType = CardRole.BLUE;
                }else{
                    cardType = CardRole.RED;
                }
            }
            else if (i < 24) cardType = CardRole.NEUTRAL;   // Next 7 = Neutral
            else cardType = CardRole.ASSASSIN;              // Last 1 = Assassin

            if(cardType == CardRole.RED){
                totalRedCards++;
            } else if(cardType == CardRole.BLUE){
                totalBlueCards++;
            }
            board.add(new Card(randomWords.get(i), cardType));
        }
        // 2. Shuffle to randomize positions
        Collections.shuffle(board);
        return board;
    }

    //randomized the starting Team
    private TeamColor startingTeamRandom() {
        SecureRandom rd = new SecureRandom();
        if(rd.nextBoolean())
            return TeamColor.RED;
        else
            return TeamColor.BLUE;
    }

    public void getClue(String[] clue){
        currentClue = clue[0];
        remainingGuesses = Integer.parseInt(clue[1]);
        endTurn();
    }

    //muss später überarbeitet werden, um die Cheatfunktion zu implementieren
    private String validateClue() throws GameException {
        String clueWord = currentClue.trim();

        if (clueWord.isEmpty()) {
            throw new GameException("Clue cannot be empty");
        }
        if (clueWord.matches(".*\\d.*")) {
            throw new GameException("Clue cannot contain numbers");
        }

        String finalClueWord = clueWord;
        if (board.stream().anyMatch(card ->
                card.getWord().equalsIgnoreCase(finalClueWord))) {
            throw new GameException("Clue cannot be a word on the board");
        }

        return clueWord;
    }

    // Input from the Operative Player for there guess
    public void guessCard(int guess) throws GameException {
        Card card = board.get(guess);

        if (card.isRevealed()) {
            throw new GameException("Card already revealed");
        }
        card.reveal();
        remainingGuesses--;
        handleCardReveal(card);
    }

    private void handleCardReveal(Card card){
        switch (card.getCardRole()) {
            case ASSASSIN:
                handleAssassinReveal();
                break;
            case NEUTRAL:
                handleNeutralReveal(card);
                break;
            default:
                handleTeamCardReveal(card);
        }
    }

    private void handleAssassinReveal() {
        state = GameState.GAME_OVER;
        // Current team loses when revealing assassin
        if (currentTurn == TeamColor.RED) {
            score[0] = -1;
        } else {
            score[1] = -1;
        }
        checkScore();
    }

    private void handleNeutralReveal(Card card) {
        checkScore();
        endTurn();
    }

    private void handleTeamCardReveal(Card card) {
        int team;
        CardRole colour;
        if (currentTurn == TeamColor.RED) {
            team = 0;
            colour = CardRole.RED;
        }
        else{
            team = 1;
            colour = CardRole.BLUE;
        }
        checkScore();

        if(card.getCardRole() == colour){
            checkTurnState();
        }
        else{
            endTurn();
        }
    }

    // calculate the score and check Win state
    private void checkScore() {
        int revealedRed = 0;
        int revealedBlue = 0;

        if(score[0] == -1 || score[1] == -1){
            notifyGameOver();
        }
        else {

            score[0] = 0;
            score[1] = 0;

            for (int i = 0; i < 25; i++) {
                if(!board.get(i).isRevealed()){
                    if(board.get(i).getCardRole() == CardRole.RED){
                        revealedRed++;
                        score[0]++;
                    }
                    if(board.get(i).getCardRole() == CardRole.BLUE){
                        revealedBlue++;
                        score[1]++;
                    }
                }
            }

            if(revealedRed == 0 || revealedBlue == 0){
                notifyWin();
            }
        }
    }

    private void checkTurnState() {
        if(remainingGuesses < 0){
            endTurn();
        }
    }

    private void endTurn() {
        if (state == GameState.OPERATIVE_TURN) {
            // Operative turn ends -> switch to other team's spymaster turn
            currentTurn = (currentTurn == TeamColor.RED) ? TeamColor.BLUE : TeamColor.RED;
            state = GameState.SPYMASTER_TURN;
        }
        else if (state == GameState.SPYMASTER_TURN) {
            // Spymaster gave clue -> switch to same team's operative turn
            state = GameState.OPERATIVE_TURN;
        }
    }

    private void notifyWin() {
        state = GameState.GAME_OVER;
    }

    private void notifyGameOver() {
        state = GameState.GAME_OVER;
        if(currentTurn == TeamColor.RED){
            currentTurn = TeamColor.BLUE;
        }
        else{
            currentTurn = TeamColor.RED;
        }
    }
}


