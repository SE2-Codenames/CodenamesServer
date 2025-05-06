package model;

public class Player {
    private String username;
    private TeamColor teamColor;
    private PlayerRole playerRole;
    private boolean isReady;

    public Player(String username) {
        this.username = username;
        this.teamColor = null;
        this.playerRole = PlayerRole.NONE;
        this.isReady = false;
    }

    //Compares usernames case-insensitively. exp.: "Jake" and "jake" are treated equally
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return username.equalsIgnoreCase(player.username);
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public PlayerRole getPlayerRole(){ return playerRole;}

    public String getUsername() { return username; }

    public void setPlayerRole(PlayerRole playerRole) { this.playerRole = playerRole;}

    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { isReady = ready; }
}
