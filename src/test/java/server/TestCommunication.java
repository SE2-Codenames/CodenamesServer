package server;

import model.card.Card;
import model.GameState;
import model.player.TeamColor;
import org.java_websocket.WebSocket;

import java.util.List;

public class TestCommunication extends Communication {

    private String testInput;
    private String[] testHint;
    private int selectedCard = -1;
    private int markedCard = -1;

    public TestCommunication(WebSocket conn) {
        super(conn);
    }

    public void setTestHint(String word, String number) {
        this.testHint = new String[]{word, number};
    }

    public void setSelectedCard(int index) {
        this.selectedCard = index;
    }

    public void setMarkedCard(int index) {
        this.markedCard = index;
    }

    public void setTestInput(String input) {
        this.testInput = input;
        super.setInput(input);
    }

    @Override
    public String[] getHint() {
        return testHint != null ? testHint : super.getHint();
    }

    @Override
    public int getSelectedCard() {
        return selectedCard >= 0 ? selectedCard : super.getSelectedCard();
    }

    @Override
    public int getMarkedCard() {
        return markedCard >= 0 ? markedCard : super.getMarkedCard();
    }

    // Optional: unterdrücke Senden, um keine echten WebSocket-Nachrichten zu schicken
    @Override
    public void sendGameState(GameState gameState, TeamColor currentTeam, List<Card> cards, int[] score, String hint) {
        // no-op für Tests
    }

    @Override
    public void sendCard(Card card) {
        // no-op für Tests
    }

    @Override
    public void sendHint(String hint, int number) {
        // no-op für Tests
    }

    @Override
    public void sendMarked(boolean[] markedCards) {
        // no-op für Tests
    }

    @Override
    public void sendExpose(String message) {
        // no-op für Tests
    }

    @Override
    public void sendWin(TeamColor team, int[] score, boolean assasin) {
        // no-op für Tests
    }
}
