package world;

import java.awt.Color;
import java.io.Serializable;

public class Creature implements Serializable {

    public static final int MOVE_UP = 1;
    public static final int MOVE_DOWN = 2;
    public static final int MOVE_LEFT = 3;
    public static final int MOVE_RIGHT = 4;

    private World world;
    public World getWorld() { return world; }

    private int x, prevX;

    public void setX(int x) {
        this.prevX = this.x;
        this.x = x;
    }

    public int x() {
        return x;
    }

    public int prevX() {
        return prevX;
    }

    private int y, prevY;

    public void setY(int y) {
        this.prevY = this.y;
        this.y = y;
    }

    public int y() {
        return y;
    }

    public int prevY() {
        return prevY;
    }

    private char glyph;

    public char glyph() {
        return this.glyph;
    }

    private Color color;

    public Color color() {
        return this.color;
    }

    private CreatureAI ai;

    public void setAI(CreatureAI ai) {
        this.ai = ai;
    }

    public CreatureAI getAI(){
        return this.ai;
    }

    private int maxHP;
    private int maxMP;

    public int maxHP() {
        return this.maxHP;
    }
    public int maxMP() { return this.maxMP; }

    private int hp;

    public int hp() {
        return this.hp;
    }
    private int mp;
    public int mp() { return this.mp; }

    public synchronized void modifyHP(int amount) {
        this.hp = Math.min(hp + amount, maxHP);
        if (this.hp < 1) {
            world.remove(this);
        }
    }
    public synchronized void modifyMP(int amount) {
        this.mp = Math.min(mp + amount, maxMP);
        this.mp = Math.max(mp, 0);
    }

    public void attack() {
        this.ai.attack();
    }

    private int attackValue;

    public int attackValue() {
        return this.attackValue;
    }

    private int defenseValue;

    public int defenseValue() {
        return this.defenseValue;
    }

    private int visionRadius;

    public int visionRadius() {
        return this.visionRadius;
    }
    public enum Type  {
        BEAN,
        SNAKE,
        MONSTER
    }
    private Type type;
    public Type getType() {
        return this.type;
    }

    public Tile tile(int wx, int wy) {
        return world.tile(wx, wy);
    }

    public void moveBy(int mx, int my) {
        Creature other = world.creature(x + mx, y + my);

        if (other == null) {
            ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));
        } else {
            ai.attack(other);
            if (other.getType() == Type.BEAN)
                ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));

        }
    }

    public void update() {
        try {
            this.ai.onUpdate();
        } catch (NullPointerException e) {
            if (this.type == Type.BEAN) System.out.println("bean exception");
            if (this.type == Type.SNAKE) System.out.println("snake exception");
            if (this.type == Type.MONSTER) System.out.println("monster exception");
        }
    }

    public boolean canEnter(int x, int y) {
        if (!world.tile(x, y).isGround()) return false;
        if (world.creature(x, y) != null) return false;
//        if (!world.isEmpty(x, y)) return false;
        return true;
    }

    public void notify(String message, Object... params) {
        ai.onNotify(String.format(message, params));
    }

    public Creature(World world, char glyph, Color color, int maxHP, int maxMP, int attack, int defense, int visionRadius, int type) {
        this.world = world;
        this.glyph = glyph;
        this.color = color;
        this.maxHP = maxHP;
        this.hp = maxHP;
        this.mp = maxMP;
        this.maxMP = maxMP;
        this.attackValue = attack;
        this.defenseValue = defense;
        this.visionRadius = visionRadius;
        if (type == 0) this.type = Type.BEAN;
        else if (type == 1) this.type = Type.SNAKE;
        else if (type == 2) this.type = Type.MONSTER;
    }
}
