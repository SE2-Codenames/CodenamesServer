import Server.Game;
import Server.GameException;
import model.Card.Card;
import model.Card.CardRole;
import model.Card.WordBank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameTest {
    private Game game;
    private WordBank words;
    private List<String> fixedWords;


    @BeforeEach
    public void setUp() {
        fixedWords = new ArrayList<>();
        for(int i = 1; i<= 25; i++) {
            fixedWords.add("Word " + i);
        }
        words = mock(WordBank.class);
        when(words.getRandomWords(25)).thenReturn(fixedWords);
        game = new Game(words);
    }

    @Test
    public void testSize(){
        assertEquals(25, game.getBoard().size());
    }

    @Test
    public void testBoardWords(){
        List<Card> board = game.getBoard();
        for(Card card : board){
            assertTrue(fixedWords.contains(card.getWord()));
        }
    }

    @Test
    public void testAssassingCard() throws GameException {
        long card = game.getBoard().stream()
                .filter(c ->c.getCardRole() == CardRole.ASSASSIN)
                .count();
        assertEquals(1, card);
    }

    @Test
    public void testRedBlueCards() {
        long red = game.getBoard().stream().filter(c -> c.getCardRole() == CardRole.RED).count();
        long blue = game.getBoard().stream().filter(c -> c.getCardRole() == CardRole.BLUE).count();

        assertTrue((red == 9 && blue == 8) || (red == 8 && blue == 9));
    }

    @Test
    public void testRevealCard() throws GameException {
        Card unrevealed = game.getBoard().stream().filter(c -> !c.isRevealed()).findFirst().orElseThrow();
        int index = game.getBoard().indexOf(unrevealed);

        game.guessCard(index);
        assertTrue(unrevealed.isRevealed());
    }

    @Test
    public void testGuessedRevealedCard() throws GameException {
        Card unrevealed = game.getBoard().stream().filter(c -> !c.isRevealed()).findFirst().orElseThrow();
        int index = game.getBoard().indexOf(unrevealed);

        game.guessCard(index);
        assertTrue(unrevealed.isRevealed());
        assertThrows(GameException.class, () -> game.guessCard(index));

    }

    @Test
    public void testRevealAssassin() throws GameException {

        Card assassin = game.getBoard().stream()
                .filter(c ->c.getCardRole() == CardRole.ASSASSIN)
                .findFirst().orElseThrow();
        int assassinFound = game.getBoard().indexOf(assassin);
        game.guessCard(assassinFound);
        assertTrue(assassinFound >= 0);
        //Server.Game Over evtl. noch (GameState)
    }

    @Test
    public void testNeutral() throws GameException {
        Card neutral = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.NEUTRAL)
                .findFirst().orElseThrow();
        int neutralFound = game.getBoard().indexOf(neutral);
        game.guessCard(neutralFound);
        assertTrue(neutralFound >= 0);

    }

    @Test
    public void testScore() throws GameException {
        Card red = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.RED && !c.isRevealed())
                .findFirst().orElseThrow();
        game.guessCard(game.getBoard().indexOf(red));
        assertTrue(red.isRevealed());
    }

}
