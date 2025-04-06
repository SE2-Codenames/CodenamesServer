import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class TestServerManager {
    private Server server;
    private Thread serverThread;

    @BeforeEach
    void setUp() {
        serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    @Test
    void testServerStart() {
        assertDoesNotThrow(() -> {
            Thread.sleep(500);
            assertTrue(isServerRunning(), "Server wurde gestartet");
        });
    }

    @Test
    void testServerClient() throws Exception {
        Socket socket = new Socket("localhost", 8080);  // Verbinde den Client
        Thread.sleep(500);
    }

    @Test
    void testUserRegister() throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 8080);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        assertEquals("Bitte Benutzernamen eingeben: ", in.readLine());

        out.println("testUser");

        socket.close();
    }

    @Test
    void testMessage() throws IOException, InterruptedException {
        Socket client1 = new Socket("localhost", 8080);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        in1.readLine();
        out1.println("client1");

        Socket client2 = new Socket("localhost", 8080);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        in2.readLine();
        out2.println("client2");

        Thread.sleep(200);

        out1.println("Hello from client1");

        String received = in2.readLine();
        assertNotNull(received);
        assertEquals("client1: Hello from client1", received);

        client1.close();
        client2.close();
    }

    @Test
    void testDisconnect() throws IOException, InterruptedException {
        Socket client = new Socket("localhost", 8080);
        client.setSoTimeout(500);

        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);

        in.readLine();
        out.println("testUser");
        Thread.sleep(200);

        out.println("bye");

        client.close();

        assertTrue(client.isClosed());
    }

    private boolean isServerRunning() {
        try (Socket socket = new Socket("localhost", 8080)) {
            return socket.isConnected();
        } catch (IOException e) {
            return false;
        }
    }

}
