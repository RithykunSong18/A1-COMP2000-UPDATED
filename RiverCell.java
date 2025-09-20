import java.awt.*;

public class RiverCell extends Cell implements Obstacle {
    public RiverCell(int x, int y) { super(x, y); }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(173, 216, 230));
        g.fillRect(x, y, SIZE, SIZE);
        g.setColor(new Color(135, 206, 250));
        g.fillRect(x + 4, y + 4, SIZE - 8, SIZE - 8);
        g.setColor(new Color(240, 170, 190));
        g.drawRect(x, y, SIZE, SIZE);
    }

    @Override public boolean isPassableFor(Actor a) { return (a instanceof Swimmable); }
}