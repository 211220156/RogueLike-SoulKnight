package world;

import org.junit.Test;

import static org.junit.Assert.*;
public class WorldTest {
    @Test
    public void testTile() {
        World world = new WorldBuilder(40,40).build();
        assertSame(world.tile(-1, -1), Tile.WALL);
        assertSame(world.tile(0,0), Tile.UPPER_LEFT);
    }
    @Test
    public void testGet() {
        World world = new WorldBuilder(40,40).build();
        world.getBullets();
        world.getObj(0, 0);
        world.glyph(0, 0);
        world.color(0, 0);
        world.width();
        world.height();
        world.dig(0, 0);
        world.creature(0, 0);
        world.getCreatures();
        world.close();
    }
    @Test
    public void testModifyCreatures() {
        World world = new WorldBuilder(40,40).build();
        CreatureFactory creatureFactory = new CreatureFactory(world);
        creatureFactory.newBean();
        creatureFactory.newSnake(0);
        creatureFactory.newMonster();
        world.remove(creatureFactory.newBean());
        world.update();
        world.close();
    }
    @Test
    public void testAttack() {
        World world = new WorldBuilder(40,40).build();
        CreatureFactory creatureFactory = new CreatureFactory(world);
        creatureFactory.newSnake(0).attack();
        creatureFactory.newMonster().attack();
    }
}
