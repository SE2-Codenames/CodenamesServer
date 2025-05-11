package Payload;

public class PayloadMoves implements Payload {
    private String cardId;
    private String player;

    public PayloadMoves(String cardId, String player) {
        this.cardId = cardId;
        this.player = player;
    }

    @Override
    public boolean valid() {
        return cardId != null && !cardId.isEmpty() && player != null && !player.isEmpty();
    }

    //Getter und Setter

    public String getCardId() {
        return cardId;
    }

    public String getPlayer() {
        return player;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setPlayer(String player) {
        this.player = player;
    }
}
