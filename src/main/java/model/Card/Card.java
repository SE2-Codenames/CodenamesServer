package model.Card;

public class Card {
    private String word;
    private CardRole cardRole;
    private boolean revealed;

    public Card(String word, CardRole cardRole) {
        this.word = word;
        this.revealed = false;
        this.cardRole = cardRole;
    }
    public String getWord() {
        return word;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public CardRole getCardRole() {
        return cardRole;
    }

    public void reveal() {
        this.revealed = true;
    }

    @Override
    public String toString() {
        return word + " [" + cardRole + "]" + (revealed ? " (revealed)" : "");
    }

    public void setCardRole (CardRole cardRole) {
        this.cardRole = cardRole;
    }
}
