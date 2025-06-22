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
                setCommunication(comm);
                super.processMessage(c, message);
            }
        };

        gameprogress.processMessage(socket, "START_GAME");
        verify(socket, atLeastOnce()).send("SHOW_GAMEBOARD");
    }

    @Test
    void testSpymasterHint(){
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestHint("apple", "2");
        comm.setTestInput("HINT:apple:2");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate())
                .thenReturn(GameState.SPYMASTER_TURN)
                .thenReturn(GameState.GAME_OVER);

        gameprogress = new Gameprogress(sessions) {
            {
                setCommunication(comm);
                setGame(mockGame);
            }

            @Override
            public void broadcastGameState() { //only want to test if method gets called. Not send data
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
                setCommunication(comm);
                setGame(mockGame);
            }

            @Override
            public void broadcastGameState() { //only want to test if method gets called. Not send data
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
                setCommunication(comm);
                setGame(null);
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
                setCommunication(comm);
                setGame(mockGame);
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
                setCommunication(comm);
                setGame(mockGame);
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
                setCommunication(comm);
                setGame(mockGame);
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
                setCommunication(comm);
                setGame(mockGame);
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
        Game mockGame = mock(Game.class);
        when(mockGame.getCurrentTeam()).thenReturn(TeamColor.RED);
        when(mockGame.getScore()).thenReturn(new int[]{1, 2});
        when(mockGame.checkAssassin()).thenReturn(false);

        gameprogress = new Gameprogress(sessions);
        gameprogress.setGame(mockGame);
        gameprogress.gameReset();

        assertNotNull(gameprogress.getGame());
        assertEquals(GameState.LOBBY, gameprogress.getGame().getGamestate());

        verify(socket).close(eq(1000), contains("reset"));
        verify(socket).send(contains("GAME_OVER"));
    }

    @Test
    void testExposeCommand() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("EXPOSE:apple");

        Game mockGame = mock(Game.class);
        when(mockGame.checkExpose()).thenReturn(true);
        when(mockGame.getCurrentTeam()).thenReturn(TeamColor.BLUE);
        when(mockGame.addTeamCard(any())).thenReturn(true);

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "EXPOSE:apple");

        verify(socket).send(startsWith("CHAT:"));
        verify(socket).send(contains("\"type\":\"expose\""));
        verify(socket).send(contains("Opponent used a forbidden word"));
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
                setCommunication(comm);
                setGame(realGame);
            }
        };

        assertDoesNotThrow(() -> gameprogress.processMessage(socket, "HINT:apple"));
    }

    @Test
    void testCardMarkedMarksCorrectCard(){
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("MARK:5");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "MARK:5");
        verify(mockGame).toggleMark(5);
    }

    @Test
    void testClearMarksClearsAll(){
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("{\"clearMarks\":true}");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN); // <- wichtig!

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "{\"clearMarks\":true}");
        verify(mockGame).clearMarks();
    }

    @Test
    void testStartGameAlreadyRunning() {
        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);

        gameprogress = new Gameprogress(sessions);
        gameprogress.setGame(mockGame);

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
        when(mockGame.getScore()).thenReturn(new int[]{1, -1});

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "EXPOSE:hint");

        verify(mockGame).setScore(any());

        verify(socket).send(startsWith("CHAT:"));
        verify(socket).send(contains("\"type\":\"expose\""));
        verify(socket).send(contains("No cards left"));
    }

    @Test
    void testUnknownInputHandled() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("UNKNOWN_COMMAND");

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(null);
        }};

        gameprogress.processMessage(socket, "UNKNOWN_COMMAND");
        verify(socket).send(contains("Spiel wurde noch nicht gestartet"));
    }

    @Test
    void testSkipTurnChangesState() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("SKIP_TURN");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "SKIP_TURN");

        verify(mockGame).endTurn();
        verify(mockGame).clearMarks();
        verify(socket).send(contains("Turn skipped"));
    }

    @Test
    void testSkipTurnIgnoredIfNotOperativeTurn() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("SKIP_TURN");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.SPYMASTER_TURN); // falscher Zustand

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "SKIP_TURN");

        verify(mockGame, never()).endTurn();
        verify(socket).send(contains("Warte auf Hinweis des Spymasters"));
    }

    @Test
    void testSkipTurnInGameOverIgnored() {
        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("SKIP_TURN");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.GAME_OVER);

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "SKIP_TURN");

        verify(mockGame, never()).endTurn();
        verify(socket).send(contains("GAME_OVER"));
    }

    @Test
    void testAllPlayersNotifiedOnSkip() {
        WebSocket socket2 = mock(WebSocket.class);
        sessions.put(socket2, new Player("Tester"));

        TestCommunication comm = new TestCommunication(socket);
        comm.setTestInput("SKIP_TURN");

        Game mockGame = mock(Game.class);
        when(mockGame.getGamestate()).thenReturn(GameState.OPERATIVE_TURN);

        gameprogress = new Gameprogress(sessions) {{
            setCommunication(comm);
            setGame(mockGame);
        }};

        gameprogress.processMessage(socket, "SKIP_TURN");

        verify(socket).send(contains("Turn skipped"));
        verify(socket2).send(contains("Turn skipped"));
    }

}
