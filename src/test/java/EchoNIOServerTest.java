import org.junit.Test;
import screen.GameScreen;
import static org.junit.Assert.*;
public class EchoNIOServerTest {
    @Test
    public void testNewServer() {
        new Thread(()-> new EchoNIOServer("localhost", 9093)).start();
    }
    @Test
    public void testSerializeObject() {
        GameScreen gameScreen = new GameScreen(1);
        EchoNIOServer.serializeObject(gameScreen);
    }
    @Test
    public void testMain() {
        new Thread(()-> EchoNIOServer.main(null));
    }
}
