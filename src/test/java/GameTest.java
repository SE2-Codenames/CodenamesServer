import Server.Game;
import Server.GameException;
import model.Card.Card;
import model.Card.CardRole;
import model.Card.WordBank;
import model.GameState;
import model.Player.TeamColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        for(char i = 1; i<= 25; i++) {
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
        assertTrue(assassin.isRevealed());
        assertEquals(GameState.GAME_OVER, game.getGamestate());
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

    @Test
    public void testClue() {
        game.getClue(new String[]{"Banana", "2"});
        assertEquals("Banana", game.getHint());
        assertEquals(2, game.getRemainingGuesses());
    }

    @Test
    public void testClueIncorrect() {
        assertThrows(NumberFormatException.class, () -> game.getClue(new String[]{"Apfel", "mm"}));
    }

    @Test
    public void testValidateClueTrue() throws Exception {
        game.getClue(new String[]{"Apfel", "2"});
        Method method = Game.class.getDeclaredMethod("validateClue");
        method.setAccessible(true);
        String clue = (String) method.invoke(game);
        assertEquals("Apfel", clue);
    }

    @Test
    public void testValidateClueFalse() throws Exception {
        game.getClue(new String[]{"     ", "2"});
        Method method = Game.class.getDeclaredMethod("validateClue");
        method.setAccessible(true);

        try{
            method.invoke(game);
        } catch(InvocationTargetException e){
            Throwable cause = e.getCause();
            assertTrue(cause instanceof GameException);
            assertEquals("Clue cannot be empty", cause.getMessage());
        }
    }

    @Test
    public void testValidateClueIncorrect() throws Exception {
        game.getClue(new String[]{"Apfel33", "2"});
        Method method = Game.class.getDeclaredMethod("validateClue");
        method.setAccessible(true);

        try{
            method.invoke(game);
        } catch(InvocationTargetException e){
            Throwable cause = e.getCause();
            assertTrue(cause instanceof GameException);
            assertEquals("Clue cannot contain numbers", cause.getMessage());
        }
    }

    @Test
    public void testValidateClueBoard() throws Exception {
        String clue = game.getBoard().get(0).getWord();
        game.getClue(new String[]{clue, "2"});
        Method method = Game.class.getDeclaredMethod("validateClue");
        method.setAccessible(true);

        try{
            method.invoke(game);
        } catch(InvocationTargetException e){
            Throwable cause = e.getCause();
            assertTrue(cause instanceof GameException);
            assertEquals("Clue cannot be a word on the board", cause.getMessage());
        }
    }

    @Test
    public void testEndTurnManyGuesses() throws Exception {
        game.getClue(new String[]{"Apple", "0"});

        Field field = Game.class.getDeclaredField("state");
        field.setAccessible(true);
        field.set(game, GameState.OPERATIVE_TURN);

        Card card = game.getBoard().stream()
                .filter(c -> !c.isRevealed() && (
                        (game.getCurrentTeam() == TeamColor.RED && c.getCardRole() == CardRole.RED) ||
                                (game.getCurrentTeam() == TeamColor.BLUE && c.getCardRole() == CardRole.BLUE)))
                .findFirst()
                .orElseThrow();

        int index = game.getBoard().indexOf(card);
        game.guessCard(index);

        assertEquals(GameState.SPYMASTER_TURN, game.getGamestate());
    }

    @Test
    public void testNotifyWin() throws Exception {
        List<Card> redCards = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.RED)
                .toList();

        for (Card c : redCards) {
            Field field = Card.class.getDeclaredField("revealed");
            field.setAccessible(true);
            field.set(c, true);
        }

        Method checkScore = Game.class.getDeclaredMethod("checkScore");
        checkScore.setAccessible(true);
        checkScore.invoke(game);

        assertEquals(GameState.GAME_OVER, game.getGamestate());
    }

}
