package screen;
import java.awt.*;
import java.awt.event.KeyEvent;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import org.junit.Test;

import static org.junit.Assert.*;
public class StartScreenTest {
    @Test
    public void testDisplayOutput() {
        StartScreen st = new StartScreen();
        AsciiPanel panel = new AsciiPanel(30,30, AsciiFont.Gold_plated_32x32);
        st.displayOutput(panel, -2);
        st.displayOutput(panel, 0);
    }
    @Test
    public void testRespondToUserInput() {
        StartScreen st = new StartScreen();
        // test Enter
        KeyEvent e1 = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
        assertEquals(st.respondToUserInput(e1, 0), 1);
        // test other
        KeyEvent e2 = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        assertEquals(st.respondToUserInput(e2, 0), 0);
    }
}
