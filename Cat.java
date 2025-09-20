/**
 * Cat – player or AI-controlled prey.
 *
 * Week 5 base: simple drawable Actor (Cat) on a grid.
 * MOD from Week 5:
 *  - Player control via setDirection(dx, dy) when Cat is chosen.
 *  - AI when Dog is chosen: flee if Dog is close, else BFS to nearest coin; wander if no target.
 *  - Swim "viscosity" delay when entering river tiles.
 *  - Unstuck logic: if we fail to move for several ticks, pick a random reachable land cell and BFS to it.
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Cat extends Actor implements Updatable, Prey, Swimmable {
    // --- player control ---
    private int moveDx = 0, moveDy = 0;
    public void setDirection(int dx, int dy) { moveDx = dx; moveDy = dy; }

    // --- AI knobs ---
    private final Random rng = new Random();
    private int aiMoveDelay = 2;      // pacing for AI steps
    private int aiCooldown = 0;
    private int threatRange = 5;      // flee when Dog is this close
    private int swimTick = 0;         // river viscosity counter

    // --- unstuck detection ---
    private int stuckTicks = 0;
    private static final int MAX_STUCK = 6;

    public Cat(Cell start) {
        this.loc = start;
        // Make Cat clearly different from Dog (blue tone)
        this.color = new Color(60, 140, 255);
    }

    @Override
    public void update(Stage s) {
        if (!alive) return;

        if ("Cat".equals(s.chosenCharacter)) {
            // === Player-controlled Cat ===
            if (moveDx != 0 || moveDy != 0) {
                attemptStep(s, moveDx, moveDy);
                moveDx = 0; moveDy = 0;
            }
        } else {
            // === AI Cat ===
            if (aiCooldown > 0) { aiCooldown--; return; }

            Cell next = null;

            // 1) Flee Dog if close
            int distDog = s.dog.isAlive() ? s.grid.manhattan(loc, s.dog.location()) : Integer.MAX_VALUE;
            if (s.dog.isAlive() && distDog <= threatRange) {
                int bestD = distDog;
                java.util.List<Cell> ns = s.grid.neighbors(loc);
                Collections.shuffle(ns, rng);
                for (Cell c : ns) {
                    if (s.grid.isBlockedFor(this, c)) continue;
                    int d = s.grid.manhattan(c, s.dog.location());
                    if (d > bestD) { bestD = d; next = c; }
                }
            }

            // 2) Seek nearest coin
            if (next == null) {
                Cell targetCoin = nearestCoinCell(s);
                if (targetCoin != null) {
                    next = PathFind.nextStepBFS(s.grid, this, loc, targetCoin);
                }
            }

            // 3) Wander if nothing else
            if (next == null) {
                java.util.List<Cell> ns = s.grid.neighbors(loc);
                Collections.shuffle(ns, rng);
                for (Cell c : ns) {
                    if (!s.grid.isBlockedFor(this, c)) { next = c; break; }
                }
            }

            // 4) Move (with river slowdown)
            if (next != null) {
                if (next instanceof RiverCell) {
                    int delay = swimDelayTicks();
                    swimTick = (swimTick + 1) % (delay + 1);
                    if (swimTick != 0) { aiCooldown = aiMoveDelay; return; }
                }
                Cell before = loc;
                stepTo(s.grid, next);
                postMoveUnstuckLogic(s, before);
                aiCooldown = aiMoveDelay;
            }
        }
    }

    /** Try to step by dx/dy respecting obstacles + river slowdown (player control path). */
    private void attemptStep(Stage s, int dx, int dy) {
        int col = (loc.x - Grid.OFFSET) / Cell.SIZE + dx;
        int row = (loc.y - Grid.OFFSET) / Cell.SIZE + dy;
        if (col < 0 || col >= Grid.COLS || row < 0 || row >= Grid.ROWS) return;
        Cell next = s.grid.cellAtColRow(col, row);

        if (next instanceof RiverCell) {
            int delay = swimDelayTicks();
            swimTick = (swimTick + 1) % (delay + 1);
            if (swimTick != 0) return;
        }
        Cell before = loc;
        stepTo(s.grid, next);
        postMoveUnstuckLogic(s, before);

        // If Dog catches Cat after we move
        if (s.dog.isAlive() && s.dog.location() == loc) {
            alive = false;
            s.gameOver = true;
            s.gameMessage = "Dog wins! (caught the cat)";
        }
    }

    /** Find the nearest coin by Manhattan distance. */
    private Cell nearestCoinCell(Stage s) {
        int bestD = Integer.MAX_VALUE;
        Cell best = null;
        for (Stage.Coin coin : s.coins) {
            int d = s.grid.manhattan(loc, coin.at);
            if (d < bestD) { bestD = d; best = coin.at; }
        }
        return best;
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

    @Override
    public void paint(Graphics g) {
        // body
        g.setColor(alive ? color : Color.GRAY);
        g.fillOval(loc.x + 3, loc.y + 3, loc.width - 6, loc.height - 6);

        // ears
        g.setColor(alive ? color.darker() : Color.GRAY);
        g.fillOval(loc.x + 8, loc.y, 10, 10);
        g.fillOval(loc.x + loc.width - 18, loc.y, 10, 10);

        // eyes
        g.setColor(Color.WHITE);
        g.fillOval(loc.x + 12, loc.y + 15, 5, 5);
        g.fillOval(loc.x + loc.width - 17, loc.y + 15, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval(loc.x + 14, loc.y + 17, 2, 2);
        g.fillOval(loc.x + loc.width - 15, loc.y + 17, 2, 2);
    }

    @Override public void evade(Stage s) { }
    @Override public void swim(Grid g) { }
}