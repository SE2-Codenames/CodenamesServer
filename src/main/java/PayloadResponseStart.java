import java.util.List;

public class PayloadResponseStart implements PayloadResponses{
    private String gameId;
    private List<String> players;

    public PayloadResponseStart(String gameId, List<String> players) {
        this.gameId = gameId;
        this.players = players;
    }

    public String getGameId() {
        return gameId;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
