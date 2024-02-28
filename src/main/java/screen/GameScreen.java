package screen;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import asciiPanel.AsciiPanel;
import world.*;

public class GameScreen implements Screen, AutoCloseable {

    public static final int WORLD_WIDTH = 40;
    public static final int WORLD_HEIGHT = 40;

    public static final int SCREEN_WIDTH = 30;
    public static final int SCREEN_HEIGHT = 20;
    private final ConcurrentHashMap<Integer, Creature> snakes;

    private World world;
    private final CreatureFactory creatureFactory;
    public transient ScheduledExecutorService executor;
    private int mode;//单人模式0，服务器模式1
    @Override
    public void close() {
        executor.shutdown();
    }

    public GameScreen(int mode) {
        super();

        this.mode = mode;
        createWorld();
        this.snakes = new ConcurrentHashMap<>();
        this.executor = Executors.newScheduledThreadPool(2);

        creatureFactory = new CreatureFactory(this.world);
        createCreatures(creatureFactory);


    }
    public int getMode() {
        return this.mode;
    }

    public void recoverFromArchive() {
        this.executor = Executors.newScheduledThreadPool(2);
        this.world.executor = Executors.newCachedThreadPool();
        this.world.initializeLockTile();
        setTimerTask();
        this.world.recoverMonster();
        this.world.recoverBullet();
    }

    public boolean createSnake(int client) {
        Creature snake = creatureFactory.newSnake(client);
        this.snakes.put(client, snake);
        return this.snakes.containsKey(client);
    }
    public boolean removeSnake(int clientID) {
        world.remove(this.snakes.get(clientID));
        this.snakes.remove(clientID);
        return !this.snakes.containsKey(clientID);
    }

    private void createCreatures(CreatureFactory creatureFactory) {
        //多人对战时，新增snake相当于新增玩家线程。增加一个list放玩家snake，新增玩家需要单独出来

        for (int i = 0; i < 20; i++)
            creatureFactory.newBean();

        setTimerTask();
    }
    private void setTimerTask() {
        TimerTask createBean = new TimerTask() {
            @Override
            public void run() {
                creatureFactory.newBean();
            }
        };
        TimerTask createMonster = new TimerTask() {
            @Override
            public void run() {
                creatureFactory.newMonster();
            }
        };

        executor.scheduleAtFixedRate(createBean, 5, 5, TimeUnit.SECONDS);

        executor.scheduleAtFixedRate(createMonster, 5, 5, TimeUnit.SECONDS);
    }

    private void createWorld() {
        world = new WorldBuilder(WORLD_WIDTH, WORLD_HEIGHT).build();
    }

    @Override
    public void displayOutput(AsciiPanel terminal, int client) {
        if (snakes.isEmpty()) {
            String message = "Waiting for Player ...";
            terminal.write(message, 0, 10);
            return;
        }
        Creature snake = null;
        if (client == -1) {//服务器显示
            for (Map.Entry<Integer, Creature> entry : snakes.entrySet()) {
                client = entry.getKey();
                snake = entry.getValue();
                if (snake != null) {
                    break;
                }
            }
        }
        else {
            snake = snakes.get(client);
        }
        displayTiles(terminal, getScrollX(client), getScrollY(client));
//         Stats
         String stats = String.format("%3d/%3d hp", snake.hp(), snake.maxHP());
         String mp = String.format("%3d/%3d mp", snake.mp(), snake.maxMP());
         terminal.write(stats, 0, 18);
         terminal.write(mp, 0, 19);
    }

    private void displayTiles(AsciiPanel terminal, int left, int top) {
        // Show terrain
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                int wx = x + left;
                int wy = y + top;

                terminal.write(world.glyph(wx, wy), x, y, world.color(wx, wy));
            }
        }
        // Show creatures
        world.creatureLock.lock();
        try {
            for (Creature creature : world.getCreatures()) {
                if (creature.x() >= left && creature.x() < left + SCREEN_WIDTH && creature.y() >= top
                        && creature.y() < top + SCREEN_HEIGHT) {//生物在屏幕内
                    if (!world.tile(creature.x(), creature.y()).isGrass())
                        terminal.write(creature.glyph(), creature.x() - left, creature.y() - top, creature.color());
                }
            }
        } finally {
            world.creatureLock.unlock();
        }

        world.bulletLock.lock();
        try {
            for (Bullet bullet : world.getBullets()) {
                if (bullet.x() >= left && bullet.x() < left + SCREEN_WIDTH && bullet.y() >= top
                        && bullet.y() < top + SCREEN_HEIGHT) {
                    terminal.write(bullet.glyph(), bullet.x() - left, bullet.y() - top, bullet.color());
                }
            }
        } finally {
            world.bulletLock.unlock();
        }
        // Creatures can choose their next action now
        world.update();
    }



    public int getScrollX(int client) {//很牛逼！屏幕显示范围相对于0，0的偏移量，始终保证蛇在屏幕中心
        Creature snake = snakes.get(client);
        return Math.max(0, Math.min(snake.x() - SCREEN_WIDTH / 2, world.width() - SCREEN_WIDTH));
    }

    public int getScrollY(int client) {
        Creature snake = snakes.get(client);
        return Math.max(0, Math.min(snake.y() - SCREEN_HEIGHT / 2, world.height() - SCREEN_HEIGHT));
    }

    @Override
    public int respondToUserInput(KeyEvent key, int client) {
        Creature snake = snakes.get(client);

        switch (key.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                snake.moveBy(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                snake.moveBy(1, 0);
                break;
            case KeyEvent.VK_UP:
                snake.moveBy(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                snake.moveBy(0, 1);
                break;
            case KeyEvent.VK_SPACE:
                snake.attack();
                break;
        }
        if (snake.hp() < 1) {
            this.snakes.remove(client);
            return 2;
        }
        return 1;
    }
}
