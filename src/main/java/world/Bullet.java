package world;

import java.awt.*;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class Bullet implements Runnable, Serializable {
    public enum Direction {
        NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST;
    }
    private int x;
    private int y;
    private World world;
    public int x() { return x; }
    public int y() { return y; }
    private int dx;
    private int dy;
    private char glyph;
    private Color color;
    private Creature creator;
    private int speed;
    public Bullet(int x, int y, char glyph, int attackValue, Color color, Direction d, World world, Creature creature, int speed) {
        this.x = x;
        this.y = y;
        this.glyph = glyph;
        this.attackValue = attackValue;
        this.color = color;
        this.setDirection(d);
        this.world = world;
        this.creator = creature;
        this.speed = speed;
    }
    public void setDirection(Direction d) {
        switch (d) {
            case NORTH -> {
                this.dx = 0;
                this.dy = -1;
            }
            case SOUTH -> {
                this.dx = 0;
                this.dy = 1;
            }
            case EAST -> {
                this.dx = 1;
                this.dy = 0;
            }
            case WEST -> {
                this.dx = -1;
                this.dy = 0;
            }
            case NORTHEAST -> {
                this.dx = 1;
                this.dy = -1;
            }
            case NORTHWEST -> {
                this.dx = -1;
                this.dy = -1;
            }
            case SOUTHEAST -> {
                this.dx = 1;
                this.dy = 1;
            }
            case SOUTHWEST -> {
                this.dx = -1;
                this.dy = 1;
            }
            default -> {}
        }
    }
    private final int attackValue;
    public int getAttackValue() { return attackValue; }
    public char glyph() {
        return this.glyph;
    }
    public Color color() {
        return this.color;
    }
    public void attack(Creature another) {
        another.modifyHP(another.defenseValue() - attackValue);
    }
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                x += dx;
                y += dy;
                if (!world.tile(x, y).isGround()) {
                    if (world.tile(x, y).isGrass()) world.dig(x, y);
                    timer.cancel();
                    world.remove(Bullet.this);
                }
                else {
                    Creature another = world.creature(x, y);
                    if (another != null && another != creator) {
                        attack(another);
                        timer.cancel();
                        world.remove(Bullet.this);
                    }
                }
            }
        }, 0, speed);
    }
}
