import java.util.List;

public class PayloadStart implements Payload{

    private String gameId;
    private List<String> players;

    public PayloadStart(String gameId, List<String> players) {
        this.gameId = gameId;
        this.players = players;
    }

    @Override
    public boolean valid() {
        return gameId != null && !gameId.isEmpty() && players != null && !players.isEmpty();
    }

    //Getter und Setter

    public String getGameId() {
        return gameId;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
