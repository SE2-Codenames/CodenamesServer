package Server;

import model.Player.Player;
import model.Player.TeamColor;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerImpl extends WebSocketServer {
    private static final Logger LOGGER = Logger.getLogger(ServerImpl.class.getName());
    private final Map<WebSocket, Player> connections = Collections.synchronizedMap(new HashMap<>());
    private final Gameprogress gameprogress;
    private static final String SPYMASTER_TOGGLE = "SPYMASTER_TOGGLE";

    public ServerImpl(int port) {
        super(new InetSocketAddress("0.0.0.0", port));
        this.gameprogress = new Gameprogress(connections);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOGGER.info("Neue Verbindung von " + conn.getRemoteSocketAddress());
        conn.send("WELCOME:Bitte Benutzernamen senden mit USER:<name>");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Player player = connections.remove(conn);
        if (player != null) {
            LOGGER.info(player.getUsername() + " hat die Verbindung getrennt.");
        } else {
            LOGGER.info("Verbindung ohne zugewiesenen Spieler getrennt.");
        }
        broadcastPlayerList();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info(String.format("Nachricht empfangen: " + message));

        if (message.startsWith("USER:")) {
            String name = message.substring("USER:".length());

            boolean name_taken = connections.values().stream()
                    .anyMatch(p -> p.getUsername().equalsIgnoreCase(name));
            if (name_taken) {
                LOGGER.info("USERNAME_TAKEN");
                conn.send("USERNAME_TAKEN");
                return;
            }

            Player player = new Player(name);
            connections.put(conn, player);
            LOGGER.info(String.format(name + " ist dem Spiel beigetreten."));
            broadcastPlayerList();
            conn.send("USERNAME_OK");

        } else if (message.startsWith("JOIN_TEAM:")) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String name = parts[1];
                String teamStr = parts[2];
                Player player = connections.get(conn);
                if (player != null && player.getUsername().equals(name)) {
                    try {
                        TeamColor team = TeamColor.valueOf(teamStr);
                        player.setTeamColor(team);
                        player.setSpymaster(false);
                        LOGGER.info(String.format(name + " ist Team " + team + " beigetreten."));
                        broadcastPlayerList();
                    } catch (IllegalArgumentException e) {
                        conn.send("MESSAGE:Ungültiges Team.");
                    }
                } else {
                    conn.send("MESSAGE:Spieler nicht gefunden.");
                }
            }

        } else if (message.startsWith(SPYMASTER_TOGGLE)) {
            String name = message.substring(SPYMASTER_TOGGLE.length());
            Player player = connections.get(conn);
            if (player != null && player.getUsername().equals(name)) {
                if (player.getTeamColor() == null) {
                    conn.send("MESSAGE:Bitte erst einem Team beitreten.");
                    return;
                }
                // Nur einer pro Team
                for (Map.Entry<WebSocket, Player> entry : connections.entrySet()) {
                    Player p = entry.getValue();
                    if (!entry.getKey().equals(conn) && p.getTeamColor() == player.getTeamColor() && p.getSpymaster()) {
                        p.setSpymaster(false);
                        entry.getKey().send(SPYMASTER_TOGGLE + p.getUsername() + ":false");
                    }
                }
                boolean newState = !player.getSpymaster();
                player.setSpymaster(newState);
                conn.send(SPYMASTER_TOGGLE + player.getUsername() + ":" + newState);
                LOGGER.info(String.format(player.getUsername() + (newState ? " ist jetzt Spymaster." : " ist kein Spymaster mehr.")));
                broadcastPlayerList();
            } else {
                conn.send("MESSAGE:Spieler nicht gefunden oder nicht zugeordnet.");
            }

        } else {
            gameprogress.processMessage(conn, message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.log(Level.SEVERE, "Fehler aufgetreten", ex);
    }

    @Override
    public void onStart() {
        LOGGER.info("WebSocket-Server erfolgreich gestartet auf Port " + getPort());
    }

    private void broadcastPlayerList() {
        StringBuilder sb = new StringBuilder("PLAYERS:");
        for (Player player : connections.values()) {
            sb.append(player.getUsername()).append(",")
                    .append(player.getTeamColor() != null ? player.getTeamColor().name() : "").append(",")
                    .append(player.getSpymaster()).append(";");
        }
        String msg = sb.toString();
        LOGGER.info(String.format("Sende Spielerliste: " + msg));
        for (WebSocket conn : connections.keySet()) {
            conn.send(msg);
        }
    }
}
