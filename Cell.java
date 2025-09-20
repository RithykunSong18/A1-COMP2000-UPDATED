import java.awt.*;

public class Cell extends Rectangle {
    public static final int SIZE = 35;

    public Cell(int x, int y) { super(x, y, SIZE, SIZE); }

    void paint(Graphics g) {
        g.setColor(new Color(255, 210, 225));  // base pink tile
        g.fillRect(x, y, SIZE, SIZE);
        g.setColor(new Color(240, 170, 190));
        g.drawRect(x, y, SIZE, SIZE);
    }
}