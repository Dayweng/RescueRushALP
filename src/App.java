import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class App {

    // simple, procedural-style game state (no custom classes)
    static int playerX = 100;
    static int playerY = 100;
    static int speed   = 10;
    static int GameState = 0; // 0 = menu, 1 = level1
    static Timer timer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rescue Rush");
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // create an instance so we can use non-static (public void) methods
            final App app = new App();

            JPanel panel = new JPanel() {
                public void paintComponent(Graphics g) {
                    //Base game Logic (mskin game state sini)
                    super.paintComponent(g);
                    if (GameState == 0) {
                        app.MenuScreen(g, this);
                    } else if (GameState == 1) {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Color.BLUE);
                        g.fillRect(playerX, playerY, 32, 32);
                    }
                }
            };

            panel.setFocusable(true);

            // keyboard: directly modify static state for smooth movement
            panel.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    int k = e.getKeyCode();
                    if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) playerY -= speed;
                    if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) playerY += speed;
                    if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) playerX -= speed;
                    if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) playerX += speed;
                }
            });

            // mouse: check button coordinates on click
            panel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    if (x >= 540 && x <= 740 && y >= 600 && y <= 650) {
                        GameState = 1;
                    }
                }
            });

            frame.add(panel);
            frame.setVisible(true);

            // ensure panel has focus so key events are delivered
            panel.requestFocusInWindow();

            // simple game loop timer (~60 FPS)
            timer = new Timer(16, ev -> {
                // could update game logic here
                panel.repaint();
            });
            timer.start();
        });
    }

    // procedural menu renderer
    public void MenuScreen(Graphics g, Component c) {
        ImageIcon mainMenuBG = new ImageIcon("C:\\Users\\Darren Wibisono\\Documents\\AlPro Coding\\ALP\\RescueRushALP\\assets\\omusi54tuse91.gif");
        g.drawImage(mainMenuBG.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        int buttonX = 540;
        int buttonY = 600;
        int buttonWidth = 200;
        int buttonHeight = 50;

        g.setColor(new Color(100, 150, 255));
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Start Game", buttonX + 50, buttonY + 35);
    }
}
