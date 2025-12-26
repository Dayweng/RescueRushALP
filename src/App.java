import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;

public class App {

    // simple, procedural-style game state (no custom classes)
    static int playerX;
    static int playerY;
    static int speed = 4;
    static int GameState = 0; // 0 = menu, 1 = level1
    static int nextScreen = 0;
    static Timer timer;
    static Timer timerInternal;

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public BufferedImage grass, water0, waterup, waterdown, waterleft, waterright, waterupright, waterupleft, waterdownright, waterdownleft, road;
    public BufferedImage warningGreen, warningYellow, warningRed;
    static String direction = "down";
    static int spriteCounter = 0;
    static int spriteNum = 1;
    static boolean isMoving = false;
    static int tileSize = 32;
    static int panelWidth = 1366;
    static int panelHeight = 768;
    static char[][] map;

    static int[][] warningPoints = new int[3][2];
    static Random random = new Random();

    // Emergency points: List of [x, y, type, resolved, subtype]
    static List<int[]> emergencies = new ArrayList<>();
    static List<String> backpack = new ArrayList<>();
    static List<String> evacuationQueue = new ArrayList<>();
    static int[] safeZone = new int[2];
    static int backpackCapacity = 5;
    static int evacCapacity;
    static int timeLeft = 300; // 5 minutes
    static Timer gameTimer;
    static int currentLevel = 1; // 1,2,3

    // Case data
    static List<Map<String, String>> cases = new ArrayList<>();
    static int timeLimitSeconds = 300;
    static int evacuationCapacity = 1;

    // In-game dialog
    static boolean isDialogShowing = false;
    static String dialogText = "";
    static int dialogType = 0; // 0 = emergency, 1 = backpack
    static int selectedToolIndex = -1;

    public static void drawSubWindow(Graphics2D graphics2D, int x, int y, int width, int height) {
        // Background hitam transparan
        Color color = new Color(0, 0, 0, 210);
        graphics2D.setColor(color);
        graphics2D.fillRoundRect(x, y, width, height, 35, 35);

        // Border putih
        color = new Color(255, 255, 255);
        graphics2D.setColor(color);
        graphics2D.setStroke(new BasicStroke(5));
        graphics2D.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    static List<String> wrapText(String text, Graphics g, int maxWidth) {
        List<String> lines = new ArrayList<>();
        FontMetrics fm = g.getFontMetrics();
        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            String[] words = para.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String testLine = line.length() == 0 ? word : line + " " + word;
                if (fm.stringWidth(testLine) <= maxWidth) {
                    line = new StringBuilder(testLine);
                } else {
                    if (line.length() > 0) {
                        lines.add(line.toString());
                        line = new StringBuilder(word);
                    } else {
                        // word too long, add as is or break
                        lines.add(word);
                    }
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    static void loadCasesFromTXT() {
        cases.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("assets/case/level1_case.txt"));
            String line;
            boolean inCase = false;
            Map<String, String> currentCase = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equals("---")) {
                    if (currentCase != null) {
                        cases.add(currentCase);
                    }
                    currentCase = new HashMap<>();
                    inCase = true;
                    continue;
                }
                int eqIndex = line.indexOf("=");
                if (eqIndex != -1) {
                    String key = line.substring(0, eqIndex).trim();
                    String value = line.substring(eqIndex + 1).trim();
                    if (!inCase) {
                        // global config
                        if (key.equals("TIME_LIMIT_SECONDS")) {
                            timeLimitSeconds = Integer.parseInt(value);
                        } else if (key.equals("BACKPACK_CAPACITY")) {
                            backpackCapacity = Integer.parseInt(value);
                        } else if (key.equals("EVACUATION_CAPACITY")) {
                            evacuationCapacity = Integer.parseInt(value);
                        }
                    } else {
                        // case fields
                        currentCase.put(key, value);
                    }
                }
            }
            if (currentCase != null) {
                cases.add(currentCase);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // map games

    public BufferedImage imagemap1;


    //User ScreenSize
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    static int Screenwidth = 1366; //(int) screenSize.getWidth();
    static int Screenheight = 768; //(int) screenSize.getHeight();

    public static void main(String[] args) {
        System.out.println("Rescue Rush Game Starting...");
        System.out.println("Screen Width: " + Screenwidth + ", Screen Height: " + Screenheight);
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rescue Rush");
            //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setSize(1366, 768);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            //frame.setUndecorated(true);

            // create an instance so we can use non-static (public void) methods
            final App app = new App();
            app.loadCharacterImages();
            app.loadTileImages();
            app.loadMap();
            spawnPlayerOnRoad();

            JPanel panel = new JPanel() {
                public void paintComponent(Graphics g) {
                    //Base game Logic (mskin game state sini)
                    super.paintComponent(g);
                    if (GameState == 0) {
                        app.OnBoarding(g, this);
                    } else if (GameState == 5) {
                        app.MenuScreen(g, this);
                    } else if (GameState == 1) {
                        app.drawTileMap(g, this);
                        app.drawCharacter(g, playerX, playerY, direction, spriteNum);
                        app.drawEmergencies(g, this);
                        app.drawSafeZone(g, this);
                        app.drawTimer(g, this);
                        if (isDialogShowing) {
                            Graphics2D g2 = (Graphics2D) g;
                            drawSubWindow(g2, 200, 200, 400, 150);
                            g.setColor(Color.WHITE);
                            g.setFont(new Font("Arial", Font.BOLD, 16));
                            List<String> wrappedLines = wrapText(dialogText, g, 380); // max width 380
                            for (int i = 0; i < wrappedLines.size() && i < 5; i++) { // limit to 5 lines
                                g.drawString(wrappedLines.get(i), 220, 240 + i * 20);
                            }
                        }
                    } else if (GameState == 2) {
                        app.drawWinScreen(g, this);
                    } else if (GameState == 3) {
                        app.drawFailScreen(g, this);
                    } else if (GameState == 100) {
                        app.selectLevelSScreen(g, this);
                    }
                }
            };

            panel.setFocusable(true);

            // keyboard: directly modify static state for smooth movement
            panel.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    int k = e.getKeyCode();
                    if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) {
                        int newY = playerY - speed;
                        if (newY >= 0 && canMove(playerX, newY)) {
                            direction = "up";
                            playerY = newY;
                            isMoving = true;
                        }
                    }
                    if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) {
                        int newY = playerY + speed;
                        if (newY <= panelHeight - tileSize && canMove(playerX, newY)) {
                            direction = "down";
                            playerY = newY;
                            isMoving = true;
                        }
                    }
                    if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) {
                        int newX = playerX - speed;
                        if (newX >= 0 && canMove(newX, playerY)) {
                            direction = "left";
                            playerX = newX;
                            isMoving = true;
                        }
                    }
                    if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) {
                        int newX = playerX + speed;
                        if (newX <= panelWidth - tileSize && canMove(newX, playerY)) {
                            direction = "right";
                            playerX = newX;
                            isMoving = true;
                        }
                    }
                    if (k == KeyEvent.VK_ENTER) {
                        if (isDialogShowing) {
                            isDialogShowing = false;
                        } else {
                            app.interactWithEmergency();
                        }
                    }
                    if (isDialogShowing && (k == KeyEvent.VK_1 || k == KeyEvent.VK_2)) {
                        handleDialogChoice(k == KeyEvent.VK_1 ? 0 : 1);
                        isDialogShowing = false;
                    }
                    if (isDialogShowing && dialogType == 1) { // Backpack dialog
                        if (k == KeyEvent.VK_Q) {
                            isDialogShowing = false;
                        }
                    }
                    if (k == KeyEvent.VK_E) {
                        app.showBackpackDialog();
                    }
                    if (k == KeyEvent.VK_ESCAPE) frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
                public void keyReleased(KeyEvent e) {
                    int k = e.getKeyCode();
                    if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP || k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN ||
                        k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT || k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) {
                        isMoving = false;
                        spriteNum = 1; // reset to standing pose
                    }
                }
            });

            // mouse: check button coordinates on click
            panel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    if (x >= 540 && x <= 740 && y >= 600 && y <= 650) {
                        GameState = 100;
                    } else if (x >= 540 && x <= 740 && y >= 250 && y <= 300) {
                        currentLevel = 1;
                        GameState = 1;
                        startLevel();
                    } else if (x >= 540 && x <= 740 && y >= 320 && y <= 370) {
                        currentLevel = 2;
                        GameState = 1;
                        startLevel();
                    } else if (x >= 540 && x <= 740 && y >= 390 && y <= 440) {
                        currentLevel = 3;
                        GameState = 1;
                        startLevel();
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
                updateSprite();
                panel.repaint();
            });
            timer.start();
        });
    }

    public void OnBoarding(Graphics g, Component c) {
        ImageIcon logo = new ImageIcon("assets/Logo/Rescue Rush Wide Logo.png");
        g.drawImage(logo.getImage(), Screenwidth/4, Screenheight/4, Screenwidth/2, Screenheight/3, c);
        
        timerInternal = new Timer(2000, ev -> {
                nextScreen = 1;
            });
        
        timerInternal.start();
        if (nextScreen == 1){
            timerInternal.stop();
            GameState = 5;
        }

    }
    
    public void MenuScreen(Graphics g, Component c) {
        ImageIcon mainMenuBG = new ImageIcon("assets/menu-background.gif");
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

    public void selectLevelSScreen(Graphics g, Component c) {
        ImageIcon mainMenuBG = new ImageIcon("assets/menu-background.gif");
        g.drawImage(mainMenuBG.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        int buttonX = 540;
        int buttonY = 250;
        int buttonWidth = 170;
        int buttonHeight = 50;

        // button to start level 1
        g.setColor(new Color(100, 150, 255));
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("LEVEL 1", buttonX + 50, buttonY + 35);

        //button to start level 2
        buttonY += 70;

        g.setColor(new Color(100, 150, 255));
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("LEVEL 2", buttonX + 50, buttonY + 35);

        //button to start level 3

        buttonY += 70;
        g.setColor(new Color(100, 150, 255));
        g.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("LEVEL 3", buttonX + 50, buttonY + 35);

    }

    // CHARACTER FUNCTION

    public void loadCharacterImages() {
        try {
            up1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_up_1.png"));
            up2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_up_2.png"));
            down1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_down_1.png"));
            down2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_down_2.png"));
            left1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_left_1.png"));
            left2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_left_2.png"));
            right1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_right_1.png"));
            right2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_right_2.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawCharacter(Graphics g, int x, int y, String dir, int num) {
        BufferedImage img = null;
        if (dir.equals("up")) {
            img = (num == 1) ? up1 : up2;
        } else if (dir.equals("down")) {
            img = (num == 1) ? down1 : down2;
        } else if (dir.equals("left")) {
            img = (num == 1) ? left1 : left2;
        } else if (dir.equals("right")) {
            img = (num == 1) ? right1 : right2;
        }
        if (img != null) {
            g.drawImage(img, x, y, 32, 32, null);
        }
    }

    public static void updateSprite() {
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 1;
        }
    }

    // MAP FUNCTIONS

    public void loadTileImages() {
        try {
            grass = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/grass01.png"));
            water0 = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water01.png"));
            waterup = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water08.png"));
            waterdown = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water03.png"));
            waterleft = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water06.png"));
            waterright = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water05.png"));
            waterupright = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water10.png"));
            waterupleft = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water11.png"));
            waterdownright = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water12.png"));
            waterdownleft = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/water13.png"));
            road = javax.imageio.ImageIO.read(new java.io.File("assets/images/tile/road00.png"));
            warningGreen = javax.imageio.ImageIO.read(new java.io.File("assets/images/interactive/warning-green.png"));
            warningYellow = javax.imageio.ImageIO.read(new java.io.File("assets/images/interactive/warning-yellow.png"));
            warningRed = javax.imageio.ImageIO.read(new java.io.File("assets/images/interactive/warning-red.png"));
            warningYellow = javax.imageio.ImageIO.read(new java.io.File("assets/images/interactive/warning-yellow.png"));
            warningRed = javax.imageio.ImageIO.read(new java.io.File("assets/images/interactive/warning-red.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawTileMap(Graphics g, Component c) {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("assets/maps/level1.txt"));
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                for (int col = 0; col < line.length(); col++) {
                    char tile = line.charAt(col);
                    if (tile == '0') {
                        g.drawImage(road, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '1') {
                        g.drawImage(grass, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '2') {
                        g.drawImage(water0, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '3') {
                        g.drawImage(waterup, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '4') {
                        g.drawImage(waterdown, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '5') {  
                        g.drawImage(waterleft, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '6') {  
                        g.drawImage(waterright, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '7') {  
                        g.drawImage(waterupright, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '8') {  
                        g.drawImage(waterupleft, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == '9') {  
                        g.drawImage(waterdownright, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                    if (tile == 'A') {  
                        g.drawImage(waterdownleft, col * tileSize, row * tileSize, tileSize, tileSize, c);
                    }
                }
                row++;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void drawWarnings(Graphics g, Component c) {
        for (int i = 0; i < 3; i++) {
            g.drawImage(warningGreen, warningPoints[i][0], warningPoints[i][1], tileSize, tileSize, c);
        }
    }

    public void loadMap() {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("assets/maps/level1.txt"));
            java.util.List<String> lines = new java.util.ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            int rows = lines.size();
            int cols = lines.get(0).length();
            map = new char[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    map[r][c] = lines.get(r).charAt(c);
                }
            }
            generateWarningPoints();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean canMove(int x, int y) {
        int left = x;
        int right = x + tileSize - 1;
        int top = y;
        int bottom = y + tileSize - 1;
        return isWalkable(left, top) && isWalkable(right, top) && isWalkable(left, bottom) && isWalkable(right, bottom);
    }

    private static boolean isWalkable(int x, int y) {
        int col = x / tileSize;
        int row = y / tileSize;
        if (col < 0 || col >= map[0].length || row < 0 || row >= map.length) {
            return false;
        }
        return map[row][col] == '0';
    }

    public void generateWarningPoints() {
        int count = 0;
        while (count < 3) {
            int col = random.nextInt(map[0].length);
            int row = random.nextInt(map.length);
            if (map[row][col] == '1' && row > 0) { // grass and not top row
                warningPoints[count][0] = col * tileSize;
                warningPoints[count][1] = (row - 1) * tileSize; // one tile above
                count++;
            }
        }
    }

    static void spawnPlayerOnRoad() {
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == '0') {
                    playerX = c * tileSize;
                    playerY = r * tileSize;
                    return;
                }
            }
        }
    }

    static void startLevel() {
        loadCasesFromTXT();
        timeLeft = timeLimitSeconds;
        emergencies.clear();
        backpack.clear();
        evacuationQueue.clear();
        setSafeZone();
        evacCapacity = evacuationCapacity;
        generateEmergencies();
        if (gameTimer != null) gameTimer.stop();
        gameTimer = new Timer(1000, ev -> {
            timeLeft--;
            if (timeLeft <= 0) {
                GameState = 3; // fail
                gameTimer.stop();
            } else if (checkWinCondition()) {
                GameState = 2; // win
                gameTimer.stop();
            }
        });
        gameTimer.start();
    }

    static void generateEmergencies() {
        for (int i = 0; i < cases.size(); i++) {
            Map<String, String> caseData = cases.get(i);
            String typeStr = caseData.get("TYPE");
            int type = 0; // green
            if ("YELLOW".equals(typeStr)) type = 1;
            else if ("RED".equals(typeStr)) type = 2;
            // Generate random position on grass
            int col = random.nextInt(map[0].length);
            int row = random.nextInt(map.length);
            while (map[row][col] != '1' || row <= 0) { // ensure grass and not top row
                col = random.nextInt(map[0].length);
                row = random.nextInt(map.length);
            }
            int x = col * tileSize;
            int y = (row - 1) * tileSize; // one tile above
            emergencies.add(new int[]{x, y, type, 0, i});
        }
    }

    static void setSafeZone() {
        safeZone[0] = (map[0].length / 2) * tileSize;
        safeZone[1] = (map.length / 2) * tileSize;
    }





    static boolean checkWinCondition() {
        boolean allRedEvacuated = true;
        boolean allGreenCollected = true;
        for (int[] e : emergencies) {
            Map<String, String> caseData = cases.get(e[4]);
            String type = caseData.get("TYPE");
            if ("RED".equals(type) && e[3] == 0) allRedEvacuated = false;
            if ("GREEN".equals(type) && e[3] == 0) allGreenCollected = false;
        }
        return allRedEvacuated && allGreenCollected;
    }

    static boolean allEmergenciesResolved() {
        for (int[] e : emergencies) {
            if (e[3] == 0) return false;
        }
        return true;
    }

    void interactWithEmergency() {
        int playerCol = playerX / tileSize;
        int playerRow = playerY / tileSize;
        int safeCol = safeZone[0] / tileSize;
        int safeRow = safeZone[1] / tileSize;
        if (Math.abs(playerCol - safeCol) <= 1 && Math.abs(playerRow - safeRow) <= 1 && !evacuationQueue.isEmpty()) {
            String evacuated = evacuationQueue.remove(0);
            dialogText = evacuated + " successfully evacuated to safe zone!";
            isDialogShowing = true;
            return;
        }
        for (int[] e : emergencies) {
            int eCol = e[0] / tileSize;
            int eRow = e[1] / tileSize;
            int dx = Math.abs(playerCol - eCol);
            int dy = Math.abs(playerRow - eRow);
            if (dx <= 1 && dy <= 1 && e[3] == 0) { // within 1 tile
                int caseIdx = e[4];
                Map<String, String> caseData = cases.get(caseIdx);
                String title = caseData.get("TITLE");
                String desc = caseData.get("DESCRIPTION");
                String type = caseData.get("TYPE");
                if ("GREEN".equals(type)) {
                    dialogText = title + "\n" + desc + "\n1. TAKE\n2. IGNORE";
                } else if ("RED".equals(type)) {
                    String reqItem = caseData.get("REQUIRED_ITEM");
                    if (reqItem != null && !reqItem.equals("none") && !backpack.contains(reqItem)) {
                        dialogText = title + "\n" + desc + "\nYou need " + reqItem + " to save this emergency.\n1. IGNORE";
                    } else {
                        dialogText = title + "\n" + desc + "\n1. SAVE\n2. IGNORE";
                    }
                } else if ("YELLOW".equals(type)) {
                    String question = caseData.get("QUESTION");
                    String choices = caseData.get("CHOICES");
                    String[] choiceArr = choices.split(",");
                    dialogText = title + "\n" + desc + "\n" + question + "\n1. " + choiceArr[0] + "\n2. " + choiceArr[1];
                }
                isDialogShowing = true;
                dialogType = 0; // emergency
                break;
            }
        }
    }

    static void handleDialogChoice(int choice) {
        if (dialogType == 0) { // Emergency
            // Find the current emergency
            int playerCol = playerX / tileSize;
            int playerRow = playerY / tileSize;
            for (int[] e : emergencies) {
                int eCol = e[0] / tileSize;
                int eRow = e[1] / tileSize;
                int dx = Math.abs(playerCol - eCol);
                int dy = Math.abs(playerRow - eRow);
                if (dx <= 1 && dy <= 1 && e[3] == 0) {
                    int caseIdx = e[4];
                    Map<String, String> caseData = cases.get(caseIdx);
                    String type = caseData.get("TYPE");
                    String title = caseData.get("TITLE");
                    if (choice == 0) { // TAKE or SAVE
                        if ("GREEN".equals(type)) {
                            String reward = caseData.get("ITEM_REWARD");
                            if (reward != null && !reward.isEmpty() && backpack.size() < backpackCapacity) {
                                backpack.add(reward);
                                e[3] = 1;
                                dialogText = "Item collected: " + reward;
                                isDialogShowing = true;
                            } else if (backpack.size() >= backpackCapacity) {
                                dialogText = "Backpack is full!";
                                isDialogShowing = true;
                            }
                        } else if ("RED".equals(type)) {
                            String reqItem = caseData.get("REQUIRED_ITEM");
                            if (reqItem != null && !reqItem.equals("none") && !backpack.contains(reqItem)) {
                                dialogText = "You must have " + reqItem + " to save this emergency.";
                                isDialogShowing = true;
                            } else {
                                if (evacuationQueue.size() < evacCapacity) {
                                    if (reqItem != null && !reqItem.equals("none")) {
                                        backpack.remove(reqItem); // Remove the required item from backpack
                                    }
                                    String evacType = caseData.get("EVAC_TYPE");
                                    evacuationQueue.add(title + " (" + evacType + ")");
                                    e[3] = 1;
                                    dialogText = "Evacuation started!";
                                    isDialogShowing = true;
                                } else {
                                    dialogText = "Evacuation capacity full!";
                                    isDialogShowing = true;
                                }
                            }
                        } else if ("YELLOW".equals(type)) {
                            // For yellow, just resolve
                            e[3] = 1;
                            dialogText = "Situation handled.";
                            isDialogShowing = true;
                        }
                    } else { // IGNORE
                        isDialogShowing = false;
                    }
                    break;
                }
            }
        } else if (dialogType == 1) { // Backpack - this will be handled in keyPressed for U/D
            // For backpack, choice is for selecting tool, but handled separately
        }
    }


    void showBackpackDialog() {
        if (backpack.isEmpty()) {
            dialogText = "No tools in backpack.";
            dialogType = 1;
            isDialogShowing = true;
            return;
        }
        StringBuilder sb = new StringBuilder("Backpack Tools:\n");
        for (int i = 0; i < backpack.size(); i++) {
            sb.append((i + 1)).append(". ").append(backpack.get(i)).append("\n");
        }
        sb.append("Press Q to cancel");
        dialogText = sb.toString();
        dialogType = 1;
        selectedToolIndex = -1;
        isDialogShowing = true;
    }

    void drawEmergencies(Graphics g, Component c) {
        for (int[] e : emergencies) {
            if (e[3] == 0) { // not resolved
                BufferedImage img = warningGreen;
                if (e[2] == 1) img = warningYellow;
                else if (e[2] == 2) img = warningRed;
                g.drawImage(img, e[0], e[1], tileSize, tileSize, c);
            }
        }
    }

    void drawTimer(Graphics g, Component c) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        String timeStr = String.format("Time: %02d:%02d", minutes, seconds);
        g.drawString(timeStr, 10, 30);
        g.drawString("Backpack: " + backpack.size() + "/" + backpackCapacity, 10, 60);
        g.drawString("Evacuation: " + evacuationQueue.size() + "/" + evacCapacity, 10, 90);
    }

    void drawSafeZone(Graphics g, Component c) {
        g.setColor(Color.BLUE);
        g.fillRect(safeZone[0], safeZone[1], tileSize, tileSize);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("SAFE", safeZone[0] + 5, safeZone[1] + 20);
    }

    void drawWinScreen(Graphics g, Component c) {
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Evacuation Successful!", Screenwidth/4, Screenheight/2);
    }

    void drawFailScreen(Graphics g, Component c) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Time is up. Evacuation failed.", Screenwidth/4, Screenheight/2);
    }

}

