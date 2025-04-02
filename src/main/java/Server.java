import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 8080;
    private static ArrayList<UserManager> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server wurde gestartet");
            while (true) {
                Socket socketUser = serverSocket.accept();
                UserManager user = new UserManager(socketUser);
                clients.add(user);
                new Thread(user).start();
            }


        } catch (Exception e) {
            System.out.println("Server Fehler - kann nicht gestartet werden");
        }
    }

    private static class UserManager implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        private UserManager(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Bitte Benutzernamen eingeben: ");
                username = in.readLine();
                System.out.println(username + " ist dem Spiel beigetreten.");

                String message;
                while((message = in.readLine()) != null) {
                    System.out.println(username + ": " + message);
                    show(username + ": " + message);
                    if(message.equals("bye")) {
                        disconnect();
                    }
                }
            } catch(Exception e) {
                System.out.println(username + " von der Verbindung getrennt");
            }


        }

        private void show(String message) {
            for(UserManager user : clients) {
                user.out.println(message);
            }
        }

        private void disconnect() {
            try{
                clients.remove(this);
                socket.close();
                show(username + " hat das Spiel verlassen.");
            } catch(Exception e) {
                System.out.println("Fehler beim Verlassen aufgetreten.");
            }
        }
    }
}
