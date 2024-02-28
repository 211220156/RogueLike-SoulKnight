import org.junit.Test;
import screen.GameScreen;

import java.io.IOException;

import static org.junit.Assert.*;
public class ClientTest {
    @Test
    public void testMain() {
        Client.main(null);
    }
    @Test
    public void testClient() {
        new Thread(()->{
            Client client = new Client();
            try {
                client.startClient();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @Test
    public void testDeserialize() {
        byte[] data = EchoNIOServer.serializeObject(new GameScreen(1));
        Client.deserializeScreenObject(data);
        byte[] id = EchoNIOServer.serializeObject(0);
        assertEquals(Client.deserializeID(id), 0);
    }

}
