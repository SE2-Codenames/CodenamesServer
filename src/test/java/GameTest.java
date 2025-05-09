import model.Card.Card;
import model.Card.CardRole;
import model.Player.TeamColor;
import model.Card.WordBank;
import model.Player.Player;
import model.Player.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    /*private Game game;
    private WordBank words;
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;

    @BeforeEach
    public void setUp() {
        words = new WordBank();
        game = new Game(words);
        player1 = new Player("Mihi");
        player2 = new Player("Edi");
        player3 = new Player("Steffi");
        player4 = new Player("Stefan");
    }

    @Test
    public void testGameNotEnoughPlayers(){
        assertThrows(IllegalArgumentException.class, game::startGame);
    }

    @Test
    public void testStartGameNoSpymaster() throws GameException {
        player1.setTeamColor(TeamColor.BLUE);
        player1.setPlayerRole(false);
        player2.setTeamColor(TeamColor.RED);

        player2.setPlayerRole(false);


        game.getPlayers().add(player1);
        game.getPlayers().add(player2);

        assertThrows(IllegalStateException.class, game::startGame);
    }

    @Test
    public void testGiveClue() throws GameException {
        player1.setTeamColor(TeamColor.BLUE);
        player2.setTeamColor(TeamColor.RED);
        player3.setTeamColor(TeamColor.BLUE);
        player4.setTeamColor(TeamColor.RED);

        player1.setPlayerRole(true);
        player2.setPlayerRole(true);
        player3.setPlayerRole(false);
        player4.setPlayerRole(false);

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);
        game.getPlayers().add(player3);
        game.getPlayers().add(player4);

        game.startGame();

        String word = game.getBoardState(null).get(0).getWord();
        assertThrows(GameException.class, () -> game.giveClue(word,2));
    }

    @Test
    public void testCorrectCardsRemaining() throws GameException {
        player1.setTeamColor(TeamColor.RED);
        player2.setTeamColor(TeamColor.BLUE);
        player3.setTeamColor(TeamColor.RED);
        player4.setTeamColor(TeamColor.BLUE);

        player1.setPlayerRole(true);
        player2.setPlayerRole(true);
        player3.setPlayerRole(false);
        player4.setPlayerRole(false);

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);
        game.getPlayers().add(player3);
        game.getPlayers().add(player4);

        game.startGame();
        game.giveClue("Apfel",2);

        List<Card> board = game.getBoardState(player1);

        Card redCard = board.stream()
                .filter(c -> c.getCardRole() == CardRole.RED && !c.isRevealed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No unrevealed RED card found"));

        long before = board.stream()
                .filter(c -> c.getCardRole() == CardRole.RED && !c.isRevealed())
                .count();

        game.guessCard(redCard.getWord(), player3);

        long after = game.getBoardState(player1).stream()
                .filter(c -> c.getCardRole() == CardRole.RED && !c.isRevealed())
                .count();

        assertEquals(before, after);
    }

    @Test
    public void testChooseAssasin() throws GameException {
        player1.setTeamColor(TeamColor.RED);
        player2.setTeamColor(TeamColor.BLUE);
        player3.setTeamColor(TeamColor.RED);
        player4.setTeamColor(TeamColor.BLUE);

        player1.setPlayerRole(true);
        player2.setPlayerRole(true);
        player3.setPlayerRole(false);
        player4.setPlayerRole(false);

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);
        game.getPlayers().add(player3);
        game.getPlayers().add(player4);

        game.startGame();
        game.giveClue("Apfel",2);

        List<Card> visibleCards = game.getBoardState(player3);
        boolean assassinFound = false;
        for (Card c : visibleCards) {
            if (!c.isRevealed() && c.getCardRole() == CardRole.ASSASSIN) {
                game.guessCard(c.getWord(), player3);
                break;
            }
        }

        assertThrows(GameException.class, () -> game.giveClue("Apfel", 2));
    }

    @Test
    public void testReset() throws GameException {
        player1.setTeamColor(TeamColor.RED);
        player2.setTeamColor(TeamColor.BLUE);
        player3.setTeamColor(TeamColor.RED);
        player4.setTeamColor(TeamColor.BLUE);

        player1.setPlayerRole(true);
        player2.setPlayerRole(true);
        player3.setPlayerRole(false);
        player4.setPlayerRole(false);

        game.getPlayers().add(player1);
        game.getPlayers().add(player2);
        game.getPlayers().add(player3);
        game.getPlayers().add(player4);

        game.startGame();
        game.giveClue("Apfel",2);

        List<Card> visible = game.getBoardState(player3);
        Card guess = visible.stream()
                .filter(c -> !c.isRevealed())
                .findFirst()
                .orElseThrow();

        game.guessCard(guess.getWord(), player3);
        assertTrue(guess.isRevealed());

        game.resetGame();

        long revealed = game.getBoardState(player3).stream()
                .filter(Card::isRevealed)
                .count();

        assertEquals(0, revealed);
    }*/
}
