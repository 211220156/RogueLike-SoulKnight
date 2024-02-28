package world;

import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MonsterAI extends CreatureAI implements Runnable {
    public MonsterAI(Creature creature) {
        super(creature);
    }
    @Override
    public void onEnter(int x, int y, Tile tile) {
        if (!tile.isGround()) return;
        int curX = creature.x(), curY = creature.y();
        synchronized (world.getObj(x, y)) {
            if (world.creature(x, y) == null) {
                creature.setX(x);
                creature.setY(y);
            }
        }
    }
    @Override
    public void onUpdate() {
    }
    @Override
    public void attack(Creature another) {
        another.modifyHP(another.defenseValue() - creature.attackValue());
//        System.out.println("Monster attack " + another.getType());
    }
    @Override
    public void onNotify(String string) {

    }
    private static final int ACTION_ATTACK = 0;
    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    @Override
    public void attack() {
        for (Bullet.Direction d : Bullet.Direction.values()) {
            if (creature.mp() > 0) {
                Bullet bullet = new Bullet(creature.x(), creature.y(), (char) 255, 15, Color.orange, d, this.world, creature, 200);
                this.world.addBullet(bullet);
                creature.modifyMP(-2);
            }
        }
    }
    private static Random random = new Random();
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (creature.hp() < 1) {
                    timer.cancel();
                }
                int action = chooseAction();
                if (action == ACTION_ATTACK) {
                    attack();
                }
                else  {
                    int dir = random.nextInt(4);
                    creature.moveBy(DIRS[dir][0], DIRS[dir][1]);
                }

                creature.modifyMP(5);
            }
        }, 0, 500);
    }
    private int chooseAction() {
        return random.nextInt(50);
    }
}
