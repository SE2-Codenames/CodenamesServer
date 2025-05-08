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

    public boolean getSpymaster(){ return spymaster;}

    public String getUsername() { return username; }

    public void setPlayerRole(boolean spymaster) { this.spymaster = spymaster;}
}
