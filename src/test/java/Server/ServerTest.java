package Server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void testServerStartsAndStops() throws Exception {
        int port = 8082;
        ServerImpl server = new ServerImpl(port);

        assertDoesNotThrow(server::start);

        server.stop(1000);
    }
}
