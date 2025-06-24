package server;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public static void main(String[] args) {
        int port = 8081; // oder frei wählbar
        ServerImpl server = new ServerImpl(port);
        server.start();
        logger.info(String.format("Server läuft auf Port %d", port));
    }
}