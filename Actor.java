import java.awt.*;

public abstract class Actor {
    protected Color color = Color.BLACK;
    protected Cell loc;

    protected int bites = 0;     // 0 normal, 1 slowed, 2 dead
    protected boolean alive = true;

    public Cell location() { return loc; }
    public boolean isAlive() { return alive; }
    public int biteCount() { return bites; }
    public void bitten() { if (alive && ++bites >= 2) alive = false; }
    public int swimDelayTicks() { return (bites >= 1) ? 1 : 0; }

    protected void stepTo(Grid grid, Cell next) {
        if (next == null) return;
        if (grid.isBlockedFor(this, next)) return;
        loc = next;
    }

    // default icon
    public void paint(Graphics g) {
        g.setColor(alive ? color : Color.GRAY);
        g.fillOval(loc.x + 6, loc.y + 6, Cell.SIZE - 12, Cell.SIZE - 12);
        g.setColor(Color.BLACK);
        g.drawOval(loc.x + 6, loc.y + 6, Cell.SIZE - 12, Cell.SIZE - 12);
    }
}