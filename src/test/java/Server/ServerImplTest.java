package Server;

import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class ServerImplTest {
    private ServerImpl server;
    private WebSocket socket;

    @BeforeEach
    void setUp() {
        server = new ServerImpl(1234);
        socket = mock(WebSocket.class);
    }

    @Test
    void testUserRegistrationSuccess() {
        server.onMessage(socket, "USER:TestUser");
        verify(socket).send("USERNAME_OK");
    }

    @Test
    void testUserRegistrationDuplicate() {
        server.onMessage(socket, "USER:TestUser");
        WebSocket secondSocket = mock(WebSocket.class);
        server.onMessage(secondSocket, "USER:TestUser");
        verify(secondSocket).send("USERNAME_TAKEN");
    }

    @Test
    void testJoinTeamValid() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "JOIN_TEAM:TestUser:RED");
        verify(socket, atLeastOnce()).send(contains("PLAYERS:"));
    }

    @Test
    void testJoinTeamInvalidTeam() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "JOIN_TEAM:TestUser:INVALIDTEAM");
        verify(socket).send("MESSAGE:Ungültiges Team.");
    }

    @Test
    void testJoinTeamUnknownPlayer() {
        server.onMessage(socket, "JOIN_TEAM:Unknown:RED");
        verify(socket).send("MESSAGE:Spieler nicht gefunden.");
    }

    @Test
    void testReadyStatus() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "READY:TestUser");
        verify(socket, atLeastOnce()).send(contains("PLAYERS:"));
    }

    @Test
    void testSpymasterToggleWithoutTeam() {
        server.onMessage(socket, "USER:TestUser");
        server.onMessage(socket, "SPYMASTER_TOGGLETestUser");
        verify(socket).send("MESSAGE:Bitte erst einem Team beitreten.");
    }

    @Test
    void testUnknownCommandDelegatesToGameProgress() {
        WebSocket testSocket = mock(WebSocket.class);
        server.onMessage(testSocket, "UNKNOWN_COMMAND");

        verify(testSocket).send(contains("MESSAGE:Spiel wurde noch nicht gestartet."));
    }
}
