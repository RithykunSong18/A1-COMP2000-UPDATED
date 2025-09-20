import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bird extends Actor implements Updatable, Swimmable, Predator {
    private final Random rng = new Random();

    // behaviour knobs (unchanged)
    private int alertRange = 5;
    private int speedSteps = 3;
    private int roamSteps  = 2;
    private int biteCooldownTicks = 6;
    private int hideMin = 40, hideMax = 80;
    private int visibleMin = 50, visibleMax = 90;

    // dynamic state
    private boolean hidden = true;
    private int stateTimer;
    private int cooldown = 0;
    private int stepClock = 0;
    private int roamEvery = 2;
    private Cell roamTarget = null;

    public Bird(Cell start) {
        this.loc = start;
        this.color = new Color(186, 85, 211); // keep the purple "predator" look
        this.stateTimer = randBetween(hideMin, hideMax);
    }

    @Override public void hunt(Stage s) { }
    @Override public void swim(Grid g) { }

    @Override
    public void update(Stage s) {
        if (!alive || s.gameOver) return;

        if (cooldown > 0) { cooldown--; return; }

        // hidden/visible machine
        if (hidden) {
            Actor swimmer = nearestSwimmer(s);
            if (swimmer != null) {
                hidden = false;
                stateTimer = randBetween(visibleMin, visibleMax);
            } else if (--stateTimer <= 0) {
                stateTimer = randBetween(hideMin, hideMax);
            }
            return;
        } else {
            if (--stateTimer <= 0) {
                hidden = true;
                stateTimer = randBetween(hideMin, hideMax);
                return;
            }
        }

        if (++stepClock < roamEvery) return;
        stepClock = 0;

        Actor swimmer = nearestSwimmer(s);
        if (swimmer != null) {
            for (int i = 0; i < speedSteps; i++) {
                if (loc == swimmer.location()) break;
                Cell next = PathFind.nextStepBFS(s.grid, this, loc, swimmer.location());
                if (next instanceof RiverCell) stepTo(s.grid, next);
            }
        } else {
            if (roamTarget == null || roamTarget == loc || !(roamTarget instanceof RiverCell)) {
                roamTarget = randomRiverCell(s);
            }
            if (roamTarget != null) {
                for (int i = 0; i < roamSteps; i++) {
                    if (loc == roamTarget) break;
                    Cell next = PathFind.nextStepBFS(s.grid, this, loc, roamTarget);
                    if (next instanceof RiverCell) stepTo(s.grid, next);
                }
            }
        }

        biteIfSameRiverCell(s, s.cat);
        biteIfSameRiverCell(s, s.dog);
    }

    private Actor nearestSwimmer(Stage s) {
        Actor best = null; int bestD = Integer.MAX_VALUE;
        if (s.cat.isAlive() && s.cat.location() instanceof RiverCell) {
            int d = s.grid.manhattan(loc, s.cat.location());
            if (d <= alertRange && d < bestD) { bestD = d; best = s.cat; }
        }
        if (s.dog.isAlive() && s.dog.location() instanceof RiverCell) {
            int d = s.grid.manhattan(loc, s.dog.location());
            if (d <= alertRange && d < bestD) { bestD = d; best = s.dog; }
        }
        return best;
    }

    private Cell randomRiverCell(Stage s) {
        List<Cell> river = new ArrayList<>();
        for (int c = 0; c < Grid.COLS; c++)
            for (int r = 0; r < Grid.ROWS; r++)
                if (s.grid.cells[c][r] instanceof RiverCell) river.add(s.grid.cells[c][r]);
        return river.isEmpty() ? null : river.get(rng.nextInt(river.size()));
    }

    private void biteIfSameRiverCell(Stage s, Actor a) {
        if (!a.isAlive()) return;
        if (this.loc == a.location() && (a.location() instanceof RiverCell)) {
            a.bitten();
            cooldown = biteCooldownTicks;
            hidden = true;
            stateTimer = randBetween(hideMin, hideMax);
            for (Cell nb : s.grid.neighbors(a.location())) {
                if (!(nb instanceof RiverCell) && !s.grid.isBlockedFor(a, nb)) { a.loc = nb; break; }
            }
            if (!a.isAlive() && a == s.cat && !s.gameOver) {
                s.gameOver = true;
                s.gameMessage = "Bird (river ambusher) wins! Cat eaten twice.";
            }
        }
    }

    private int randBetween(int lo, int hi) {
        return lo + rng.nextInt(Math.max(1, hi - lo + 1));
    }

    @Override
    public void paint(Graphics g) {
        if (hidden) return; // invisible while hidden

        g.setColor(alive ? new Color(186,85,211) : Color.GRAY);
        g.fillOval(loc.x + 5, loc.y + 10, Cell.SIZE - 10, Cell.SIZE - 20);
        g.setColor(new Color(140, 60, 170));
        int[] tx = { loc.x + 8, loc.x + 2, loc.x + 8 };
        int[] ty = { loc.y + 12, loc.y + Cell.SIZE/2, loc.y + Cell.SIZE - 12 };
        g.fillPolygon(tx, ty, 3); // tail/fin
        g.setColor(Color.WHITE);
        g.fillOval(loc.x + Cell.SIZE - 18, loc.y + Cell.SIZE/2 - 4, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(loc.x + Cell.SIZE - 15, loc.y + Cell.SIZE/2 - 1, 3, 3);
    }
}
