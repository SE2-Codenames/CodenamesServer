package Server;

import model.Player.TeamColor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class UserManager implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private TeamColor team;
    private boolean isSpymaster = false;
    private static List<UserManager> clients; // Statische Referenz auf die Client-Liste

    public UserManager(Socket socket, List<UserManager> clients) {
        this.socket = socket;
        UserManager.clients = clients;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Bitte Benutzernamen eingeben: ");
            username = in.readLine();
            System.out.println(username + " ist dem Spiel beigetreten: " + username); // Logge den Benutzernamen
            out.println("USERNAME_OK"); // Sende Bestätigung für den Benutzernamen
            System.out.println(username + " hat USERNAME_OK erhalten."); // Logge nach dem Senden von OK
            ServerImpl.broadcastPlayerList();
            System.out.println(username + " hat die erste Spielerliste gesendet."); // Logge nach dem Senden der Liste

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(username + ": " + message);
                if (message.startsWith("JOIN_TEAM:")) {
                    String requestedTeam = message.substring("JOIN_TEAM:".length());
                    try {
                        this.team = TeamColor.valueOf(requestedTeam);
                        System.out.println(username + " ist dem Team " + team + " beigetreten.");
                        ServerImpl.broadcastPlayerList();
                    } catch (IllegalArgumentException e) {
                        sendMessage("Ungültiges Team.");
                    }
                } else if (message.equals("SPYMASTER_TOGGLE")) {
                    toggleSpymaster();
                } else if (message.equals("bye")) {
                    disconnect();
                    break;
                } else {
                    ServerImpl.broadcastMessage(username + ": " + message);
                }
            }
            System.out.println(username + " - Verbindung beendet (readLine gab null zurück)."); // Logge, wenn die Schleife endet
        } catch (IOException e) {
            System.out.println(username + " - IOException in run(): " + e.getMessage()); // Logge IOExceptions
            disconnect();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPlayerInfo() {
        return username + "," + (team != null ? team.toString() : "") + "," + isSpymaster;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void broadcastMessage(String message) {
        for (UserManager user : clients) {
            user.sendMessage("MESSAGE:" + message);
        }
    }

    private void toggleSpymaster() {
        if (team != null) {
            if (clients.stream().anyMatch(user -> user != this && user.team == this.team && user.isSpymaster)) {
                sendMessage("Es gibt bereits einen Spymaster im Team.");
            } else {
                this.isSpymaster = !this.isSpymaster;
                System.out.println(username + " ist nun Spymaster für Team " + team + ": " + isSpymaster);
                ServerImpl.broadcastPlayerList();
            }
        } else {
            sendMessage("Du musst zuerst einem Team beitreten, um Spymaster zu werden.");
        }
    }

    private void disconnect() {
        clients.remove(this);
        ServerImpl.broadcastPlayerList();
        ServerImpl.broadcastMessage(username + " hat das Spiel verlassen.");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}