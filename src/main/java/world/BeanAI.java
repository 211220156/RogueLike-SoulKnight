package world;

public class BeanAI extends CreatureAI{


    private CreatureFactory factory;
    private int spreadcount = 0;

    public static int spores = 3;
    public static double spreadchance = 0.001;

    public BeanAI(Creature creature, CreatureFactory factory) {
        super(creature);
        this.factory = factory;
    }

    public void onUpdate() {
        if (this.spreadcount < BeanAI.spores && Math.random() < BeanAI.spreadchance) {
            spread();
        }
    }

    private void spread() {
        this.factory.newBean();
        spreadcount++;
    }

    @Override
    public void onEnter(int x, int y, Tile tile) {
        throw new UnsupportedOperationException("Unimplemented method 'onEnter'");
    }

    @Override
    public void onNotify(String message) {
        throw new UnsupportedOperationException("Unimplemented method 'onNotify'");
    }

    @Override
    public void attack(Creature another) {
        throw new UnsupportedOperationException("Unimplemented method 'attack'");
    }
    @Override
    public void attack() {
        throw new UnsupportedOperationException("Unimplemented method 'attack'");
    }

}
