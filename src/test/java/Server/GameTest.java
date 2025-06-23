package Server;

import model.Card.Card;
import model.Card.CardRole;
import model.Card.WordBank;
import model.GameState;
import model.Player.TeamColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameTest {
    private Game game;
    private WordBank words;
    private List<String> fixedWords;


    @BeforeEach
    void setUp() {
        fixedWords = new ArrayList<>();
        for(char i = 1; i<= 25; i++) {
            fixedWords.add("Word " + i);
        }
        words = mock(WordBank.class);
        when(words.getRandomWords(25)).thenReturn(fixedWords);
        game = new Game(words);
    }

    @Test
    void testSize(){
        assertEquals(25, game.getBoard().size());
    }

    @Test
    void testBoardWords(){
        List<Card> board = game.getBoard();
        for(Card card : board){
            assertTrue(fixedWords.contains(card.getWord()));
        }
    }

    @Test
    void testAssassingCard(){
        long card = game.getBoard().stream()
                .filter(c ->c.getCardRole() == CardRole.ASSASSIN)
                .count();
        assertEquals(1, card);
    }

    @Test
    void testRedBlueCards() {
        long red = game.getBoard().stream().filter(c -> c.getCardRole() == CardRole.RED).count();
        long blue = game.getBoard().stream().filter(c -> c.getCardRole() == CardRole.BLUE).count();

        assertTrue((red == 9 && blue == 8) || (red == 8 && blue == 9));
    }

    @Test
    void testRevealCard() throws GameException {
        Card unrevealed = game.getBoard().stream().filter(c -> !c.isRevealed()).findFirst().orElseThrow();
        int index = game.getBoard().indexOf(unrevealed);

        game.guessCard(index);
        assertTrue(unrevealed.isRevealed());
    }

    @Test
    void testGuessedRevealedCard() throws GameException {
        Card unrevealed = game.getBoard().stream().filter(c -> !c.isRevealed()).findFirst().orElseThrow();
        int index = game.getBoard().indexOf(unrevealed);

        game.guessCard(index);
        assertTrue(unrevealed.isRevealed());
        assertThrows(GameException.class, () -> game.guessCard(index));

    }

    @Test
    void testRevealAssassin() throws GameException {

        Card assassin = game.getBoard().stream()
                .filter(c ->c.getCardRole() == CardRole.ASSASSIN)
                .findFirst().orElseThrow();
        int assassinFound = game.getBoard().indexOf(assassin);
        game.guessCard(assassinFound);
        assertTrue(assassin.isRevealed());
        assertEquals(GameState.GAME_OVER, game.getGamestate());
    }

    @Test
    void testNeutral() throws GameException {
        Card neutral = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.NEUTRAL)
                .findFirst().orElseThrow();
        int neutralFound = game.getBoard().indexOf(neutral);
        game.guessCard(neutralFound);
        assertTrue(neutralFound >= 0);

    }

    @Test
    void testScore() throws GameException {
        Card red = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.RED && !c.isRevealed())
                .findFirst().orElseThrow();
        game.guessCard(game.getBoard().indexOf(red));
        assertTrue(red.isRevealed());
    }

    @Test
    void testCheckScoreGameOverTriggers() throws Exception {
        int[] losingScore = {-1, 0};
        game.setScore(losingScore);

        Method checkScore = Game.class.getDeclaredMethod("checkScore");
        checkScore.setAccessible(true);
        checkScore.invoke(game);

        assertEquals(GameState.GAME_OVER, game.getGamestate());
        assertEquals(TeamColor.BLUE, game.getCurrentTeam());
    }

    @Test
    void testCheckScoreTriggersWin() throws Exception {
        List<Card> blueCards = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.BLUE)
                .toList();

        for (Card c : blueCards) {
            Field revealedField = Card.class.getDeclaredField("revealed");
            revealedField.setAccessible(true);
            revealedField.set(c, true);
        }

        Method checkScore = Game.class.getDeclaredMethod("checkScore");
        checkScore.setAccessible(true);
        checkScore.invoke(game);

        assertEquals(GameState.GAME_OVER, game.getGamestate());
    }

    @Test
    void testUpdateScoresCounts() throws Exception {
        Method updateScores = Game.class.getDeclaredMethod("updateScores", CardRole.class);
        updateScores.setAccessible(true);

        Field redField = Game.class.getDeclaredField("revealedRed");
        Field blueField = Game.class.getDeclaredField("revealedBlue");
        redField.setAccessible(true);
        blueField.setAccessible(true);
        redField.set(game, 0);
        blueField.set(game, 0);

        updateScores.invoke(game, CardRole.RED);
        updateScores.invoke(game, CardRole.BLUE);

        assertEquals(1, redField.get(game));
        assertEquals(1, blueField.get(game));
    }

    @Test
    void testResetScoreAllZero() throws Exception {
        Field redField = Game.class.getDeclaredField("revealedRed");
        Field blueField = Game.class.getDeclaredField("revealedBlue");
        redField.setAccessible(true);
        blueField.setAccessible(true);
        redField.set(game, 3);
        blueField.set(game, 2);

        int[] newScore = {5, 6};
        game.setScore(newScore);

        Method resetScore = Game.class.getDeclaredMethod("resetScore");
        resetScore.setAccessible(true);
        resetScore.invoke(game);

        assertArrayEquals(new int[]{0, 0}, game.getScore());
        assertEquals(0, redField.get(game));
        assertEquals(0, blueField.get(game));
    }


    @Test
    void testClue() {
        game.getClue(new String[]{"Banana", "2"});
        assertEquals("Banana", game.getHint());
        assertEquals(2, game.getRemainingGuesses());
    }

    @Test
    void testClueIncorrect() {
        assertThrows(NumberFormatException.class, () -> game.getClue(new String[]{"Apfel", "mm"}));
    }

    @Test
    void testEndTurnManyGuesses() throws Exception {
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
    void testNotifyWin() throws Exception {
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

    @Test
    void testToggleMark() {
        // Anfangszustand
        boolean[] marked = game.getMarkedCards();
        assertFalse(marked[5]);

        // Markieren
        game.toggleMark(5);
        assertTrue(marked[5]);

        // Nochmals aufrufen → wieder unmarkiert
        game.toggleMark(5);
        assertFalse(marked[5]);
    }

    @Test
    void testToggleMarkOutOfBounds() {
        // Sollte keine Exception werfen
        game.toggleMark(-1);
        game.toggleMark(25);  // Index 25 ist außerhalb (0–24 erlaubt)

        // Sicherstellen, dass Array unverändert ist
        for (boolean mark : game.getMarkedCards()) {
            assertFalse(mark);
        }
    }

    @Test
    void testClearMarks() {
        // Einige Karten markieren
        game.toggleMark(0);
        game.toggleMark(10);
        game.toggleMark(24);

        assertTrue(game.getMarkedCards()[0]);
        assertTrue(game.getMarkedCards()[10]);
        assertTrue(game.getMarkedCards()[24]);

        // Jetzt alle löschen
        game.clearMarks();

        for (boolean mark : game.getMarkedCards()) {
            assertFalse(mark);
        }
    }

    @Test
    void testCheckExposeMatchesBoardWord() {
        String wordOnBoard = game.getBoard().get(0).getWord();
        game.getClue(new String[]{wordOnBoard, "1"});
        assertTrue(game.checkExpose());
    }

    @Test
    void testCheckExposeNoMatch() {
        game.getClue(new String[]{"NichtAufDemBrett", "1"});
        assertFalse(game.checkExpose());
    }

    @Test
    void testAddTeamCard() {
        boolean changed = game.addTeamCard(TeamColor.RED);
        assertTrue(changed);

        long neutralCount = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.NEUTRAL)
                .count();

        assertTrue(neutralCount <= 6); // Start: 7 → danach max. 6
    }

    @Test
    void testAddTeamCardNoNeutralLeft() {
        // Alle neutralen Karten in revealed Zustand setzen (unveränderlich)
        game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.NEUTRAL)
                .forEach(c -> {
                    try {
                        Field field = Card.class.getDeclaredField("revealed");
                        field.setAccessible(true);
                        field.set(c, true);
                    } catch (Exception e) {
                        fail(e);
                    }
                });

        boolean changed = game.addTeamCard(TeamColor.BLUE);
        assertFalse(changed);
    }

    @Test
    void testCheckAssassinBeforeReveal() {
        assertFalse(game.checkAssassin());
    }

    @Test
    void testCheckAssassinAfterReveal() throws GameException {
        Card assassin = game.getBoard().stream()
                .filter(c -> c.getCardRole() == CardRole.ASSASSIN)
                .findFirst()
                .orElseThrow();

        game.guessCard(game.getBoard().indexOf(assassin));
        assertTrue(game.checkAssassin());
    }

    @Test
    void testEndTurnSwitchesTeam() throws Exception {
        Field stateField = Game.class.getDeclaredField("state");
        stateField.setAccessible(true);
        stateField.set(game, GameState.OPERATIVE_TURN);

        TeamColor original = game.getCurrentTeam();
        game.endTurn();

        assertNotEquals(original, game.getCurrentTeam());
        assertEquals(GameState.SPYMASTER_TURN, game.getGamestate());
    }

    @Test
    void testContainsInvalidCharacters() {
        assertTrue(game.containsInvalidCharacters("Test1"));     // enthält Zahlen
        assertTrue(game.containsInvalidCharacters("Test!"));       // enthält Sonderzeichen
        assertTrue(game.containsInvalidCharacters("Test Test"));    // enthält Leerzeichen
        assertTrue(game.containsInvalidCharacters("TÄST"));        // enthält Umlaut
        assertTrue(game.containsInvalidCharacters("Test_test"));    // enthält Unterstrich
        assertTrue(game.containsInvalidCharacters("Test "));

        assertFalse(game.containsInvalidCharacters("Test"));
        assertFalse(game.containsInvalidCharacters("testText"));
    }
}
