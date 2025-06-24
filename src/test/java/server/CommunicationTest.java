package server;

import model.card.Card;
import model.card.CardRole;
import model.GameState;
import model.player.TeamColor;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunicationTest {

    private WebSocket mockSocket;
    private Communication comm;

    @BeforeEach
    void setUp() {
        mockSocket = mock(WebSocket.class);
        comm = new Communication(mockSocket);
    }

    // ===== Eingehende Nachrichten =====

    @Test
    void testIsGameStartRequested() {
        comm.setInput("START_GAME");
        assertTrue(comm.isGameStartRequested());
    }

    @Test
    void testIsHint() {
        comm.setInput("HINT:apple:3");
        assertTrue(comm.isHint());
    }

    @Test
    void testIsCardSelection() {
        comm.setInput("SELECT:5");
        assertTrue(comm.isCardSelection());
    }

    @Test
    void testIsCardMarked() {
        comm.setInput("MARK:12");
        assertTrue(comm.isCardMarked());
    }

    @Test
    void testIsExposeCommand() {
        comm.setInput("EXPOSE:banana");
        assertTrue(comm.isExposeCommand());
    }

    // ===== Nachrichtenwerte auslesen =====

    @Test
    void testGetHintValid() {
        comm.setInput("HINT:fruit:2");
        String[] result = comm.getHint();
        assertEquals("fruit", result[0]);
        assertEquals("2", result[1]);
    }

    @Test
    void testGetHintInvalidFormat() {
        comm.setInput("HINT:invalid");
        String[] result = comm.getHint();
        assertEquals("", result[0]);
        assertEquals("0", result[1]);
    }

    @Test
    void testGetSelectedCardValid() {
        comm.setInput("SELECT:7");
        assertEquals(7, comm.getSelectedCard());
    }

    @Test
    void testGetSelectedCardInvalid() {
        comm.setInput("SELECT:abc");
        assertEquals(-1, comm.getSelectedCard());
    }

    @Test
    void testGetMarkedCardValid() {
        comm.setInput("MARK:14");
        assertEquals(14, comm.getMarkedCard());
    }

    @Test
    void testGetMarkedCardInvalid() {
        comm.setInput("MARK:invalid");
        assertEquals(-1, comm.getMarkedCard());
    }

    @Test
    void testGetExposeData() {
        comm.setInput("EXPOSE:cheat");
        assertEquals("cheat", comm.getExposeData());
    }

    @Test
    void testClearMarksRequestedTrue() {
        comm.setInput("{\"clearMarks\":true}");
        assertTrue(comm.clearMarksRequested());
    }

    @Test
    void testClearMarksRequestedFalse() {
        comm.setInput("randomInput");
        assertFalse(comm.clearMarksRequested());
    }

    // ===== Sendemethoden =====

    @Test
    void testSendGameState() {
        Card card = new Card("Tree", CardRole.NEUTRAL);
        comm.sendGameState(GameState.OPERATIVE_TURN, TeamColor.RED, List.of(card), new int[]{3, 4}, "nature");

        verify(mockSocket).send(startsWith("GAME_STATE:"));
    }

    @Test
    void testSendMarked() {
        boolean[] marked = new boolean[25];
        marked[2] = true;
        comm.sendMarked(marked);

        verify(mockSocket).send(startsWith("MARK:"));
    }

    @Test
    void testSendHint() {
        comm.sendHint("apple", 3);
        verify(mockSocket).send(contains("hint"));
    }

    @Test
    void testSendCard() {
        Card card = new Card("River", CardRole.BLUE);
        comm.sendCard(card);
        verify(mockSocket).send(contains("card"));
    }

    @Test
    void testSendExpose() {
        comm.sendExpose("cheat activated");
        verify(mockSocket).send(contains("cheat activated"));
    }

    @Test
    void testSendWin() {
        comm.sendWin(TeamColor.RED, new int[]{8, 9}, true);
        verify(mockSocket).send(contains("GAME_OVER"));
        verify(mockSocket).send(contains("RED"));
    }

    @Test
    void testIsSkippedTurn() {
        comm.setInput("SKIP_TURN");
        assertTrue(comm.isSkippedTurn());
    }

    @Test
    void testSendMessage() {
        comm.sendMessage("Testnachricht");
        verify(mockSocket).send(contains("Testnachricht"));
        verify(mockSocket).send(contains("\"type\":\"message\""));
    }

    @Test
    void testGetHintEmpty() {
        comm.setInput("");
        String[] result = comm.getHint();
        assertEquals("", result[0]);
        assertEquals("0", result[1]);
    }

    @Test
    void testNullInputHandling() {
        comm.setInput(null);
        assertFalse(comm.isGameStartRequested());
        assertFalse(comm.isHint());
        assertFalse(comm.isCardSelection());
        assertFalse(comm.isCardMarked());
        assertFalse(comm.isExposeCommand());
        assertFalse(comm.isSkippedTurn());
    }

    @Test
    void testGetSelectedCardNegative() {
        comm.setInput("SELECT:-5");
        assertEquals(-5, comm.getSelectedCard());  // Sollte evtl. geblockt werden?
    }
}
