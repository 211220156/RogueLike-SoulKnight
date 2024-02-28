import org.junit.Test;

import java.awt.*;
import java.awt.event.KeyEvent;

public class ApplicationMainTest {
    @Test
    public void testLoadRecord() {
        new Thread(()->{
            ApplicationMain app = new ApplicationMain();
            KeyEvent e = new KeyEvent(new Component() {
            }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_R, 'r');
            app.keyPressed(e);
        }).start();
    }
    @Test
    public void testLoadArchive() {
        new Thread(()->{
            ApplicationMain app = new ApplicationMain();
            KeyEvent e = new KeyEvent(new Component() {
            }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_S, 's');
            app.keyPressed(e);
        }).start();
    }
    @Test
    public void testDeleteRecord() {
        new Thread(()->{
            ApplicationMain app = new ApplicationMain();
            KeyEvent e = new KeyEvent(new Component() {
            }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_D, 'd');
            app.keyPressed(e);
        }).start();

    }
    @Test
    public void testNewGame() {
        new Thread(() -> {
            ApplicationMain app = new ApplicationMain();
            KeyEvent e = new KeyEvent(new Component() {
            }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                    0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            app.keyPressed(e);
        }).start();

    }
    @Test
    public void testMain() {
        new Thread(()-> ApplicationMain.main(null)).start();
    }
}
