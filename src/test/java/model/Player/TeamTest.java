package model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TeamTest {
    private Team team1;
    private Team team2;

    @BeforeEach
    public void setUp() {
        team1 = new Team(TeamColor.RED);
        team2 = new Team(TeamColor.BLUE);
    }

    @Test
    public void testConstructor() {
        assertEquals(TeamColor.RED, team1.getColor());
        assertEquals(9, team1.getCardsRemaining());
        assertFalse(team1.isCurrentTurn());
        assertEquals(TeamColor.BLUE, team2.getColor());
        assertEquals(8, team2.getCardsRemaining());
        assertFalse(team2.isCurrentTurn());
    }

    @Test
    public void testSetGetCardsRemain() {
        team1.setCardsRemaining(3);
        assertEquals(3, team1.getCardsRemaining());
        team2.setCardsRemaining(6);
        assertEquals(6, team2.getCardsRemaining());
    }

    @Test
    public void testStartEnd() {
        team1.startTurn();
        assertTrue(team1.isCurrentTurn());
        team1.endTurn();
        assertFalse(team1.isCurrentTurn());
    }

    @Test
    public void testIsCurrentTurn() {
        team1.setCurrentTurn(true);
        assertTrue(team1.isCurrentTurn());
    }

    @Test
    public void testWon() {
        team1.setCardsRemaining(0);
        team2.setCardsRemaining(3);
        assertTrue(team1.hasWon());
        assertFalse(team2.hasWon());
    }

    @Test
    public void testToString() {
        String s = team1.toString();
        assertTrue(s.contains("RED"));
        assertTrue(s.contains("Cards left: 9"));
    }
}
