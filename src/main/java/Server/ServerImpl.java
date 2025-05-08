package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerImpl {
    private ServerSocket serverSocket;
    private static final int PORT = 8081;
    private static List<UserManager> clients = new CopyOnWriteArrayList<>();

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server.Server gestartet auf Port " + port);
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
            user.sendMessage(message); // Verwende die öffentliche sendMessage()-Methode
        }
    }

    public static void broadcastMessage(String message) {
        for (UserManager user : clients) {
            user.sendMessage("MESSAGE:" + message); // Verwende die öffentliche sendMessage()-Methode
        }
    }
}