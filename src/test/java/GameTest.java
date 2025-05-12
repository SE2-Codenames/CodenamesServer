import model.Card.Card;
import model.Card.CardRole;
import model.Card.WordBank;
import model.Player.Player;
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
    private Player player1, player2, player3, player4;
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
        player1 = new Player("Mihi");
        player2 = new Player("Edi");
        player3 = new Player("Steffi");
        player4 = new Player("Stefan");
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
    public void testRevealCard() throws GameException {
        List<Card> board = game.getBoard();
        int index = -1;
        for(int i = 0; i < board.size(); i++){
            if(!board.get(i).isRevealed()) {
                index = i;
                break;
            }
        }

        assertTrue(index >= 0);
        game.guessCard(index);
        assertTrue(board.get(index).isRevealed());
    }

    @Test
    public void testGuessRevealedCard() throws GameException {
        List<Card> board = game.getBoard();
        int index = -1;

        for(int i = 0; i < board.size(); i++){
            if(!board.get(i).isRevealed()) {
                index = i;
                break;
            }
        }

        assertTrue(index >= 0);
        game.guessCard(index);
        int finalIndex = index;
        assertThrows(GameException.class, () -> game.guessCard(finalIndex));

    }

    @Test
    public void testRevealAssasin() throws GameException {

        List<Card> board = game.getBoard();
        int assassinFound = -1;
        for (int i = 0; i < board.size(); i++) {
            if (board.get(i).getCardRole() == CardRole.ASSASSIN) {
                assassinFound = i;
                break;
            }
        }

        assertTrue(assassinFound >= 0);
        game.guessCard(assassinFound);
        List<Card> updated = game.getBoard();
        assertTrue(updated.get(assassinFound).isRevealed());
    }

}
