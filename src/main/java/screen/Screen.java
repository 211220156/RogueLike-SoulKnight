package screen;

import asciiPanel.AsciiPanel;
import java.awt.event.KeyEvent;
import java.io.Serializable;

public interface Screen extends Serializable {

    public void displayOutput(AsciiPanel terminal, int client);

    public int respondToUserInput(KeyEvent key, int client);
}
