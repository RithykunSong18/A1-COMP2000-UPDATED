import java.awt.*;
import java.util.Random;

public class Grid {
    public static final int COLS = 20, ROWS = 20, OFFSET = 10;
    public final Cell[][] cells = new Cell[COLS][ROWS];

    public Grid() {
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                cells[c][r] = new Cell(OFFSET + c * Cell.SIZE, OFFSET + r * Cell.SIZE);

        // meandering river (3 tiles wide)
        Random rng = new Random();
        int baseCol = 7 + rng.nextInt(3);     // 7..9
        int riverWidth = 3;
        for (int r = 0; r < ROWS; r++) {
            for (int w = 0; w < riverWidth; w++) {
                int c = clamp(baseCol + w, 0, COLS - 1);
                cells[c][r] = new RiverCell(cells[c][r].x, cells[c][r].y);
            }
            int shift = rng.nextInt(3) - 1;
            baseCol = clamp(baseCol + shift, 1, COLS - riverWidth - 1);
        }

        // random trees on land
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                if (!(cells[c][r] instanceof RiverCell) && rng.nextDouble() < 0.12)
                    cells[c][r] = new TreeCell(cells[c][r].x, cells[c][r].y);
    }

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    public void paint(Graphics g) {
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                cells[c][r].paint(g);
    }

    public Cell cellAtColRow(int c, int r) { return cells[c][r]; }

    public boolean isBlockedFor(Actor a, Cell cell) {
        if (cell instanceof Obstacle) return !((Obstacle)cell).isPassableFor(a);
        return false;
    }

    public java.util.List<Cell> neighbors(Cell c) {
        java.util.List<Cell> res = new java.util.ArrayList<>(4);
        int col = (c.x - OFFSET) / Cell.SIZE;
        int row = (c.y - OFFSET) / Cell.SIZE;
        int[][] d = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] v : d) {
            int nc = col + v[0], nr = row + v[1];
            if (nc >= 0 && nc < COLS && nr >= 0 && nr < ROWS) res.add(cells[nc][nr]);
        }
        return res;
    }

    public int manhattan(Cell a, Cell b) {
        int ac = (a.x - OFFSET) / Cell.SIZE;
        int ar = (a.y - OFFSET) / Cell.SIZE;
        int bc = (b.x - OFFSET) / Cell.SIZE;
        int br = (b.y - OFFSET) / Cell.SIZE;
        return Math.abs(ac - bc) + Math.abs(ar - br);
    }

    public Cell firstRiverCellOrFallback() {
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                if (cells[c][r] instanceof RiverCell) return cells[c][r];
        return cells[COLS/2][ROWS/2];
    }
}