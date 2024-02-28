package world;

import java.awt.*;

public class SnakeAI extends CreatureAI {

    private final int clientID;

    public SnakeAI(Creature creature, int client) {
        super(creature);
        this.clientID = client;
    }

    @Override
    public void onEnter(int x, int y, Tile tile) {
        //地图块锁的释放待完善
        if (tile.isGround() || tile.isWater() || tile.isGrass()) {
            synchronized (world.getObj(x, y)) {
                if (world.creature(x, y) == null) {
                    creature.setX(x);
                    creature.setY(y);
                }

            }
        }
    }

    @Override
    public void attack(Creature another) {
        another.modifyHP(another.defenseValue() - creature.attackValue());
        if (another.getType() == Creature.Type.BEAN) {
            this.grow();
        }
    }
    public Bullet.Direction getCurrentDirection() {
        int dx = creature.x() - creature.prevX(), dy = creature.y() - creature.prevY();
        if (dx > 0 && dy == 0) return Bullet.Direction.EAST;
        else if (dx < 0 && dy == 0) return Bullet.Direction.WEST;
        else if (dx == 0 && dy < 0) return Bullet.Direction.NORTH;
        else return Bullet.Direction.SOUTH;
    }
    @Override
    public void attack() {
        if (creature.mp() - 50 <= 0) {
            return;
        }
        Bullet.Direction d = getCurrentDirection();
        Bullet bullet = new Bullet(creature.x(), creature.y(), (char) 228,
                15, Color.GREEN, d, this.world, creature, 100);
        this.world.addBullet(bullet);
        switch (d) {
            case NORTH : {
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.NORTHEAST, this.world, creature, 100));
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.NORTHWEST, this.world, creature, 100));
                break;
            }
            case SOUTH : {
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.SOUTHEAST, this.world, creature, 100));
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.SOUTHWEST, this.world, creature, 100));
                break;
            }
            case EAST : {
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.NORTHEAST, this.world, creature, 100));
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.SOUTHEAST, this.world, creature, 100));
                break;
            }
            case WEST : {
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.NORTHWEST, this.world, creature, 100));
                this.world.addBullet(new Bullet(creature.x(), creature.y(), (char) 228,
                        15, Color.GREEN, Bullet.Direction.SOUTHWEST, this.world, creature, 100));
                break;
            }
        }

        creature.modifyMP(-50);

    }

    public void grow() {
        creature.modifyHP(5);
        creature.modifyMP(80);
    }

    @Override
    public void onUpdate() {
        creature.modifyMP(1);
    }

    @Override
    public void onNotify(String message) {

    }
}
