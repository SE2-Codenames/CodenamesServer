import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class TestTestClient {
    @Test
    public void testClientCanSendByeAndExit() throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 8081);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String greeting = in.readLine();
        assertEquals("Bitte Benutzernamen eingeben: ", greeting);

        out.println("TestClientUser");

        out.println("Hallo Server.Server!");

        String response = in.readLine();
        assertNotNull(response);
        assertTrue(response.contains("Hallo Server.Server!"));

        out.println("bye");
        
        socket.close();
    }
}
