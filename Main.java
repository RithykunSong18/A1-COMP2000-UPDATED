// MOD: Added game loop logic and keyboard support modifications for character selection & in-game controls.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {

    static class App extends JPanel {
        Stage stage = new Stage();

        public App() {
            // MOD: Set preferred size for the drawing area.
            setPreferredSize(new Dimension(1024, 720));
            setFocusable(true);
            requestFocusInWindow();

            // MOD: Add KeyListener to handle character selection & in-game controls.
            addKeyListener(new KeyAdapter() {
                @Override 
                public void keyPressed(KeyEvent e) {
                    int k = e.getKeyCode();

                    // MOD: Handle start screen key events (character selection).
                    if (stage.showStart) {
                        if (k == KeyEvent.VK_1) { // Cat
                            stage.chosenCharacter = "Cat";
                            stage.showStart = false;
                            stage.buildWorldWithChoice();
                        } else if (k == KeyEvent.VK_2) { // Dog
                            stage.chosenCharacter = "Dog";
                            stage.showStart = false;
                            stage.buildWorldWithChoice();
                        }
                        return;
                    }

                    // MOD: Handle in-game movement and controls.
                    switch (k) {
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A: stage.setInput(-1, 0); break;
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D: stage.setInput(1, 0); break;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W: stage.setInput(0, -1); break;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S: stage.setInput(0, 1); break;
                        case KeyEvent.VK_R: stage.resetGame(); break;
                        case KeyEvent.VK_Q: System.exit(0); break;
                        case KeyEvent.VK_P: stage.togglePause(); break;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // MOD: Paint a soft pink background.
            g.setColor(new Color(255, 228, 235));
            g.fillRect(0, 0, getWidth(), getHeight());
            stage.paint(g);
        }
    }

    // MOD: Main method initializes JFrame and starts the game loop.
    public static void main(String[] args) {
        Main m = new Main();
        m.run();
    }

    private final App app;

    // MOD: JFrame configuration, setting content pane and window properties.
    public Main() {
        setTitle("Pinky Jungle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app = new App();
        setContentPane(app);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // MOD: Game loop implementation calling tick, repaint and thread sleep.
    private void run() {
        while (true) {
            if (!app.stage.paused && !app.stage.showStart) {
                app.stage.tick();
            }
            repaint();
            try { 
                Thread.sleep(120); 
            } catch (InterruptedException e) { 
                break; 
            }
        }
    }
}