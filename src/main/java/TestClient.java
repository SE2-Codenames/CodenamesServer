import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        new TestClient().runClient(System.in, System.out);
    }

    public void runClient(InputStream inStream, PrintStream outStream) {
        try (
                Socket socket = new Socket("localhost", 8081);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(inStream));
        ) {
            Thread listener = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        outStream.println(serverMessage);
                    }
                } catch (IOException e) {
                    outStream.println("Verbindung zum Server verloren.");
                }
            });

            listener.setDaemon(true);
            listener.start();

            String userMessage;
            while ((userMessage = stdIn.readLine()) != null) {
                out.println(userMessage);
                if ("bye".equalsIgnoreCase(userMessage)) {
                    break;
                }
            }

        } catch (IOException e) {
            outStream.println("Fehler beim Verbinden:");
            e.printStackTrace(outStream);
        }
    }
}
