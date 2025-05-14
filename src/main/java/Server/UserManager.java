package Server;

import model.Player.TeamColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManager implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private TeamColor team;  // Verwende TeamColor
    private boolean isSpymaster = false;
    private static List<UserManager> clients;
    private Gameprogress gameprogress;
    private static final Logger LOGGER = Logger.getLogger(UserManager.class.getName());

    public UserManager(Socket socket, List<UserManager> clients) {
        this.socket = socket;
        UserManager.clients = clients;
        this.gameprogress = new Gameprogress(clients);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Bitte Benutzernamen eingeben: ");
            username = in.readLine();
            LOGGER.log(Level.INFO, "{0} ist dem Spiel beigetreten: {0}", username);
            out.println("USERNAME_OK");
            LOGGER.log(Level.INFO, "{0} hat USERNAME_OK erhalten.", username);
            ServerImpl.broadcastPlayerList();
            LOGGER.log(Level.INFO, "{0} hat die erste Spielerliste gesendet.", username);

            String message;
            while ((message = in.readLine()) != null) {
                LOGGER.log(Level.INFO, "{0}: {1}", new Object[]{username, message});
                if (message.startsWith("JOIN_TEAM:")) {
                    String requestedTeam = message.substring("JOIN_TEAM:".length());
                    try {
                        this.team = TeamColor.valueOf(requestedTeam);
                        LOGGER.log(Level.INFO, "{0} ist dem Team {1} beigetreten.", new Object[]{username, team});
                        ServerImpl.handleJoinTeam(username, requestedTeam);
                    } catch (IllegalArgumentException e) {
                        sendMessage("Ungültiges Team.");
                        LOGGER.log(Level.WARNING, "Ungültiges Team angefordert: {0} von {1}", new Object[]{requestedTeam, username});
                    }
                } else if (message.equals("SPYMASTER_TOGGLE")) {
                    ServerImpl.handleSpymasterToggle(username);
                } else if (message.equals("bye")) {
                    disconnect();
                    break;
                } else if (message.equals("START_GAME") || message.startsWith("HINT:") || message.startsWith("SELECT:")) {
                    LOGGER.log(Level.INFO, "Verarbeitung: {0}", message);
                    gameprogress.processMessage(message, out);
                } else {
                    ServerImpl.broadcastMessage(username + ": " + message);
                }
            }
            LOGGER.log(Level.INFO, "{0} - Verbindung beendet (readLine gab null zurück).", username);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "{0} - IOException in run(): {1}", new Object[]{username, e.getMessage()});
            disconnect();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Fehler beim Schließen des Sockets: {0}", e.getMessage());
            }
        }
    }

    public String getPlayerInfo() {
        String teamString = (team != null) ? team.toString() : "";
        return username + "," + teamString + "," + isSpymaster;
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }

    public String getUsername() {
        return username;
    }

    public TeamColor getTeam() {
        return team;
    }

    public void setTeam(TeamColor team) {
        this.team = team;
    }

    public boolean isSpymaster() {
        return isSpymaster;
    }

    public void setSpymaster(boolean spymaster) {
        isSpymaster = spymaster;
    }

    private void disconnect() {
        clients.remove(this);
        ServerImpl.broadcastPlayerList();
        ServerImpl.broadcastMessage(username + " hat das Spiel verlassen.");
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Schließen des Sockets: {0}", e.getMessage());
        }
    }

    public PrintWriter getWriter() {
        return out;
    }
}