import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try{
            Socket socket = new Socket("localhost", 8080);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));


            new Thread(() -> {
                try{
                    String server;
                    while((server = in.readLine()) != null) {
                        System.out.println(server);
                    }
                } catch (IOException e) {
                    System.out.println("Verbindung zum Server verloren.");
                }
            }).start();

            String userMessage;
            while((userMessage = stdIn.readLine()) != null) {
                out.println(userMessage);
                if(userMessage.equals("bye")) {
                    break;
                }
            }

        } catch(Exception e) {
            System.out.println("Fehler beim Verbinden");
        }
    }
}
