import static org.junit.jupiter.api.Assertions.*;

import Server.ServerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class TestServerImplManager {
    private ServerImpl server;
    private Thread serverThread;

    @BeforeEach
    void setUp() {
        server = new ServerImpl();
        serverThread = new Thread(() -> {
            try {
                server.start(8081); // Port ändern, um Konflikte zu vermeiden
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        try {
            Thread.sleep(500); // kurz warten, bis der Server.Server läuft
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testServerStart() {
        assertDoesNotThrow(() -> {
            Thread.sleep(500);
            assertTrue(isServerRunning(), "Server.Server wurde gestartet");
        });
    }

    @Test
    void testServerClient() throws Exception {
        try (Socket socket = new Socket("localhost", 8081)) {
            assertTrue(socket.isConnected());

            assertFalse(socket.isClosed());
            assertTrue(socket.getPort() == 8081);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("test");
            assertDoesNotThrow(() -> in.readLine());
        }
    }

    @Test
    void testUserRegister() throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 8081);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        assertEquals("Bitte Benutzernamen eingeben: ", in.readLine());

        out.println("testUser");

        socket.close();
    }

    @Test
    void testMessage() throws IOException, InterruptedException {
        Socket client1 = new Socket("localhost", 8081);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        PrintWriter out1 = new PrintWriter(client1.getOutputStream(), true);
        in1.readLine();
        out1.println("client1");

        Socket client2 = new Socket("localhost", 8081);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        PrintWriter out2 = new PrintWriter(client2.getOutputStream(), true);
        in2.readLine();
        out2.println("client2");

        Thread.sleep(200);

        out1.println("Hello from client1");

        String received = in2.readLine();
        assertNotNull(received);

        client1.close();
        client2.close();
    }

    @Test
    void testDisconnect() throws IOException, InterruptedException {
        Socket client = new Socket("localhost", 8081);
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
        try (Socket socket = new Socket("localhost", 8081)) {
            return socket.isConnected();
        } catch (IOException e) {
            return false;
        }
    }

}
