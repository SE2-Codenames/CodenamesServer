package model;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final List<Card> board;
    private final Team redTeam;
    private final Team blueTeam;
    private final List<Player> players;
    private TeamColor startingTeam; // Which team starts first (usually RED)
    private GameState state;

    public Game() {
        this.board = new ArrayList<>();
        this.redTeam = new Team(TeamColor.RED);
        this.blueTeam = new Team(TeamColor.BLUE);
        this.players = new ArrayList<>();
        this.state = GameState.LOBBY;
    }
}
