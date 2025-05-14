import Server.Communication;
import Server.Game;
import Server.GameException;
import Server.Gameprogress;
import model.GameState;
import model.Player.Player;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class GameProgressTest {
    @Mock
    private WebSocket socket;

    @InjectMocks
    private Gameprogress gameprogress;

    private Map<WebSocket, Player> sessions;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessions = new HashMap<>();
        sessions.put(socket, new Player("Mihi"));
        gameprogress = new Gameprogress(sessions);
    }

    @Test
    public void testStartGame() {
        Communication comm = spy(new Communication(socket));
        comm.setInput("START_GAME");

        gameprogress = new Gameprogress(sessions) {
            @Override
            public void processMessage(WebSocket c, String message) {
                this.communication = comm;
                super.processMessage(c, message);
            }
        };

        gameprogress.processMessage(socket, "START_GAME");
        verify(socket, atLeastOnce()).send(eq("SHOW_GAMEBOARD"));
    }

    @Test
    public void testSpymasterHint() throws Exception {
        Communication comm = spy(new Communication(socket));
        comm.setInput("HINT:apple:2");

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mock(Game.class);
                when(game.getGamestate()).thenReturn(GameState.SPYMASTER_TURN);
            }

            @Override
            public void processMessage(WebSocket c, String message) {
                this.communication = comm;
                super.processMessage(c, message);
            }

        };

        gameprogress.processMessage(socket, "HINT:apple:2");
        verify(gameprogress.game).getClue(new String[]{"apple", "2"});
    }

    @Test
    public void testOperativeTurn() throws Exception {
        Communication comm = spy(new Communication(socket));
        comm.setInput("SELECT:5");

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mock(Game.class);
                when(game.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);
            }

            @Override
            public void processMessage(WebSocket c, String message) {
                this.communication = comm;
                super.processMessage(c, message);
            }

        };

        gameprogress.processMessage(socket, "SELECT:5");
        verify(gameprogress.game).guessCard(eq(5));
    }

    @Test
    public void testGameNotStarted() {
        Communication comm = spy(new Communication(socket));
        comm.setInput("SELECT:3");

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = null;
            }

            @Override
            public void processMessage(WebSocket c, String message) {
                this.communication = comm;
                super.processMessage(c, message);
            }
        };

        gameprogress.processMessage(socket, "SELECT:3");
        verify(socket).send(contains("Spiel wurde noch nicht gestartet"));
    }

    @Test
    public void testGameException() throws Exception {
        Communication comm = spy(new Communication(socket));
        comm.setInput("SELECT:1");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);
        doThrow(new GameException("Testfehler")).when(mockGame).guessCard(anyInt());

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void processMessage(WebSocket c, String message) {
                this.communication = comm;
                super.processMessage(c, message);
            }

        };

        gameprogress.processMessage(socket, "SELECT:1");
        verify(socket).send(contains("GameException: Testfehler"));
    }

    @Test
    public void testBrokeException() {
        Communication comm = spy(new Communication(socket));
        comm.setInput("BAD_INPUT");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenThrow(new RuntimeException("kaputt"));

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void processMessage(WebSocket c, String message) {
                this.communication = comm;
                super.processMessage(c, message);
            }
        };

        gameprogress.processMessage(socket, "BAD_INPUT");
        verify(socket).send(contains("Unexpected error:"));
    }

    @Test
    public void testGameLobbyGameOver() {

        Communication comm = spy(new Communication(socket));
        comm.setInput("IRRELEVANT");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.LOBBY);

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void broadcastGameState() {
                socket.send("LOBBY");
            }
        };

        gameprogress.processMessage(socket, "ANY");
        verify(socket).send(contains("LOBBY"));
    }

    @Test
    public void testGameStateGameOver() {
        Communication comm = spy(new Communication(socket));
        comm.setInput("IRRELEVANT");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.GAME_OVER);

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void broadcastGameState() {
                socket.send("GAME_OVER");
            }
        };

        gameprogress.processMessage(socket, "ANY");
        verify(socket).send(contains("GAME_OVER"));
    }

}
