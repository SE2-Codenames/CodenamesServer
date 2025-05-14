package model.Player;

public class Player {
    private String username;
    private TeamColor teamColor;
    private boolean spymaster;

    public Player(String username) {
        this.username = username;
        this.teamColor = null;
        this.spymaster = false;
    }

    public String getUsername() {
        return username;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public boolean getSpymaster() {
        return spymaster;
    }

    public void setSpymaster(boolean spymaster) {
        this.spymaster = spymaster;
    }

    public String toInfoString() {
        return username + "," +
                (teamColor != null ? teamColor.name() : "") + "," +
                spymaster;
    }
}
