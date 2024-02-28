package screen;

import asciiPanel.AsciiPanel;

import java.awt.*;
import java.awt.event.KeyEvent;

public class StartScreen implements Screen {

    @Override
    public void displayOutput(AsciiPanel terminal, int client) {
        terminal.write("Welcome to Soul Knight!", 0, 0, Color.orange);
        terminal.write("Press ENTER to start", 0, 4);

        //client -2表示是单人版，可以查看录像、进度恢复
        if (client == -2) {
            terminal.write("Press r to watch the reply", 0, 10);
            terminal.write("Press s to get the archive", 0, 12);
            terminal.write("Press d to delete old record", 0, 14);
        }
    }
    @Override
    public int respondToUserInput(KeyEvent key, int client) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                return 1;
            default:
                return 0;
        }
    }
}
