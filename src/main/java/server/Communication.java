package server;

import com.google.gson.Gson;
import model.card.Card;
import model.GameState;
import model.player.TeamColor;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Communication {

    private static final Logger LOGGER = Logger.getLogger(Communication.class.getName());

    private final WebSocket conn;
    private final Gson gson = new Gson();
    private String input;
    private static final String MARK = "MARK:";
    private static final String GESENDET_AN_CLIENT = "Gesendet an Client:";
    private static final String MESSAGE = "message";

    public Communication(WebSocket conn) {
        this.conn = conn;
    }

    public void setInput(String input) {
        this.input = input;
    }

    // ==== Eingehende Nachrichten prüfen ====

    public boolean isGameStartRequested() {
        return input != null && input.equalsIgnoreCase("START_GAME");
    }

    public boolean isHint() {
        return input != null && input.startsWith("HINT:");
    }

    public boolean isCardSelection() {
        return input != null && input.startsWith("SELECT:");
    }

    public boolean isCardMarked() {
        return input != null && input.startsWith(MARK);
    }

    public boolean isSkippedTurn(){return input != null && input.equalsIgnoreCase("SKIP_TURN");}

    public boolean isExposeCommand() {
        return input != null && input.startsWith("EXPOSE:");
    }

    // ==== Eingehende Nachricht auslesen ====

    public String[] getHint() {
        if (isHint()) {
            String[] parts = input.split(":");
            if (parts.length == 3) {
                return new String[]{parts[1], parts[2]};
            } else {
                LOGGER.warning("Ungültiges HINT-Format: " + input);
            }
        }
        return new String[]{"", "0"};
    }

    public int getSelectedCard() {
        if (isCardSelection()) {
            String posStr = input.substring("SELECT:".length());
            try {
                return Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Ungültige Kartenposition empfangen: " + posStr);
            }
        }
        return -1;
    }

    public int getMarkedCard() {
        if (input.startsWith(MARK)) {
            String posStr = input.substring(MARK.length());
            try {
                return Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Ungültige MARK-Kartenposition empfangen: " + posStr);
            }
        }
        return -1;
    }

    public String getExposeData () {
        if (isExposeCommand()) {
            return input.substring("EXPOSE:".length()).trim();
        }
        return "";
    }

    public boolean clearMarksRequested() {
        return input.contains("\"clearMarks\":true");
    }

    // ==== Nachricht an Client senden ====

    public void sendGameState(GameState gameState, TeamColor currentTeam, List<Card> cards, int[] score, String hint) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameState", gameState);
        payload.put("teamRole", currentTeam);
        payload.put("card", cards);
        payload.put("score", score);
        payload.put("hint", hint);

        String message = "GAME_STATE:" + gson.toJson(payload);
        conn.send(message);
        LOGGER.info(GESENDET_AN_CLIENT + message);
    }

    public void sendMarked(boolean[] markedCards) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("markedCards", markedCards);
        String message = MARK + gson.toJson(payload);
        conn.send(message);
        LOGGER.info(GESENDET_AN_CLIENT + message);
    }

    public void sendWin(TeamColor team, int[] score, boolean assassinTriggered) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("team", team);
        payload.put("assassinTriggered", assassinTriggered);
        payload.put("score", score);
        String message = "GAME_OVER" + gson.toJson(payload);
        conn.send(message);
    }

    //Chatnachrichten
    public void sendHint(String hint, int number) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "hint");
        payload.put("hint", hint);
        payload.put("number", number);
        sendChat(payload);
    }

    public void sendCard(Card card) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "card");
        payload.put("card", card);
        sendChat(payload);
    }

    public void sendExpose(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "expose");
        payload.put(MESSAGE, message);
        sendChat(payload);
    }

    public void sendMessage(String message){
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", MESSAGE);
        payload.put(MESSAGE, message);
        sendChat(payload);
    }

    private void sendChat(Map<String, Object> payload) {
        String json = gson.toJson(payload);
        String fullMessage = "CHAT:" + json;
        conn.send(fullMessage);
        LOGGER.info(GESENDET_AN_CLIENT + fullMessage);
    }
}