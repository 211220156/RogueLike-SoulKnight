package screen;

import asciiPanel.AsciiPanel;

import java.awt.event.KeyEvent;

public class LoseScreen implements Screen {
    @Override
    public int respondToUserInput(KeyEvent key, int client) {
        switch (key.getKeyCode()) {
            default:
                return 2;
        }
    }

    @Override
    public void displayOutput(AsciiPanel terminal, int client) {
        terminal.write("You lost!", 0, 0);
        terminal.write("Come on next time!", 0, 4);
        terminal.write("Game video has been saved", 0, 8);
        terminal.write("Game progress saved", 0, 12);
    }

}
