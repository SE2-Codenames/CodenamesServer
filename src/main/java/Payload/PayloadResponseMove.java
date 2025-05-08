package Payload;

public class PayloadResponseMove implements PayloadResponses{
    private String gameId;
    private boolean success;
    private String mess;

    public PayloadResponseMove(String gameId, boolean success, String mess) {
        this.gameId = gameId;
        this.success = success;
        this.mess = mess;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMess() {
        return mess;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }
}
