package model.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    private Card redTeamCard;
    private Card blueTeamCard;

    @BeforeEach
    void setUp() {
        redTeamCard = new Card("Dog", CardRole.RED);
        blueTeamCard = new Card("Cat", CardRole.BLUE);
    }

    @Test
    void constructorTestRed() {
        assertEquals("Dog", redTeamCard.getWord());
        assertEquals(CardRole.RED, redTeamCard.getCardRole());
        assertFalse(redTeamCard.isRevealed());
    }

    @Test
    void constructorTestBlue() {
        assertEquals("Cat", blueTeamCard.getWord());
        assertEquals(CardRole.BLUE, blueTeamCard.getCardRole());
        assertFalse(blueTeamCard.isRevealed());
    }

    @Test
    void isRevealedTestRed() {
        redTeamCard.reveal();
        assertTrue(redTeamCard.isRevealed());
    }

    @Test
    void isRevealedTestBlue() {
        blueTeamCard.reveal();
        assertTrue(blueTeamCard.isRevealed());
    }

    @Test
    void toStringTestRed() {
        assertEquals("Dog [RED]", redTeamCard.toString());
    }

    @Test
    void toStringTestBlue() {
        assertEquals("Cat [BLUE]", blueTeamCard.toString());
    }

    @Test
    void toStringTest2Red() {
        redTeamCard.reveal();
        assertEquals("Dog [RED] (revealed)", redTeamCard.toString());
    }

    @Test
    void toStringTest2Blue() {
        blueTeamCard.reveal();
        assertEquals("Cat [BLUE] (revealed)", blueTeamCard.toString());
    }

    @Test
    void setCardRoleTest() {
        redTeamCard.setCardRole(CardRole.NEUTRAL);
        assertEquals(CardRole.NEUTRAL, redTeamCard.getCardRole());
    }

    @Test
    void constructorWithNullValues() {
        Card card = new Card(null, null);
        assertNull(card.getWord());
        assertNull(card.getCardRole());
        assertFalse(card.isRevealed());
    }


}
