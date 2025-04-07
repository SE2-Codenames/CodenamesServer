import java.io.IOException;

public class Server {
    public static void main(String[] args) {
        ServerImpl server = new ServerImpl();
        try {
            server.start(8081);
        } catch (IOException e) {
            System.out.println("Server konnte nicht gestartet werden.");
        }
    }
}
