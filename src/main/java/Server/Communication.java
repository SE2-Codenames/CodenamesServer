package Server;

import com.google.gson.Gson;
import model.Card.Card;
import model.GameState;
import model.Player.TeamColor;
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

    public String getExposeData() {
        if (isExposeCommand()) {
            return input.substring("EXPOSE:".length()).trim();
        }
        return "";
    }

    // ==== Nachricht an Client senden ====

    public void sendGameState(GameState gameState, TeamColor currentTeam, List<Card> cards,
                              int[] score, String hint, int remainingGuesses) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("gameState", gameState);
        payload.put("teamRole", currentTeam);
        payload.put("card", cards);
        payload.put("score", score);
        payload.put("hint", hint);
        payload.put("remainingGuesses", remainingGuesses);

        String message = "GAME_STATE:" + gson.toJson(payload);
        conn.send(message);
        LOGGER.info("Gesendet an Client: " + message);
    }
}
