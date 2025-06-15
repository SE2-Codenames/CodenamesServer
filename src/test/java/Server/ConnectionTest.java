package Server;

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

    @Test
    public void testUsernameTaken() throws Exception {
        int port = 9091;
        ServerImpl server = new ServerImpl(port);
        server.start();

        URI uri = new URI("ws://localhost:" + port);
        CountDownLatch takenLatch = new CountDownLatch(1);

        WebSocketClient client1 = new WebSocketClient(uri) {
            @Override public void onOpen(ServerHandshake handshakedata) {
                send("USER:Stefan");
            }
            @Override public void onMessage(String message) {}
            @Override public void onClose(int code, String reason, boolean remote) {}
            @Override public void onError(Exception ex) {}
        };

        WebSocketClient client2 = new WebSocketClient(uri) {
            @Override public void onOpen(ServerHandshake handshakedata) {
                send("USER:Stefan");  //in case that name is different, test will fail
            }
            @Override public void onMessage(String message) {
                if (message.equals("USERNAME_TAKEN")) {
                    takenLatch.countDown();
                }
            }
            @Override public void onClose(int code, String reason, boolean remote) {}
            @Override public void onError(Exception ex) {}
        };

        client1.connectBlocking();
        Thread.sleep(200);
        client2.connect();

        boolean takenMessageReceived = takenLatch.await(3, TimeUnit.SECONDS);

        client1.close();
        client2.close();
        server.stop();

        assertTrue(takenMessageReceived, "Server should respond with USERNAME_TAKEN for duplicate usernames.");
    }

}
