import model.Card.Card;
import model.Card.CardRole;
import TeamColor;
import model.Card.WordBank;
import model.Player.Player;
import model.Player.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {
    private Game game;
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
    public void testAddedPlayers() throws GameException {
        game.addPlayer(player1);
        assertEquals(1, game.getPlayers().size());
    }

    @Test
    public void testAssignTeam() throws GameException {
        game.addPlayer(player1);
        game.addPlayer(player2);
        game.assignTeamAndRole("Edi", TeamColor.RED, PlayerRole.SPYMASTER);
        game.assignTeamAndRole("Mihi", TeamColor.BLUE, PlayerRole.OPERATIVE);

        assertEquals(TeamColor.RED, player2.getTeamColor());
        assertEquals(TeamColor.BLUE, player1.getTeamColor());
        assertEquals(PlayerRole.SPYMASTER, player2.getPlayerRole());
        assertEquals(PlayerRole.OPERATIVE, player1.getPlayerRole());
    }

    @Test
    public void testStartGame() throws GameException {
        game.addPlayer(player1);
        game.addPlayer(player2);
        assertThrows(IllegalStateException.class, game::startGame);
    }

    @Test
    public void testStartGameNoSpymaster() throws GameException {
        player1.setTeamColor(TeamColor.BLUE);
        player2.setTeamColor(TeamColor.RED);


        player1.setPlayerRole(PlayerRole.OPERATIVE);
        player2.setPlayerRole(PlayerRole.OPERATIVE);


        game.addPlayer(player1);
        game.addPlayer(player2);

        assertThrows(IllegalStateException.class, game::startGame);
    }

    @Test
    public void testFailBoard() throws GameException {
        player1.setTeamColor(TeamColor.BLUE);
        player2.setTeamColor(TeamColor.RED);
        player3.setTeamColor(TeamColor.BLUE);
        player4.setTeamColor(TeamColor.RED);

        player1.setPlayerRole(PlayerRole.SPYMASTER);
        player2.setPlayerRole(PlayerRole.SPYMASTER);
        player3.setPlayerRole(PlayerRole.OPERATIVE);
        player4.setPlayerRole(PlayerRole.OPERATIVE);

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);
        game.addPlayer(player4);

        String board = game.getBoardState(null).get(0).getWord();
        assertThrows(GameException.class, () -> game.giveClue(board,2));
    }

    @Test
    public void testCorrectCardsRemaining() throws GameException {
        player1.setTeamColor(TeamColor.RED);
        player2.setTeamColor(TeamColor.BLUE);
        player3.setTeamColor(TeamColor.RED);
        player4.setTeamColor(TeamColor.BLUE);

        player1.setPlayerRole(PlayerRole.SPYMASTER);
        player2.setPlayerRole(PlayerRole.SPYMASTER);
        player3.setPlayerRole(PlayerRole.OPERATIVE);
        player4.setPlayerRole(PlayerRole.OPERATIVE);

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);
        game.addPlayer(player4);

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

        player1.setPlayerRole(PlayerRole.SPYMASTER);
        player2.setPlayerRole(PlayerRole.SPYMASTER);
        player3.setPlayerRole(PlayerRole.OPERATIVE);
        player4.setPlayerRole(PlayerRole.OPERATIVE);

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);
        game.addPlayer(player4);

        game.startGame();
        game.giveClue("Apfel",2);

        List<Card> visibleCards = game.getBoardState(player3);
        boolean assassinFound = false;
        for (Card c : visibleCards) {
            if (!c.isRevealed()) {
                try {
                    game.guessCard(c.getWord(), player3);
                } catch (GameException e) {
                    assassinFound = true;
                    break;
                }
            }
        }

        assertTrue(assassinFound);
    }

    @Test
    public void testReset() throws GameException {
        player1.setTeamColor(TeamColor.BLUE);
        player2.setTeamColor(TeamColor.RED);
        player3.setTeamColor(TeamColor.BLUE);
        player4.setTeamColor(TeamColor.RED);

        player1.setPlayerRole(PlayerRole.SPYMASTER);
        player2.setPlayerRole(PlayerRole.SPYMASTER);
        player3.setPlayerRole(PlayerRole.OPERATIVE);
        player4.setPlayerRole(PlayerRole.OPERATIVE);

        game.addPlayer(player1);
        game.addPlayer(player2);
        game.addPlayer(player3);
        game.addPlayer(player4);

        game.startGame();
        game.resetGame();

        List<Player> players = game.getPlayers();
        assertTrue(players.stream().noneMatch(Player::isReady));
    }
}
