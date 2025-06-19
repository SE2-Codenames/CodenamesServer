package Server;

import model.Card.WordBank;
import model.GameState;
import model.Player.Player;
import model.Player.TeamColor;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameProgressTest {

    private WebSocket socket;
    private Gameprogress gameprogress;
    private Map<WebSocket, Player> sessions;

    @BeforeEach
    void setUp() {
        socket = mock(WebSocket.class);
        sessions = new HashMap<>();
        sessions.put(socket, new Player("Mihi"));
        gameprogress = new Gameprogress(sessions);
    }

    @Test
    void testStartGame() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("START_GAME");

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
    void testSpymasterHint() throws Exception {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestHint("apple", "2");
        comm.setTestInput("HINT:apple:2");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate())
                .thenReturn(GameState.SPYMASTER_TURN)
                .thenReturn(GameState.GAME_OVER);

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void broadcastGameState() {
            }
        };

        gameprogress.processMessage(socket, "HINT:apple:2");
        verify(mockGame).getClue(new String[]{"apple", "2"});
    }

    @Test
    void testOperativeTurn() throws Exception {
        TestCommunication comm = new TestCommunication(socket);
        comm.setSelectedCard(5);
        comm.setTestInput("SELECT:5");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate())
                .thenReturn(GameState.OPERATIVE_TURN)
                .thenReturn(GameState.GAME_OVER);

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void broadcastGameState() {
            }
        };

        gameprogress.processMessage(socket, "SELECT:5");
        verify(mockGame).guessCard(5);
    }

    @Test
    void testGameNotStarted() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("SELECT:3");

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = null;
            }
        };

        gameprogress.processMessage(socket, "SELECT:3");
        verify(socket).send(contains("Spiel wurde noch nicht gestartet"));
    }

    @Test
    void testGameException() throws Exception {
        TestCommunication comm = new TestCommunication(socket);
        comm.setSelectedCard(1);
        comm.setTestInput("SELECT:1");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);
        doThrow(new GameException("Testfehler")).when(mockGame).guessCard(anyInt());

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }
        };

        gameprogress.processMessage(socket, "SELECT:1");
        verify(socket).send(contains("GameException: Testfehler"));
    }

    @Test
    void testBrokeException() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("BAD_INPUT");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenThrow(new RuntimeException("kaputt"));

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }
        };

        gameprogress.processMessage(socket, "BAD_INPUT");
        verify(socket).send(contains("Unexpected error:"));
    }

    @Test
    void testGameLobbyGameOver() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("IRRELEVANT");

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
        verify(socket).send(contains("Warte auf Spielstart."));
    }

    @Test
    void testGameStateGameOver() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("IRRELEVANT");

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

    @Test
    void testGameReset() {
        gameprogress = new Gameprogress(sessions) {
            @Override
            public void broadcastGameState() {
                socket.send("RESET_BROADCAST");
            }
        };

        gameprogress.gameReset();

        assertNotNull(gameprogress.game);
        assertEquals(GameState.LOBBY, gameprogress.game.getGamestate());

        verify(socket).send("RESET");
        verify(socket).send("RESET_BROADCAST");
    }

    @Test
    void testExposeCommand() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("EXPOSE:apple");

        Game mockGame = mock(Game.class);
        when(mockGame.checkExpose()).thenReturn(true);
        when(mockGame.getCurrentTeam()).thenReturn(TeamColor.BLUE);
        when(mockGame.addTeamCard(any())).thenReturn(true);

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = mockGame;
            }

            @Override
            public void broadcastGameState() {
            }
        };

        gameprogress.processMessage(socket, "EXPOSE:apple");

        verify(mockGame).addTeamCard(any());
        verify(socket).send(contains("Expose successful."));
    }

    @Test
    void testInvalidHintFormatHandled() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("HINT:apple");

        List<String> words = new ArrayList<>();
        for (int i = 0; i < 25; i++) words.add("Word" + i);

        WordBank mockWordBank = mock(WordBank.class);
        when(mockWordBank.getRandomWords(25)).thenReturn(words);

        Game realGame = new Game(mockWordBank);
        realGame.setGamestate(GameState.SPYMASTER_TURN);

        gameprogress = new Gameprogress(sessions) {
            {
                this.communication = comm;
                this.game = realGame;
            }
        };

        assertDoesNotThrow(() -> gameprogress.processMessage(socket, "HINT:apple"));
    }

    @Test
    void testCardMarkedMarksCorrectCard() throws Exception {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("MARK:5");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);

        gameprogress = new Gameprogress(sessions) {{
            communication = comm;
            game = mockGame;
        }};

        gameprogress.processMessage(socket, "MARK:5");
        verify(mockGame).toggleMark(5);
    }

    @Test
    void testClearMarksClearsAll() throws Exception {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("{\"clearMarks\":true}");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN); // <- wichtig!

        gameprogress = new Gameprogress(sessions) {{
            communication = comm;
            game = mockGame;
        }};

        gameprogress.processMessage(socket, "{\"clearMarks\":true}");
        verify(mockGame).clearMarks();
    }

    @Test
    void testStartGameAlreadyRunning() {
        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);

        gameprogress = new Gameprogress(sessions);
        gameprogress.game = mockGame;

        gameprogress.processMessage(socket, "START_GAME");
        verify(socket).send(contains("Das Spiel läuft bereits"));
    }

    @Test
    void testExposeWithNoNeutralCards() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("EXPOSE:hint");

        Game mockGame = mock(Game.class);
        when(mockGame.checkExpose()).thenReturn(false);
        when(mockGame.getCurrentTeam()).thenReturn(TeamColor.RED);
        when(mockGame.addTeamCard(any())).thenReturn(false);
        when(mockGame.getScore()).thenReturn(new int[]{1, 1});

        gameprogress = new Gameprogress(sessions) {{
            communication = comm;
            game = mockGame;
        }};

        gameprogress.processMessage(socket, "EXPOSE:hint");

        verify(socket).send(contains("No cards left."));
        verify(mockGame).setScore(any());
    }

    @Test
    void testUnknownInputHandled() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("UNKNOWN_COMMAND");

        gameprogress = new Gameprogress(sessions) {{
            communication = comm;
            game = null;
        }};

        gameprogress.processMessage(socket, "UNKNOWN_COMMAND");
        verify(socket).send(contains("Spiel wurde noch nicht gestartet"));
    }
}
