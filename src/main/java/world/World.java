package world;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class World implements AutoCloseable, Serializable {//自动关闭线程池

    private Tile[][] tiles;
    private transient Object[][] lockTile;
    private int width;
    private int height;
    private List<Creature> creatures;
    private List<Bullet> bullets;
    public Lock creatureLock = new ReentrantLock();
    public Lock bulletLock = new ReentrantLock();
    public transient ExecutorService executor = Executors.newCachedThreadPool();
    @Override
    public void close() {
        executor.shutdown();
    }


    public World(Tile[][] tiles) {
        this.tiles = tiles;
        this.width = tiles.length;
        this.height = tiles[0].length;
        this.creatures = new ArrayList<>();
        this.bullets = new ArrayList<>();
        initializeLockTile();
    }
    public void initializeLockTile() {
        this.lockTile = new Object[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                lockTile[i][j] = new Object();
            }
        }
    }

    public Tile tile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Tile.WALL;
        } else {
            return tiles[x][y];
        }
    }
    public void addBullet(Bullet bullet) {
        bulletLock.lock();
        try {
            this.bullets.add(bullet);
            executor.execute(bullet);
        } finally {
            bulletLock.unlock();
        }
    }
    public List<Bullet> getBullets() {
        return this.bullets;
    }
    public Object getObj(int x, int y) { return lockTile[x][y]; }

    public char glyph(int x, int y) {
        return tiles[x][y].glyph();
    }

    public Color color(int x, int y) {
        return tiles[x][y].color();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void dig(int x, int y) {
        if (tile(x, y).isGrass()) {
            synchronized (tile(x, y)) {
                tiles[x][y] = Tile.FLOOR;
            }
        }
    }

    public void addAtEmptyLocation(Creature creature) {
        int x;
        int y;

        while (true) {
            do {
                x = (int) (Math.random() * this.width);
                y = (int) (Math.random() * this.height);
            } while (!tile(x, y).isGround() || this.creature(x, y) != null);
            synchronized (tile(x, y)) {
                if (creature(x, y) == null) {
                    creature.setX(x);
                    creature.setY(y);
                    break;
                }
            }
        }

        creatureLock.lock();
        try {
            this.creatures.add(creature);
        } finally {
            creatureLock.unlock();
        }
    }

    public Creature creature(int x, int y) {
        creatureLock.lock();
        try {
            for (Creature c : this.creatures) {
                if (c.x() == x && c.y() == y) {
                    return c;
                }
            }
            return null;
        } finally {
            creatureLock.unlock();
        }
    }

    public List<Creature> getCreatures() {
        return this.creatures;
    }

    public void remove(Creature target) {
        creatureLock.lock();
        try {
            this.creatures.remove(target);
        } finally {
            creatureLock.unlock();
        }
    }
    public void remove(Bullet target) {
        bulletLock.lock();
        try {
            this.bullets.remove(target);
        } finally {
            bulletLock.unlock();
        }
    }

    public void update() {
        ArrayList<Creature> toUpdate;
        creatureLock.lock();
        try {
            toUpdate = new ArrayList<>(this.creatures);
        } finally {
            creatureLock.unlock();
        }


        for (Creature creature : toUpdate) {
            if (creature != null) creature.update();
        }
    }

    public void recoverMonster() {
        creatureLock.lock();
        try {
            for (Creature creature : getCreatures()) {
                if (creature.getType() == Creature.Type.MONSTER) {
                    executor.execute(creature.getAI());
                }
            }
        } finally {
            creatureLock.unlock();
        }
    }
    public void recoverBullet() {
        bulletLock.lock();
        try {
            for (Bullet bullet : getBullets()) {
                executor.execute(bullet);
            }
        } finally {
            bulletLock.unlock();
        }
    }
}
