package Payload;

public class Request {
    private String type;
    private Payload payload;

    public Request(String type, Payload payload) {
        this.type = type;
        this.payload = payload;
    }

    //Getter und Setter
    public String getType() {
        return type;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public void setType(String type) {
        this.type = type;
    }
}
