package model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TeamTest {
    private Team team1;
    private Team team2;

    @BeforeEach
    void setUp() {
        team1 = new Team(TeamColor.RED);
        team2 = new Team(TeamColor.BLUE);
    }

    @Test
    void testConstructor() {
        assertEquals(TeamColor.RED, team1.getColor());
        assertEquals(9, team1.getCardsRemaining());
        assertFalse(team1.isCurrentTurn());
        assertEquals(TeamColor.BLUE, team2.getColor());
        assertEquals(8, team2.getCardsRemaining());
        assertFalse(team2.isCurrentTurn());
    }

    @Test
    void testSetGetCardsRemain() {
        team1.setCardsRemaining(3);
        assertEquals(3, team1.getCardsRemaining());
        team2.setCardsRemaining(6);
        assertEquals(6, team2.getCardsRemaining());
    }

    @Test
    void testStartEnd() {
        team1.startTurn();
        assertTrue(team1.isCurrentTurn());
        team1.endTurn();
        assertFalse(team1.isCurrentTurn());
    }

    @Test
    void testIsCurrentTurn() {
        team1.setCurrentTurn(true);
        assertTrue(team1.isCurrentTurn());
    }

    @Test
    void testWon() {
        team1.setCardsRemaining(0);
        team2.setCardsRemaining(3);
        assertTrue(team1.hasWon());
        assertFalse(team2.hasWon());
    }

    @Test
    void testToString() {
        String s = team1.toString();
        assertTrue(s.contains("RED"));
        assertTrue(s.contains("Cards left: 9"));
    }

    @Test
    void testSetNegativeCardsRemaining() {
        team1.setCardsRemaining(-5);
        assertEquals(-5, team1.getCardsRemaining()); // aktuell erlaubt, evtl. ändern
    }

    @Test
    void testToStringAfterWin() {
        team1.setCardsRemaining(0);
        String output = team1.toString();
        assertTrue(output.contains("RED"));
        assertTrue(output.contains("Cards left: 0"));
    }

}
