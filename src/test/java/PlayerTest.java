import model.Player.Player;
import model.Player.TeamColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private Player player1;
    private Player player2;

    @BeforeEach
    public void setUp() {
        player1 = new Player("Mihi");
        player2 = new Player("Edi");
    }

    @Test
    public void testConstructor() {
        assertEquals("Mihi", player1.getUsername());
        assertNull(player1.getTeamColor());
        assertFalse(player1.getSpymaster());
    }

    @Test
    public void testSetGetPlayerRole() {
        player1.setPlayerRole(true);
        assertTrue(player1.getSpymaster());
        player2.setPlayerRole(false);
        assertFalse(player2.getSpymaster());
    }

    @Test
    public void testSetGetColor() {
        player1.setTeamColor(TeamColor.RED);
        assertEquals(TeamColor.RED, player1.getTeamColor());
        player2.setTeamColor(TeamColor.BLUE);
        assertEquals(TeamColor.BLUE, player2.getTeamColor());
    }

    @Test
    public void testUpperCase() {
        Player player3 = new Player("mihi");
        assertEquals(player3, player1);
    }

    @Test
    public void testRandomName() {
        Player random = new Player("Pablo");
        assertNotEquals(random, player1);
    }

    @Test
    public void testEqualsNull() {
        assertNotEquals(player1, null);
    }

}
