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

    // ==== Empfangene Nachrichten verarbeiten ====

    public boolean isGameStartRequested() {
        return input != null && input.equalsIgnoreCase("START_GAME");
    }

    public String[] getHint() {
        if (input.startsWith("HINT:")) {
            String[] parts = input.split(":");
            if (parts.length == 3) {
                return new String[]{parts[1], parts[2]};
            }
        }
        return new String[]{"", "0"};
    }

    public int getSelectedCard() {
        if (input.startsWith("SELECT:")) {
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
        if (input.startsWith("MARK:")) {
            String posStr = input.substring("MARK:".length());
            try {
                return Integer.parseInt(posStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Ungültige MARK-Kartenposition empfangen: " + posStr);
            }
        }
        return -1;
    }

    // ==== Nachricht an Client senden ====

    public void sendGameState(GameState gameState, TeamColor currentTeam, List<Card> cards,
                              int[] score, String hint, int remainingGuesses, boolean[] markedCards) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("gameState", gameState);
        payload.put("teamRole", currentTeam);
        payload.put("card", cards);
        payload.put("score", score);
        payload.put("hint", hint);
        payload.put("remainingGuesses", remainingGuesses);
        payload.put("markedCards", markedCards);

        String message = "GAME_STATE:" + gson.toJson(payload);
        conn.send(message);
        LOGGER.info("Gesendet an Client: " + message);
    }
}
