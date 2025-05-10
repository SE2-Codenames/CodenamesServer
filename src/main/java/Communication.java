import model.Card.Card;
import model.GameState;
import model.Player.TeamColor;

import java.io.PrintWriter;
import java.util.List;

public class Communication {

    private PrintWriter out;

    public Communication() {
        // empty constructor
    }

    public Communication(PrintWriter out) {
        this.out = out;
    }

    // get Information from the Client
    // _______________________________

    // Spymaster gives a hint and the number of hints
    public String[] getHint(String input) {
        if (input != null && input.startsWith("HINT:")) {
            String[] parts = input.substring(5).split(":");
            if (parts.length == 2) {
                return parts;
            }
        }
        return new String[]{"", "0"};
    }


    //Operater give a Clue     (-1 ENDTURN)
    public int selectCard(String input) {
        if (input != null && input.startsWith("SELECT:")) {
            try {
                return Integer.parseInt(input.substring(7));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    //gamestart after lobby
    public boolean gameStart(String input) {
        return input != null && input.trim().equalsIgnoreCase("START_GAME");
    }


    //give Information to the Client
    //______________________________

    //give Gamestate, TeamState, Cardlist and Score
    public void giveGame(GameState gameState, TeamColor teamColor, List<Card> cards, int[] score) {
        if (out == null) return;

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"gameState\":\"").append(gameState.name()).append("\",");
        json.append("\"teamRole\":\"").append(teamColor.name()).append("\",");
        json.append("\"card\":[");
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            json.append("{")
                    .append("\"word\":\"").append(card.getWord()).append("\",")
                    .append("\"role\":\"").append(card.getCardRole()).append("\",")
                    .append("\"isRevealed\":").append(card.isRevealed())
                    .append("}");
            if (i < cards.size() - 1) json.append(",");
        }
        json.append("],");
        json.append("\"score\":[").append(score[0]).append(",").append(score[1]).append("]");
        json.append("}");

        out.println("GAME_STATE:" + json.toString());
    }
}
