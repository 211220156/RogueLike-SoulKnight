package screen;
import java.awt.*;
import java.awt.event.KeyEvent;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import org.junit.Test;

import static org.junit.Assert.*;
public class GameSceneTest {
    @Test
    public void testGetMode() {
        GameScreen gameScreen = new GameScreen(0);
        assertEquals(gameScreen.getMode(), 0);
    }
    @Test
    public void testCreateSnake() {
        GameScreen gameScreen = new GameScreen(0);
        assertTrue(gameScreen.createSnake(0));
    }
    @Test
    public void testRemoveSnake() {
        GameScreen gameScreen = new GameScreen(0);
        gameScreen.createSnake(0);
        assertTrue(gameScreen.removeSnake(0));
    }
    @Test
    public void testDisplayOutput() {
        GameScreen gameScreen = new GameScreen(1);
        AsciiPanel panel = new AsciiPanel(30, 20, AsciiFont.Gold_plated_32x32);
        gameScreen.displayOutput(panel, -1);
        gameScreen.createSnake(0);
        gameScreen.displayOutput(panel, -1);
    }
    @Test
    public void testRespondToUserInput() {
        GameScreen gameScreen = new GameScreen(1);
        gameScreen.createSnake(0);
        // VK_LEFT
        KeyEvent left = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);
        gameScreen.respondToUserInput(left, 0);
        // VK_RIGHT
        KeyEvent right = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);
        gameScreen.respondToUserInput(right, 0);
        // VK_UP
        KeyEvent up = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
        gameScreen.respondToUserInput(up, 0);
        // VK_DOWN
        KeyEvent down = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        gameScreen.respondToUserInput(down, 0);
        // VK_SPACE
        KeyEvent space = new KeyEvent(new Component() {
        }, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        gameScreen.respondToUserInput(space, 0);
    }
}
