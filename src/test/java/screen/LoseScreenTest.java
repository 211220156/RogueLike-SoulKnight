package screen;
import java.awt.*;
import java.awt.event.KeyEvent;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import org.junit.Test;

import static org.junit.Assert.*;
public class LoseScreenTest {
    @Test
    public void testDisplayOutput() {
        LoseScreen loseScreen = new LoseScreen();
        AsciiPanel panel = new AsciiPanel(30,30, AsciiFont.Gold_plated_32x32);
        loseScreen.displayOutput(panel, 0);
    }
    @Test
    public void testRespondToUserInput() {
        LoseScreen loseScreen = new LoseScreen();
        // test Enter
        KeyEvent e = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
        assertEquals(loseScreen.respondToUserInput(e, 0), 2);
    }
}
