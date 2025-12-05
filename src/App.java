import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow());
    }
}

class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("Rescue Rush");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        add(new GamePanel());

        setVisible(true);
    }
}

class GamePanel extends JPanel implements KeyListener {

    // ===== GAME VARIABLES: put ALL your game data here =====
    public int playerX = 100;
    public int playerY = 100;
    public int speed = 4;

    // Timer for game loop (public only)
    public Timer timer;

    public GamePanel() {
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(16, e -> {
            update();
            repaint();
        });
        timer.start();
    }

    // ===== UPDATE GAME LOGIC =====
    public void update() {
        // move enemies, physics, collision, etc.
    }

    // ===== DRAW EVERYTHING HERE =====
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // background
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, getWidth(), getHeight());

        // player
        g.setColor(Color.RED);
        g.fillRect(playerX, playerY, 32, 32);
    }

    // ===== KEY INPUT =====
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W) playerY -= speed;
        if (k == KeyEvent.VK_S) playerY += speed;
        if (k == KeyEvent.VK_A) playerX -= speed;
        if (k == KeyEvent.VK_D) playerX += speed;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}
