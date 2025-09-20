import java.awt.*;

public class TreeCell extends Cell implements Obstacle {
    public TreeCell(int x, int y) { super(x, y); }

    @Override
    public void paint(Graphics g) {
        // trunk
        g.setColor(new Color(139, 69, 19));
        g.fillRect(x + SIZE/3, y + SIZE/2, SIZE/3, SIZE/2);
        // leaves
        g.setColor(new Color(34, 139, 34));
        g.fillOval(x + 4, y, SIZE - 8, SIZE - 10);
        g.setColor(Color.GREEN);
        g.drawOval(x + 4, y, SIZE - 8, SIZE - 10);
    }

    @Override public boolean isPassableFor(Actor a) { return false; }
}