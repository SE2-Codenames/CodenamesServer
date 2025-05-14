package Server;

public class Server {
    public static void main(String[] args) {
        int port = 8081; // oder frei wählbar
        ServerImpl server = new ServerImpl(port);
        server.start();
        System.out.println("Server läuft auf Port " + port);
    }
}