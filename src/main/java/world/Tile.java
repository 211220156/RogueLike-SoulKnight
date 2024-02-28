package world;

import asciiPanel.AsciiPanel;
import java.awt.Color;
import java.io.Serializable;

public enum Tile implements Serializable {

    FLOOR((char) 250, AsciiPanel.green),
    WALL((char) 219, AsciiPanel.brightCyan),
    GRASS((char) 61, AsciiPanel.black),//背景色
    WATER((char) 240, AsciiPanel.brightBlack),
    UPPER_LEFT((char) 218, AsciiPanel.brightBlack),
    UPPER_RIGHT((char) 191, AsciiPanel.brightBlack),
    LOWER_LEFT((char) 192, AsciiPanel.brightBlack),
    LOWER_RIGHT((char) 217, AsciiPanel.brightBlack),
    ROW((char) 220, AsciiPanel.brightBlack),
    COL((char) 222, AsciiPanel.brightBlack);

//    BOUNDS('x', AsciiPanel.magenta);

    private char glyph;

    public char glyph() {
        return glyph;
    }

    private Color color;

    public Color color() {
        return color;
    }

    public boolean isGrass() {
        return this == Tile.GRASS;
    }

    public boolean isGround() {
        return this == Tile.FLOOR;
    }
    public boolean isWater() {
        return this == Tile.WATER;
    }

    Tile(char glyph, Color color) {
        this.glyph = glyph;
        this.color = color;
    }
}
