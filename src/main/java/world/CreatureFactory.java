package world;

import java.io.Serializable;
import java.util.List;

import asciiPanel.AsciiPanel;

public class CreatureFactory implements Serializable {

    private World world;

    public CreatureFactory(World world) {
        this.world = world;
    }


    public Creature newSnake(int client) {
        Creature snake = new Creature(this.world, (char) 1, AsciiPanel.brightWhite, 100, 500,20, 5, 12, 1);
        world.addAtEmptyLocation(snake);
        new SnakeAI(snake, client);
        return snake;
    }

    public Creature newBean() {
        Creature bean = new Creature(this.world, (char) 3, AsciiPanel.green, 10, 200, 0, 0, 0, 0);
        world.addAtEmptyLocation(bean);
        new BeanAI(bean, this);
        return bean;
    }

    public Creature newMonster() {
        Creature monster = new Creature(this.world, (char) 2, AsciiPanel.brightMagenta, 50, 200,20, 5, 12, 2);
        MonsterAI ai = new MonsterAI(monster);
        world.addAtEmptyLocation(monster);
        world.executor.execute(ai);
        return monster;
    }
}
