package model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = new Player("Mihi");
        player2 = new Player("Edi");
    }

    @Test
    void testConstructor() {
        assertEquals("Mihi", player1.getUsername());
        assertNull(player1.getTeamColor());
        assertFalse(player1.getSpymaster());
    }

    @Test
    void testSetGetSpymaster() {
        player1.setSpymaster(true);
        assertTrue(player1.getSpymaster());
        player2.setSpymaster(false);
        assertFalse(player2.getSpymaster());
    }

    @Test
    void testSetGetColor() {
        player1.setTeamColor(TeamColor.RED);
        assertEquals(TeamColor.RED, player1.getTeamColor());
        player2.setTeamColor(TeamColor.BLUE);
        assertEquals(TeamColor.BLUE, player2.getTeamColor());
    }

    @Test
    void testToInfoString() {
        player1.setTeamColor(TeamColor.RED);
        player1.setSpymaster(true);
        assertEquals("Mihi,RED,true", player1.toInfoString());
    }

    @Test
    void testToInfoStringNoTeamColor() {
        player1.setSpymaster(true);
        assertEquals("Mihi,,true", player1.toInfoString());
    }


    @Test
    void testRandomName() {
        Player random = new Player("Pablo");
        assertNotEquals(random, player1);
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(null, player1);
    }

    @Test
    void testSetGetReady() {
        assertFalse(player1.isReady()); // initial false
        player1.setReady(true);
        assertTrue(player1.isReady());
    }
}
