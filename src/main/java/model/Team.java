package model;

public class Team {
    private TeamColor color;
    private int score;
    private int cardsRemaining;
    private boolean isCurrentTurn;

    public Team(TeamColor color) {
       this.color = color;
        this.score = 0;
        this.cardsRemaining = 0;
        this.isCurrentTurn = false;
    }

    public TeamColor getColor() {
        return color;
    }

    public int getScore() {
        return score;
    }

    public int getCardsRemaining() {
        return cardsRemaining;
    }

    public boolean isCurrentTurn() {
        return isCurrentTurn;
    }

    public void setCurrentTurn(boolean currentTurn) {
        isCurrentTurn = currentTurn;
    }

    public void incrementScore() {
        score++;
        cardsRemaining--;
    }

    public void setCardsRemaining(int count) {
        this.cardsRemaining = count;
    }

    @Override
    public String toString() {
        return color + " Team - Score: " + score + ", Cards left: " + cardsRemaining;
    }
}
