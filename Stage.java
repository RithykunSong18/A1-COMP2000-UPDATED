import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stage {
    // timing / goals
    public static final int TICK_MS = 120;
    public static final int START_TIME_MS = 60_000;
    public static final int COIN_GOAL = 10;

    // start screen / selection
    boolean showStart = true;
    String chosenCharacter = ""; // "Cat" or "Dog"

    // world + actors
    Grid grid;
    Dog dog;
    Cat cat;
    Bird bird;
    List<Actor> actors = new ArrayList<>();
    List<Updatable> updatables = new ArrayList<>();

    // coins
    static class Coin { Cell at; Coin(Cell c){ at = c; } }
    List<Coin> coins = new ArrayList<>();
    int catCoins = 0;
    int dogCoins = 0;

    // state
    boolean paused = false;
    boolean gameOver = false;
    String gameMessage = "Race! First to collect 10 coins wins.";
    int score = 0;
    int highScore = 0;
    int timeLeftMs = START_TIME_MS;

    // input
    private int inDx = 0, inDy = 0;
    Random rng = new Random();

    public Stage() { }

    public void togglePause() { paused = !paused; }
    public void setInput(int dx, int dy) { inDx = dx; inDy = dy; }

    public void buildWorldWithChoice() {
        grid = new Grid();

        if ("Cat".equals(chosenCharacter)) {
            cat = new Cat(grid.cellAtColRow(15, 12));
            dog = new Dog(grid.cellAtColRow(2, 2));
        } else if ("Dog".equals(chosenCharacter)) {
            dog = new Dog(grid.cellAtColRow(15, 12));
            cat = new Cat(grid.cellAtColRow(2, 2));
        } else {
            cat = new Cat(grid.cellAtColRow(15, 12));
            dog = new Dog(grid.cellAtColRow(2, 2));
        }

        bird = new Bird(grid.firstRiverCellOrFallback());

        actors.clear(); updatables.clear(); coins.clear();
        actors.add(dog); actors.add(cat); actors.add(bird);
        updatables.add(dog); updatables.add(cat); updatables.add(bird);

        spawnCoins(22);
        catCoins = 0;
        dogCoins = 0;
        score = 0;
        timeLeftMs = START_TIME_MS;
        gameOver = false;
        gameMessage = "Race! First to collect 10 coins wins.";
    }

    public void resetGame() {
        if (score > highScore) highScore = score;
        showStart = true;
        chosenCharacter = "";
        actors.clear(); updatables.clear(); coins.clear();
        catCoins = dogCoins = 0;
        score = 0; timeLeftMs = START_TIME_MS;
        gameOver = false;
    }

    private void spawnCoins(int n) {
        int tries = 0;
        while (coins.size() < n && tries < 800) {
            tries++;
            int c = rng.nextInt(Grid.COLS), r = rng.nextInt(Grid.ROWS);
            Cell spot = grid.cellAtColRow(c, r);
            if (grid.isBlockedFor(cat, spot)) continue;
            boolean onActor = (spot == cat.location() || spot == dog.location() || spot == bird.location());
            if (onActor) continue;
            boolean dup = false;
            for (Coin co : coins) if (co.at == spot) { dup = true; break; }
            if (dup) continue;
            coins.add(new Coin(spot));
        }
    }

    public void tick() {
        if (showStart || gameOver) return;

        timeLeftMs -= TICK_MS;
        if (timeLeftMs < 0) timeLeftMs = 0;

        // send keyboard input to the chosen character
        if ("Cat".equals(chosenCharacter)) {
            cat.setDirection(inDx, inDy);
        } else if ("Dog".equals(chosenCharacter)) {
            dog.setDirection(inDx, inDy);
        }
        inDx = 0; inDy = 0;

        // updates
        for (Updatable u : updatables) u.update(this);

        // Cat collects coin
        Coin picked = null;
        for (Coin c : coins) {
            if (c.at == cat.location()) { picked = c; break; }
        }
        if (picked != null) {
            coins.remove(picked);
            catCoins++;
            score += 10;
        }

        // Dog collects coin
        Coin dogPick = null;
        for (Coin c : coins) {
            if (c.at == dog.location()) { dogPick = c; break; }
        }
        if (dogPick != null) {
            coins.remove(dogPick);
            dogCoins++;
        }

        // win / lose checks
        if (catCoins >= COIN_GOAL) {
            gameOver = true;
            gameMessage = "Cat wins! Collected 10 coins first.";
            if (score > highScore) highScore = score;
        } else if (dogCoins >= COIN_GOAL) {
            gameOver = true;
            gameMessage = "Dog wins! Collected 10 coins first.";
        } else if (timeLeftMs <= 0) {
            gameOver = true;
            gameMessage = "Time up! Cat " + catCoins + " vs Dog " + dogCoins + " coins.";
            if (score > highScore) highScore = score;
        } else if (!cat.isAlive()) {
            gameOver = true;
            if (score > highScore) highScore = score;
            // message set by Dog/Bird when cat dies
        }

        if (!gameOver) score++;
    }

    public void paint(Graphics g) {
        if (showStart) {
            // start screen
            g.setColor(new Color(255, 228, 235));
            g.fillRect(0, 0, 1024, 720);

            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("Welcome to Pinky Jungle!", 360, 200);
            g.setFont(new Font("SansSerif", Font.PLAIN, 18));
            g.drawString("Instructions:", 380, 260);
            g.drawString("- Use arrow keys or WASD to move your character", 380, 290);
            g.drawString("- Race to collect 10 coins before your rival (Dog or Cat)", 380, 315);
            g.drawString("- Beware the hidden Bird in the river — it ambushes swimmers", 380, 340);
            g.drawString("- Press P to pause, R to restart, Q to quit", 380, 365);
            g.drawString("Choose your character to start:", 380, 420);
            g.drawString("Press 1 to play as Cat", 380, 450);
            g.drawString("Press 2 to play as Dog", 380, 480);
            return;
        }

        drawSky(g);
        grid.paint(g);
        drawCoins(g);
        for (Actor a : actors) a.paint(g);

        // HUD
        g.setColor(Color.DARK_GRAY);
        g.drawString("Cat coins: " + catCoins + " / " + COIN_GOAL, 20, 660);
        g.drawString("Dog coins: " + dogCoins + " / " + COIN_GOAL, 20, 680);
        g.drawString("Time left: " + (timeLeftMs/1000) + "s", 20, 700);
        g.drawString(gameMessage, 260, 700);

        if (paused) {
            g.setColor(new Color(0,0,0,120));
            g.fillRect(0, 0, 1024, 720);
            g.setColor(Color.WHITE);
            g.drawString("PAUSED (press P to resume)", 400, 360);
        }

        if (gameOver) {
            g.setColor(new Color(0,0,0,140));
            g.fillRect(0, 0, 1024, 720);
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER: " + gameMessage, 360, 360);
            g.drawString("Press R to go to the start screen", 360, 380);
        }
    }

    private void drawCoins(Graphics g) {
        for (Coin c : coins) {
            g.setColor(new Color(255, 215, 0));
            g.fillOval(c.at.x + 10, c.at.y + 10, Cell.SIZE - 20, Cell.SIZE - 20);
            g.setColor(Color.ORANGE);
            g.drawOval(c.at.x + 10, c.at.y + 10, Cell.SIZE - 20, Cell.SIZE - 20);
        }
    }

    private void drawSky(Graphics g) {
        g.setColor(new Color(255, 221, 89)); g.fillOval(940, 30, 50, 50);
        g.setColor(Color.WHITE); drawCloud(g, 140, 50); drawCloud(g, 300, 70); drawCloud(g, 520, 45);
    }
    private void drawCloud(Graphics g, int x, int y) {
        g.fillOval(x, y, 60, 38); g.fillOval(x+22, y-8, 60, 38); g.fillOval(x+40, y, 60, 38);
    }
}