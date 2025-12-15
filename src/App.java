import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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
    public int speed = 10;
    public int GameState = 0;
    /* 
        Game State List:
        0 = Main Menu
        4 = Level Picker Screen
        1 = Level 1
        2 = Level 2
        3 = Level 3 
    */

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
        
    }

    // ===== DRAW EVERYTHING HERE =====
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (GameState == 0) {
            MenuScreen(g);
        } else if (GameState == 1) {
            /// background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            // player
            g.setColor(Color.BLUE);
            g.fillRect(playerX, playerY, 32, 32);
        } else if (GameState == 2) {
            // Level 2 drawing
        } else if (GameState == 3) {
            // Level 3 drawing
        } else if (GameState == 4) {
            // Level Picker drawing
        }

    }

    // Game Screens Designs
    public void MenuScreen(Graphics g) {
        ImageIcon mainMenuBG = new ImageIcon("C:\\Users\\Darren Wibisono\\Documents\\AlPro Coding\\ALP\\RescueRushALP\\assets\\omusi54tuse91.gif");
        g.drawImage(mainMenuBG.getImage(), 0, 0, getWidth(), getHeight(), this);
    }

    // INPUT KEYBOARD
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) playerY -= speed;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) playerY += speed;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) playerX -= speed;
        if (k == KeyEvent.VK_D | k == KeyEvent.VK_RIGHT) playerX += speed;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}
