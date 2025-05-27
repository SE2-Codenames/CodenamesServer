import Server.ServerImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConnectionTest {

    @Test
    public void testServerAcceptsConnection() throws Exception {
        int port = 9090;
        ServerImpl server = new ServerImpl(port);
        server.start();

        URI uri = new URI("ws://localhost:" + port);
        CountDownLatch latch = new CountDownLatch(1);

        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                latch.countDown();
            }

            @Override public void onMessage(String message) {}
            @Override public void onClose(int code, String reason, boolean remote) {}
            @Override public void onError(Exception ex) {}
        };

        client.connect();
        boolean connected = latch.await(3, TimeUnit.SECONDS);

        server.stop();

        assertTrue(connected, "Server should accept connections on 0.0.0.0");
    }
}
