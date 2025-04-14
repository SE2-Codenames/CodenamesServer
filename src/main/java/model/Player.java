package model;

public class Player {
    private String username;
    private TeamColor teamColor;
    private PlayerRole role;
    private boolean isReady;

    public Player(String username) {
        this.username = username;
        this.teamColor = null;
        this.role = PlayerRole.NONE;
        this.isReady = false;
    }
    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;
    }
}
