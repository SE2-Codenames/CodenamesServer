import model.Card;
import model.CardRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    private Card redTeamCard;
    private Card blueTeamCard;

    @BeforeEach
    public void setUp() {
        redTeamCard = new Card("Dog", CardRole.RED);
        blueTeamCard = new Card("Cat", CardRole.BLUE);
    }

    @Test
    public void constructorTestRed() {
        assertEquals("Dog", redTeamCard.getWord());
        assertEquals(CardRole.RED, redTeamCard.getCardRole());
        assertFalse(redTeamCard.isRevealed());
    }

    @Test
    public void constructorTestBlue() {
        assertEquals("Cat", blueTeamCard.getWord());
        assertEquals(CardRole.BLUE, blueTeamCard.getCardRole());
        assertFalse(blueTeamCard.isRevealed());
    }

    @Test
    public void isRevealedTestRed() {
        redTeamCard.reveal();
        assertTrue(redTeamCard.isRevealed());
    }

    @Test
    public void isRevealedTestBlue() {
        blueTeamCard.reveal();
        assertTrue(blueTeamCard.isRevealed());
    }

    @Test
    public void toStringTestRed() {
        assertEquals("Dog [RED]", redTeamCard.toString());
    }

    @Test
    public void toStringTestBlue() {
        assertEquals("Cat [BLUE]", blueTeamCard.toString());
    }

    @Test
    public void toStringTest2Red() {
        redTeamCard.reveal();
        assertEquals("Dog [RED] (revealed)", redTeamCard.toString());
    }

    @Test
    public void toStringTest2Blue() {
        blueTeamCard.reveal();
        assertEquals("Cat [BLUE] (revealed)", blueTeamCard.toString());
    }
}
