import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerImpl {
    private ServerSocket serverSocket;
    private static final int PORT = 8081;
    private static ArrayList<UserManager> clients = new ArrayList<>();


    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (!serverSocket.isClosed()) {
            System.out.println("Server konnte gestartet werden.");
            Socket socket = serverSocket.accept();
            UserManager user = new UserManager(socket);
            clients.add(user);
            new Thread(user).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
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
