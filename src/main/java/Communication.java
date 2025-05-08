import model.Card.Card;
import model.GameState;
import model.Player.TeamColor;

import java.util.List;

public class Communication {

    //get Information from the Client
    //_______________________________
    //Spymaster give a hint and the number of hints
    public String[] getHint(){
        String[] hint = {"hint", "0"};
        return hint;
    }

    //Operater give a Clue     (-1 ENDTURN)
    public int selectCard(){
        return 0;
    }

    //gamestart after lobby
    public boolean gameStart(){
        return false;
    }


    //give Information to the Client
    //______________________________

    //give Gamestate, TeamState, Cardlist and Score
    public void giveGame(GameState gameState, TeamColor teamColor, List<Card> cards, int[] score){
    }

}
