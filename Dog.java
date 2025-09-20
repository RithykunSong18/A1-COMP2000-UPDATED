/**
 * Dog – hunter or player-controlled character.
 *
 * Week 5 base: simple drawable Actor (Dog) on a grid.
 * MOD from Week 5:
 *  - Player control via setDirection(dx, dy) when Dog is chosen.
 *  - AI when Cat is chosen: BFS hunt when close; otherwise wander.
 *  - Unstuck escape when not progressing.
 *  - Swim slowdown on river tiles; catching Cat ends the game.
 *  - Visual differentiation: rich brown body + lighter muzzle patch.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Dog extends Actor implements Predator, Updatable, Swimmable {
    // --- AI pacing ---
    private int moveCooldown = 0;
    public int moveDelay = 2;
    private int sightRange = 7;
    private final Random rng = new Random();

    // --- player control ---
    private int moveDx = 0, moveDy = 0;
    public void setDirection(int dx, int dy) { moveDx = dx; moveDy = dy; }

    // --- swim slowdown ---
    private int swimTick = 0;

    // --- unstuck detection ---
    private int stuckTicks = 0;
    private static final int MAX_STUCK = 6;

    public Dog(Cell start) {
        this.loc = start;
        this.color = new Color(139, 69, 19); // rich brown
    }

    @Override
    public void update(Stage s) {
        if (!alive) return;

        if ("Dog".equals(s.chosenCharacter)) {
            // === Player-controlled Dog ===
            if (moveDx != 0 || moveDy != 0) {
                int col = (loc.x - Grid.OFFSET) / Cell.SIZE + moveDx;
                int row = (loc.y - Grid.OFFSET) / Cell.SIZE + moveDy;
                if (col >= 0 && col < Grid.COLS && row >= 0 && row < Grid.ROWS) {
                    Cell next = s.grid.cellAtColRow(col, row);
                    if (next instanceof RiverCell) {
                        int delay = swimDelayTicks();
                        swimTick = (swimTick + 1) % (delay + 1);
                        if (swimTick != 0) { moveDx = 0; moveDy = 0; return; }
                    }
                    Cell before = loc;
                    stepTo(s.grid, next);
                    postMoveUnstuckLogic(s, before);
                }
                moveDx = 0; moveDy = 0;
            }
        } else {
            // === AI Dog ===
            hunt(s);
        }

        // Win check after any move
        if (s.cat.isAlive() && this.loc == s.cat.location()) {
            s.cat.bitten();
            s.cat.alive = false;
            s.gameOver = true;
            s.gameMessage = "Dog wins! (caught the cat)";
        }
    }

    @Override
    public void hunt(Stage s) {
        if (!alive || !s.cat.isAlive()) return;
        if (moveCooldown > 0) { moveCooldown--; return; }

        Cell next = null;

        // Chase when close using BFS; otherwise wander
        int dist = s.grid.manhattan(loc, s.cat.location());
        if (dist <= sightRange) {
            next = PathFind.nextStepBFS(s.grid, this, loc, s.cat.location());
        } else {
            java.util.List<Cell> ns = s.grid.neighbors(loc);
            Collections.shuffle(ns, rng);
            for (Cell c : ns) {
                if (!s.grid.isBlockedFor(this, c)) { next = c; break; }
            }
        }

        if (next == null) { moveCooldown = moveDelay; return; }

        if (next instanceof RiverCell) {
            int delay = swimDelayTicks();
            swimTick = (swimTick + 1) % (delay + 1);
            if (swimTick != 0) { moveCooldown = moveDelay; return; }
        }

        Cell before = loc;
        stepTo(s.grid, next);
        postMoveUnstuckLogic(s, before);
        moveCooldown = moveDelay;

        // catch check (also done in update)
        if (s.cat.isAlive() && this.loc == s.cat.location()) {
            s.cat.bitten();
            s.cat.alive = false;
            s.gameOver = true;
            s.gameMessage = "Dog wins! (caught the cat)";
        }
    }

    /** If we didn't move for several ticks, pick a random reachable land target and BFS to it. */
    private void postMoveUnstuckLogic(Stage s, Cell before) {
        if (loc == before) {
            stuckTicks++;
            if (stuckTicks >= MAX_STUCK) {
                Cell escape = randomReachableLandCell(s);
                if (escape != null) {
                    Cell step = PathFind.nextStepBFS(s.grid, this, loc, escape);
                    if (step != null && step != loc && !s.grid.isBlockedFor(this, step)) {
                        stepTo(s.grid, step);
                    }
                }
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }
    }

    /** Pick a random land (non-river) cell that this actor can enter. */
    private Cell randomReachableLandCell(Stage s) {
        java.util.List<Cell> options = new ArrayList<>();
        for (int c = 0; c < Grid.COLS; c++) {
            for (int r = 0; r < Grid.ROWS; r++) {
                Cell cell = s.grid.cells[c][r];
                if (cell instanceof RiverCell) continue;
                if (!s.grid.isBlockedFor(this, cell)) options.add(cell);
            }
        }
        return options.isEmpty() ? null : options.get(rng.nextInt(options.size()));
    }

    @Override public void swim(Grid g) { }

    @Override
    public void paint(Graphics g) {
        // body
        g.setColor(alive ? color : Color.GRAY);
        g.fillOval(loc.x + 3, loc.y + 3, loc.width - 6, loc.height - 6);

        // lighter muzzle patch to differentiate
        g.setColor(new Color(205, 133, 63));
        g.fillOval(loc.x + loc.width/2 - 6, loc.y + loc.height/2 - 6, 12, 12);

        // eyes
        g.setColor(Color.WHITE);
        g.fillOval(loc.x + 10, loc.y + 14, 6, 6);
        g.fillOval(loc.x + loc.width - 16, loc.y + 14, 6, 6);
        g.setColor(Color.BLACK);
        g.fillOval(loc.x + 12, loc.y + 16, 3, 3);
        g.fillOval(loc.x + loc.width - 14, loc.y + 16, 3, 3);

        // ears
        g.setColor(new Color(101, 67, 33));
        g.fillOval(loc.x + 4, loc.y, 10, 10);
        g.fillOval(loc.x + loc.width - 14, loc.y, 10, 10);
    }
}