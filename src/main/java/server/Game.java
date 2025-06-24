package server;

import model.card.*;
import model.GameState;
import model.player.TeamColor;

import java.security.SecureRandom;
import java.util.*;


public class Game {
    private final List<Card> board;
    private GameState state;            //Role Turn
    private TeamColor currentTurn;      //Team Turn
    private int remainingGuesses;
    private int[] score;                //score[0] = RED; score[1] = BLUE
    private boolean[] markedCards = new boolean[25];
    // für die Cheatfunktion nötig
    private String currentClue;
    private int revealedRed;
    private int revealedBlue;


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
    public boolean[] getMarkedCards() {
        return markedCards;
    }
    //setter Methoden
    public void setGamestate(GameState state){this.state = state;}
    public void setScore(int[] score){this.score = score;}


    // create Cardboard
    private List<Card> createBoard(List<String> randomWords) {
        List<Card> boardCards = new ArrayList<>();

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

            boardCards.add(new Card(randomWords.get(i), cardType));
        }
        // 2. Shuffle to randomize positions
        Collections.shuffle(boardCards);
        return boardCards;
    }

    //randomized the starting Team
    private TeamColor startingTeamRandom() {
        SecureRandom rd = new SecureRandom();
        //if(rd.nextBoolean())
            return TeamColor.RED;
        //else
        //    return TeamColor.BLUE;
    }

    public void getClue(String[] clue){
        currentClue = clue[0];
        remainingGuesses = Integer.parseInt(clue[1]);
        endTurn();
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
                handleNeutralReveal();
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

    private void handleNeutralReveal() {
        checkScore();
        endTurn();
    }

    private void handleTeamCardReveal(Card card) {
        CardRole colour;
        if (currentTurn == TeamColor.RED) {
            colour = CardRole.RED;
        }
        else{
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

    protected void checkScore() {
        if (isGameOver()) {
            notifyGameOver();
            return;
        }
        resetScore();
        countUnrevealedCards();

        if (revealedRed == 0 || revealedBlue == 0) {
            notifyWin();
        }
    }

    private boolean isGameOver() {
        return score[0] == -1 || score[1] == -1;
    }

    private void resetScore() {
        score[0] = 0;
        score[1] = 0;
        revealedRed = 0;
        revealedBlue = 0;
    }

    private void countUnrevealedCards() {
        for (int i = 0; i < 25; i++) {
            if (!board.get(i).isRevealed()) {
                CardRole role = board.get(i).getCardRole();
                updateScores(role);
            }
        }
    }

    private void updateScores(CardRole role) {
        if(role == CardRole.RED){
            revealedRed++;
            score[0]++;
        }else if(role == CardRole.BLUE){
            revealedBlue++;
            score[1]++;
        }
    }

    private void checkTurnState() {
        if(remainingGuesses < 0){
            endTurn();
        }
    }

    protected void endTurn() {
        if (state == GameState.OPERATIVE_TURN) {
            // Operative turn ends -> switch to other team's spymaster turn
            currentTurn = (currentTurn == TeamColor.RED) ? TeamColor.BLUE : TeamColor.RED;
            state = GameState.SPYMASTER_TURN;
            clearMarks();
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

    public void toggleMark(int index) {
        if (index >= 0 && index < markedCards.length) {
            markedCards[index] = !markedCards[index];
        }
    }

    public void clearMarks() {
        Arrays.fill(markedCards, false);
    }

    public boolean checkExpose() {
        String hint = currentClue.trim().toLowerCase();
        for (Card card : board) {
            String cardWord = card.getWord().toLowerCase();
            if (cardWord.equals(hint) || cardWord.contains(hint) || hint.contains(cardWord)) {
                return true;
            }
        }
        return false;
    }

    public boolean addTeamCard (TeamColor targetTeam) {
        List<Integer> neutralCards = new ArrayList<>();
        for (int i = 0; i < board.size(); i++) {
            Card card = board.get(i);
            if (card.getCardRole() == CardRole.NEUTRAL && !card.isRevealed()) {
                neutralCards.add(i);
            }
        }

        if (neutralCards.isEmpty()) {
            return false;
        }

        SecureRandom random = new SecureRandom();
        int randomIndex = neutralCards.get(random.nextInt(neutralCards.size()));
        Card selectedCard = board.get(randomIndex);

        if (targetTeam == TeamColor.RED) {
            selectedCard.setCardRole(CardRole.RED);
        } else {
            selectedCard.setCardRole(CardRole.BLUE);
        }

        return true;
    }

    public boolean checkAssassin(){
        for (Card card : board) {
            if (card.getCardRole() == CardRole.ASSASSIN) {
                if (card.isRevealed()) {
                    return true;
                }
            }
        }
        return false;
    }
}


