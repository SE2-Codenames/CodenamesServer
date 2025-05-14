import model.Player.TeamColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserTest {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private List<UserManager> clients;
    private UserManager user;

    @BeforeEach
    public void setUp() throws Exception {
        socket = mock(Socket.class);
        out = mock(PrintWriter.class);
        in = mock(BufferedReader.class);
        clients = new ArrayList<>();
        user = new UserManager(socket, clients);


        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testPlayerInfo_Spymaster() {
        setPrivateField(user, "username", "Mihi");
        setPrivateField(user, "team", TeamColor.RED);
        setPrivateField(user, "isSpymaster", true);

        assertEquals("Mihi,RED,true", user.getPlayerInfo());
    }

    @Test
    public void testPlayerInfo_Operative() {
        setPrivateField(user, "username", "Mihi");
        setPrivateField(user, "team", TeamColor.BLUE);
        setPrivateField(user, "isSpymaster", false);
        assertEquals("Mihi,BLUE,false", user.getPlayerInfo());
    }

    @Test
    public void testSendMessage() throws IOException {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer, true);

        Socket socket2 = mock(Socket.class);
        when(socket2.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket2.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        UserManager user2 = new UserManager(socket2, clients);
        setPrivateField(user2, "out", printer);
        user2.sendMessage("Hallo!");
        assertTrue(writer.toString().contains("Hallo"));
    }


    private void setPrivateField(Object target, String name, Object object) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, object);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Setzen des Feldes: " + name, e);
        }
    }


}
