package Server;

import model.Card.Card;
import model.GameState;
import model.Player.TeamColor;
import java.util.List;

public class ServerPayload {
    public final GameState gameState;
    public final TeamColor teamRole;
    public final List<Card> cards;
    public final int[] score;
    public final String hint;
    public final int remainingGuesses;
    public final boolean[] markedCards;

    public ServerPayload(GameState gameState, TeamColor teamRole,
                         List<Card> cards, int[] score,
                         String hint, int remainingGuesses, boolean[] markedCards) {
        this.gameState = gameState;
        this.teamRole = teamRole;
        this.cards = cards;
        this.score = score;
        this.hint = hint;
        this.remainingGuesses = remainingGuesses;
        this.markedCards = markedCards;
    }
}