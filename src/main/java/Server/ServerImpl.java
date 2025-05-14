package Server;

import model.Player.TeamColor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerImpl {
    private ServerSocket serverSocket;
    private static final int PORT = 8081;
    private static List<UserManager> clients = new CopyOnWriteArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(ServerImpl.class.getName());

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        LOGGER.log(Level.INFO, "Server gestartet auf Port {0}", port);
        while (!serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            UserManager user = new UserManager(socket, clients);
            clients.add(user);
            new Thread(user).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public static void broadcastPlayerList() {
        StringBuilder playerListString = new StringBuilder("PLAYERS:");
        for (UserManager user : clients) {
            playerListString.append(user.getPlayerInfo()).append(";");
        }
        String message = playerListString.toString();
        for (UserManager user : clients) {
            user.sendMessage(message);
        }
    }

    public static void broadcastMessage(String message) {
        for (UserManager user : clients) {
            user.sendMessage("MESSAGE:" + message);
        }
    }

    // Funktion, um den aktuellen Spymaster eines Teams zu entfernen
    private static void removeSpymasterFromTeam(TeamColor team) {
        for (UserManager user : clients) {
            if (user.getTeam() == team && user.isSpymaster()) {
                user.setSpymaster(false);
                user.sendMessage("SPYMASTER_TOGGLE:" + user.getUsername() + ":false"); // Informiere den Client
                break; // Es kann nur einen Spymaster pro Team geben, also beenden wir die Schleife, sobald wir ihn gefunden haben
            }
        }
    }

    // Diese Funktion bearbeitet die JOIN_TEAM Nachricht
    public static void handleJoinTeam(String playerName, String teamString) {
        TeamColor team = TeamColor.valueOf(teamString);
        for (UserManager user : clients) {
            if (user.getUsername().equals(playerName)) {
                TeamColor oldTeam = user.getTeam();
                user.setTeam(team);
                user.setSpymaster(false); // Jeder der das Team wechselt, ist nicht länger Spymaster
                if (oldTeam != null) {
                    removeSpymasterFromTeam(oldTeam); //Entferne Spymaster aus altem Team
                }
                user.sendMessage("JOIN_TEAM:" + playerName + ":" + team);
                broadcastPlayerList();
                LOGGER.log(Level.INFO, "Spieler {0} ist dem Team {1} beigetreten.", new Object[]{playerName, team});
                break;
            }
        }
    }

    // Diese Funktion bearbeitet die SPYMASTER_TOGGLE Nachricht
    public static void handleSpymasterToggle(String playerName) {
        for (UserManager user : clients) {
            if (user.getUsername().equals(playerName)) {
                TeamColor team = user.getTeam();
                if (team != null) { //Stelle sicher, dass der Spieler in einem Team ist.
                    // Entferne den aktuellen Spymaster des Teams, bevor ein neuer gesetzt wird
                    removeSpymasterFromTeam(team);
                    if (!user.isSpymaster()) {
                        user.setSpymaster(true);
                        user.sendMessage("SPYMASTER_TOGGLE:" + playerName + ":true");
                        LOGGER.log(Level.INFO, "Spieler {0} ist jetzt Spymaster.", playerName);
                    }
                    else{
                        user.setSpymaster(false);
                        user.sendMessage("SPYMASTER_TOGGLE:" + playerName + ":false");
                        LOGGER.log(Level.INFO, "Spieler {0} ist nicht länger Spymaster.", playerName);
                    }
                    broadcastPlayerList();
                    break;
                }
                else{
                    user.sendMessage("MESSAGE:You must be in a team to become a Spymaster");
                    LOGGER.log(Level.WARNING, "Spieler {0} ist nicht in einem Team und kann nicht Spymaster werden.", playerName);
                }
            }
        }
    }
}