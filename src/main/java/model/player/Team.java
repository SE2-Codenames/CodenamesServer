package model.player;


public class Team {
    private TeamColor color;
    private int cardsRemaining;
    private boolean isCurrentTurn;

    public Team(TeamColor color) {
       this.color = color;
        this.cardsRemaining = (color == TeamColor.RED) ? 9 : 8; //Red team always starts with 9 card, blue iwth 8
        this.isCurrentTurn = false;
    }

    public TeamColor getColor() {
        return color;
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

    public void startTurn() { isCurrentTurn = true; }

    public void endTurn() { isCurrentTurn = false; }

    public void setCardsRemaining(int count) {
        this.cardsRemaining = count;
    }

    public boolean hasWon() { return cardsRemaining == 0; }

    @Override
    public String toString() {
        return color + " Team Cards left: " + cardsRemaining;
    }
}
