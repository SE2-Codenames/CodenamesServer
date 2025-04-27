public class Response {
    private String stat;
    private String mess;
    private PayloadResponses data;

    public Response(String stat, String mess, PayloadResponses data) {
        this.stat = stat;
        this.mess = mess;
        this.data = data;
    }

    //Getter und Setter
    public String getStat() {
        return stat;
    }

    public String getMess() {
        return mess;
    }

    public PayloadResponses getData() {
        return data;
    }

    public void setData(PayloadResponses data) {
        this.data = data;
    }

    public void setMess(String mess) {
        this.mess = mess;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }
}
