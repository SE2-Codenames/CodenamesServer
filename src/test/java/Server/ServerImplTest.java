package Server;

import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class ServerImplTest {
    private ServerImpl server;
    private WebSocket socket;

    @BeforeEach
    public void setUp() {
        server = new ServerImpl(1234);
        socket = mock(WebSocket.class);
    }

    @Test
    public void testUserRegistrationSuccess() {
        server.onMessage(socket, "USER:TestUser");
        verify(socket).send("USERNAME_OK");
    }

    @Test
    public void testUserRegistrationDuplicate() {
        server.onMessage(socket, "USER:TestUser");
        WebSocket secondSocket = mock(WebSocket.class);
        server.onMessage(secondSocket, "USER:TestUser");
        verify(secondSocket).send("USERNAME_TAKEN");
    }

    @Test
    public void testJoinTeamValid() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "JOIN_TEAM:TestUser:RED");
        verify(socket, atLeastOnce()).send(contains("PLAYERS:"));
    }

    @Test
    public void testJoinTeamInvalidTeam() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "JOIN_TEAM:TestUser:INVALIDTEAM");
        verify(socket).send("MESSAGE:Ungültiges Team.");
    }

    @Test
    public void testJoinTeamUnknownPlayer() {
        server.onMessage(socket, "JOIN_TEAM:Unknown:RED");
        verify(socket).send("MESSAGE:Spieler nicht gefunden.");
    }

    @Test
    public void testReadyStatus() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "READY:TestUser");
        verify(socket, atLeastOnce()).send(contains("PLAYERS:"));
    }

    @Test
    public void testSpymasterToggleWithoutTeam() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "SPYMASTER_TOGGLETestUser");
        verify(socket).send("MESSAGE:Bitte erst einem Team beitreten.");
    }

    @Test
    public void testUnknownCommandDelegatesToGameProgress() {
        WebSocket testSocket = mock(WebSocket.class);
        server.onMessage(testSocket, "UNKNOWN_COMMAND");
    }
}
