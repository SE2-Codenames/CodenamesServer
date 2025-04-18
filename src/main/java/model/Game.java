package model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Game {
    private final List<Card> board;
    private final Team redTeam;
    private final Team blueTeam;
    private final List<Player> players;
    private TeamColor startingTeam; // Which team starts first (usually RED)
    private GameState state;
    private final WordBank wordBank;
    private TeamColor currentTurn;

    public Game(WordBank wordBank) {
        this.redTeam = new Team(TeamColor.RED);
        this.blueTeam = new Team(TeamColor.BLUE);
        this.players = new ArrayList<>();
        this.state = GameState.LOBBY;
        this.wordBank = wordBank;

        this.board = createBoard(wordBank.getRandomWords(25));
    }

    private List<Card> createBoard(List<String> randomWords) {
        List<Card> board = new ArrayList<>();

        // 1. Assign card types (9 red, 8 blue, 7 neutral, 1 assassin)
        for (int i = 0; i < 25; i++) {
            CardRole cardType;
            if (i < 9) cardType = CardRole.RED;          // First 9 = Red Team
            else if (i < 17) cardType = CardRole.BLUE;    // Next 8 = Blue Team
            else if (i < 24) cardType = CardRole.NEUTRAL; // Next 7 = Neutral
            else cardType = CardRole.ASSASSIN;            // Last 1 = Assassin

            board.add(new Card(randomWords.get(i), cardType));
        }
        // 2. Shuffle to randomize positions
        Collections.shuffle(board);
        return board;
    }

    public void startGame() {
        if (players.size() < 4) {
            throw new IllegalStateException("Need at least 4 players (2 per team)");
        } else if (players.stream().filter(p -> p.getPlayerRole() == PlayerRole.SPYMASTER).count() < 2) {
            throw new IllegalStateException("Both teams need a spymaster");
        }
        // Assign starting team randomly if not set
        if (startingTeam == null) {
            startingTeam = TeamColor.RED;
        }
        currentTurn = startingTeam;

        state = GameState.SPYMASTER_TURN;
    }

    public void addPlayer(Player newPlayer) throws GameException {
        validatePlayerAdd(newPlayer);
        players.add(newPlayer);
    }

    private void validatePlayerAdd(Player newPlayer) throws GameException {
        // Check if username is taken
        if (players.stream().anyMatch(p -> p.equals(newPlayer))) {
            throw new GameException("Username '" + newPlayer.getUsername() + "' is already taken");
        }

        // Check if role is taken per team
        if (newPlayer.getTeamColor() != null && newPlayer.getPlayerRole() != PlayerRole.NONE) {
            boolean roleTaken = players.stream().filter(p -> p.getTeamColor() == newPlayer.getTeamColor()).anyMatch(p -> p.getPlayerRole() == newPlayer.getPlayerRole());

            if (roleTaken) {
                String message = String.format(
                        "%s role for %s team is already taken", // exp.: Spymaster for Red team is already taken
                        newPlayer.getPlayerRole(),
                        newPlayer.getTeamColor()
                );
                throw new GameException(message);
            }
        }
    }

    public void assignTeamAndRole(String username, TeamColor team, PlayerRole role) throws GameException {
        Player player = getPlayerByUsername(username).orElseThrow(() -> new GameException("Player not found"));

        // Check if role is already taken in this team
        boolean roleTaken = players.stream().filter(p -> p.getTeamColor() == team).anyMatch(p -> p.getPlayerRole() == role && !p.equals(player));
        if (roleTaken) {
            throw new GameException(role + " role for " + team + " team is already taken");
        }

        player.setTeamColor(team);
        player.setPlayerRole(role);
    }

    private Optional<Player> getPlayerByUsername(String username) {
        //Looks for player with username, compares it ignoring upper/lower case differences (equalsIgnoreCase)
        //If found: Returns the Player object wrapped in an Optional, If not found: Returns an empty Optional
        //It's a safe way to handle "might not exist" situations,Forces you to think about the case where the player isn't found
        //Better than returning null which can cause errors
        return players.stream().filter(p -> p.getUsername().equalsIgnoreCase(username)).findFirst();
    }
}


