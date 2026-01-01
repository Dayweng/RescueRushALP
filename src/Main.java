import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;

public class Main {

    // Config Variables
    static int frameWidth = 1366;
    static int frameHeight = 768;
    static Font gameFont;

    static int unlockedLevels;
    static boolean levelUIRefreshed;

    // Game Logic Variables
    static int playerX;
    static int playerY;
    static String direction = "down";
    static boolean isMoving = false;
    static int spriteCounter = 0;
    static int spriteNum = 1;
    static int moveSpeed = 4;

    static String message = "";
    static boolean showMessage = false;
    static boolean warningMessage = false;

    static int timeLimitSeconds = 71;
    static int timeLeft = timeLimitSeconds;
    static Timer gameTimeTimer;

    static boolean isFlooding = false;
    static boolean isMapFrozen = false;
    static int floodStep = 0;
    static Timer floodTimer;
    static Timer earthquakeTimer;

    static int backpack = 0;
    static int[] backpackValue = new int[2];
    static String[] backpackItems = new String[2];
    static int evacuated = 0;
    static int totalEvacuated;

    static ArrayList<String[]> Emergency = new ArrayList<>();
    static int currentEmergencyIndex = -1;

    static boolean isCountingDown = true;
    static boolean level1Inizialized = false;
    static boolean level2Inizialized = false;
    static int countdownValue = 3;
    static Timer countdownTimer;

    static char[][] map;
    static int[][] warningPoints;
    static int tileSize = 32;
    static Random random = new Random();

    static String results = "";

    // Navigation Variables
    static boolean onboardingTimerStarted = false;
    static boolean levelLoadingTimerStarted = false;
    static int gameState = 0;

    // UI Variables
    static JButton startButton;
    static JButton settingsButton;
    static JButton level_1;
    static JButton level_2;
    static JButton level_3;
    static JButton backButton;
    static JButton retryButton;
    static JButton selectLevelButton;
    static JButton nextLevelButton;
    static JButton exitLevelButton;
    static JSlider volumeSlider;

    static BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    static BufferedImage grass, water0, waterup, waterdown, waterleft, waterright, waterupright, waterupleft, waterdownright, waterdownleft, road;
    static BufferedImage warningGreen, warningYellow, warningRed, earthquakeBlock;
    static BufferedImage house1;

    //UI Scalling Var
    static int BASE_CARD_W = 500;
    static int BASE_CARD_H = 640;
    static double scale = frameWidth / 1920.0;
    static int cardW = (int)(frameWidth * 0.26);
    static int cardH = (int)(cardW * 1.28);
    static int centerX = frameWidth / 2;
    static int centerY = frameHeight / 2;
    //Level Cards
    static int gap = (int)(60 * scale);
    static int level2X = centerX - cardW / 2;
    static int level1X = level2X - cardW - gap;
    static int level3X = level2X + cardW + gap;
    static int cardsY = (int)(frameHeight * 0.18);
    //Back Button Size & Position
    static int backW = (int)(220 * scale);
    static int backH = (int)(70 * scale);
    static int backX = frameWidth / 2 - backW / 2;


    // Music Variables
    static Clip menuBGM;
    static FloatControl volumeControl;    


    //#region MAIN SYSTEM
    public static void main(String[] args) {
        // Game Properties
        System.out.println("Game Started Window Size: " + frameWidth + "x" + frameHeight);
        SwingUtilities.invokeLater(()-> {
            JFrame mainFrame = new JFrame();
            
            mainFrame.setSize(frameWidth, frameHeight);
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setResizable(false);
            mainFrame.setVisible(true);
            playBGM("assets/sounds/BGMmenu.wav");

            Main app = new Main();

            // loadCasesFromTXT("assets/data/case/level1-case.txt");
            // totalEvacuation();
            loadUserData();
            app.loadTileImages();
            app.loadMap();
            app.spawnCharacter();

            JPanel gamePanel = new JPanel() {
                public void paintComponent (Graphics g) {
                    super.paintComponent(g);

                    startButton.setVisible(gameState == 4);
                    settingsButton.setVisible(gameState == 4);
                    level_1.setVisible(gameState == 5);
                    level_2.setVisible(gameState == 5);
                    level_3.setVisible(gameState == 5);
                    backButton.setVisible(gameState == 5 || gameState == 10);
                    volumeSlider.setVisible(gameState == 10);
                    retryButton.setVisible(gameState == 6 && (results.equals("TIME") || results.equals("FLOODED")));
                    selectLevelButton.setVisible(gameState == 6);
                    nextLevelButton.setVisible(gameState == 6 && results.equals("SUCCESS"));
                    exitLevelButton.setVisible(gameState == 1 || gameState == 2);

                    //#region Game states
                    /* 
                    GAME STATE 0: Onboarding Screen
                    GAME STATE 1: Level 1 Screen
                    GAME STATE 2: Level 2 Screen
                    GAME STATE 3: Level 3 Screen
                    GAME STATE 4: Main Menu Screen
                    GAME STATE 5: Level Selection Screen
                    GAME STATE 6: Result Screen
                    GAME STATE 7: Paused Screen (NOT YET IMPLEMENTED)

                    GAME STATE 10: Settings Screen
                    GAME STATE 11: Loading Level 1
                    GAME STATE 12: Loading Level 2
                    GAME STATE 13: Loading Level 3
                     */
                    if (gameState == 0) { 
                        app.OnBoarding(g, this);
                    } else if (gameState == 4) {
                        app.MainMenu(g, this);
                    } else if (gameState == 5) {
                        if (!levelUIRefreshed) {
                            loadUserData();       
                            refreshLevelUI();   
                            levelUIRefreshed = true;
                        }
                        app.LevelScreen(g, this);
                    } else if (gameState == 6) {
                        app.ResultScreen(g, this);
                    } else if (gameState == 1) {
                        app.Level1_Screen(g, this);
                        levelLoadingTimerStarted = false;
                    } else if (gameState == 2) {
                        app.Level2_Screen(g, this);
                        levelLoadingTimerStarted = false;
                    } else if (gameState == 3) {
                        app.Level3_Screen(g, this);
                        levelLoadingTimerStarted = false;
                    } else if (gameState == 10) {
                        app.Setting(g, this);
                    } else if (gameState == 11) {
                        app.level1_loading(g, this);
                    } else if (gameState == 12) {
                        app.level2_loading(g, this);
                    } else if (gameState == 13) {
                        app.level3_loading(g, this);
                    }
                }
            };
            gamePanel.setLayout(null);
            gamePanel.setPreferredSize(new Dimension(frameWidth, frameHeight));
            gamePanel.setSize(frameWidth, frameHeight);
            System.out.println("Panel size: " + gamePanel.getWidth() + " x " + gamePanel.getHeight());
            System.out.println("Card size: " + cardW + " x " + cardH);

            //#region UI COMPONENTS INITIALIZATION

            // Volume Slider
            volumeSlider = new JSlider(50, 100, 70);
            volumeSlider.setBounds(centerX-150, centerY, 300, 40);
            volumeSlider.setOpaque(false); // defolt bg transparent
            gamePanel.add(volumeSlider);

            // setting button
            settingsButton = createImageButton(
                "assets/images/buttons/setting-button.png",
                centerX(400), 500, 400, 80,
                () -> gameState = 10,
                gamePanel
            );

            // start button
            startButton = createImageButton(
                "assets/images/buttons/start-button.png",
                centerX(400), 400, 400, 80,
                () -> gameState = 5,
                gamePanel
            );
            
            // LEVEL 1
            level_1 = createImageButton(
                "assets/images/buttons/level1-button.png",
                level1X, cardsY, cardW, cardH,
                () -> gameState = 11,
                gamePanel
            );

            // LEVEL 2
            level_2 = createImageButton(
                (unlockedLevels == 2 || unlockedLevels == 3) ? "assets/images/buttons/level2-button.png" : "assets/images/buttons/level2-locked.png",
                level2X, cardsY,
                cardW, cardH,
                () -> {
                    if (unlockedLevels == 2 || unlockedLevels == 3) {
                        gameState = 12;
                    } else if (unlockedLevels == 1){
                        gameState = 5;
                    }
                },
                gamePanel
            );

            // LEVEL 3
            level_3 = createImageButton(
                (unlockedLevels == 3) ? "assets/images/buttons/level3-button.png" : "assets/images/buttons/level3-locked.png",
                level3X, cardsY, cardW, cardH,
                () -> {
                    if (unlockedLevels == 3) {
                        gameState = 12;
                    } else {
                        gameState = 5;
                    }
                },
                gamePanel
            );

            // back button
            backButton = createImageButton(
                "assets/images/buttons/back-button.png",
                backX, (frameHeight/2)+250, backW, backH,
                () -> gameState = 4,
                gamePanel
            );

            // retry button
            retryButton = createImageButton(
                "assets/images/buttons/back-button.png",
                centerX(400), 400, 400, 80,
                () -> {
                    app.resetGame();
                    gameState = 1;
                    level1Inizialized = false;
                    level2Inizialized = false;
                },
                gamePanel
            );

            // select level button
            selectLevelButton = createImageButton(
                "assets/images/buttons/back-button.png",
                centerX(400), 500, 400, 80,
                () -> {
                    app.resetGame();
                    gameState = 5;
                    level1Inizialized = false;
                    level2Inizialized = false;
                    levelUIRefreshed = false;
                },
                gamePanel
            );

            // next level button
            nextLevelButton = createImageButton(
                "assets/images/buttons/back-button.png",
                centerX(400), 400, 400, 80,
                () -> {
                    gameState = 2;
                },
                gamePanel
            );

            // exit level button
            exitLevelButton = createImageButton(
                "assets/images/buttons/back-button.png",
                1260, 10, 100, 40,
                () -> {
                    app.resetGame();
                    gameState = 5;
                    level1Inizialized = false;
                    level2Inizialized = false;
                },
                gamePanel
            );
            //#endregion

            //#region KEY LISTENERS

            gamePanel.setFocusable(true);
            gamePanel.requestFocusInWindow();

            gamePanel.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e){
                    int key = e.getKeyCode();
                    
                    if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
                        int newY = playerY - moveSpeed;
                        if (canMove(playerX, newY)) {
                            direction = "up";
                            playerY = newY;
                            isMoving = true;
                        }
                    }

                    if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
                        int newY = playerY + moveSpeed;
                        if (canMove(playerX, newY)) {
                            direction = "down";
                            playerY = newY;
                            isMoving = true;
                        }
                    }

                    if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
                        int newX = playerX - moveSpeed;
                        if (canMove(newX, playerY)) {
                            direction = "left";
                            playerX = newX;
                            isMoving = true;
                        }   
                    }

                    if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
                        int newX = playerX + moveSpeed;
                        if (canMove(newX, playerY)) {
                            direction = "right";
                            playerX = newX;
                            isMoving = true;
                        }
                    }
                    if (key == KeyEvent.VK_ENTER) {
                        if(warningMessage) {
                            warningMessage = false;
                            message = "";
                        }
                        if(showMessage) {
                            showMessage = false;
                            message = "";
                        } else {
                            app.cekPoint();
                        }
                    }
                    if (key == KeyEvent.VK_Q) {
                        if(showMessage) {
                            showMessage = false;
                            message = "";
                        }
                    }
                    if (key == KeyEvent.VK_ESCAPE) {
                        System.out.println("Game Exited by User");
                        System.exit(0);
                    }
                    if (showMessage && (key == KeyEvent.VK_1 || key == KeyEvent.VK_2)) {
                        if(key == KeyEvent.VK_1) {
                            app.characterAction();
                        }
                        showMessage = false;
                    }
                    // exit windows game
                    if (key == KeyEvent.VK_ESCAPE){
                        System.exit(0);
                    }
                }
                public void keyReleased(KeyEvent e) {
                    int k = e.getKeyCode();
                    if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP || k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN ||
                        k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT || k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) {
                        isMoving = false;
                        spriteNum = 1;
                    }
                }
            });

            volumeSlider.addChangeListener(e -> {
                if (volumeControl != null) {
                    float min = volumeControl.getMinimum(); // usually -80
                    float max = volumeControl.getMaximum(); // usually 0
                    float value = volumeSlider.getValue() / 100f;
                    float volume = min + (max - min) * value;
                    volumeControl.setValue(volume);
                }
            });

            ////#endregion


            mainFrame.add(gamePanel);

            Timer gameTimer = new Timer(16, e -> {
                updateSprites();
                gamePanel.repaint();
            });
            gameTimer.start();
        });
    }
    //#endregion


    //#region SCREENS FUNCTIONS

    // onboarding screen
    public void OnBoarding(Graphics g, Component c) {
        ImageIcon OnBoardingVideo = new ImageIcon("assets/images/background/onboarding.gif");
        g.drawImage(OnBoardingVideo.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        if(!onboardingTimerStarted) {
            System.out.println("Onboarding Screen...");
            onboardingTimerStarted = true;
            timerDelayed(5000, () -> gameState = 4);
        }
    }

    // main menu screen
    public void MainMenu(Graphics g, Component c) {

        ImageIcon MainMenuBg = new ImageIcon("assets/images/background/main-menu.gif");
        g.drawImage(MainMenuBg.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        g.setFont(getGameFont(24f));
        g.setColor(Color.WHITE);

        g.drawString("press esc to quit", centerX(180), 670);
    }

    // Setting screen
    public void Setting(Graphics g, Component c) {
        ImageIcon LevelScreenBG = new ImageIcon("assets/images/background/main-background.jpg");
        g.drawImage(LevelScreenBG.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);
    }

    // select level screen
    public void LevelScreen(Graphics g, Component c) {
        ImageIcon LevelScreenBg = new ImageIcon("assets/images/background/main-background.jpg");
        g.drawImage(LevelScreenBg.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);
    }

    // Level 1 Loading
    public void level1_loading(Graphics g, Component c) {
        ImageIcon LoadingL1 = new ImageIcon("assets/images/background/Loading L1.gif");
        g.drawImage(LoadingL1.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);
        
        if(!levelLoadingTimerStarted) {
            levelLoadingTimerStarted = true;
            System.out.println("Loading Level 1...");
            timerDelayed(5000, () -> gameState = 1);
        }
    }

    // Level 2 Loading
    public void level2_loading(Graphics g, Component c) {
        ImageIcon LoadingL2 = new ImageIcon("assets/images/background/Loading L2.gif");
        g.drawImage(LoadingL2.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        if(!levelLoadingTimerStarted) {
            levelLoadingTimerStarted = true;
            System.out.println("Loading Level 2...");
            timerDelayed(5000, () -> gameState = 2);
        }
    }

    // Level 3 Loading
    public void level3_loading(Graphics g, Component c) {
        ImageIcon LoadingL3 = new ImageIcon("assets/images/background/Loading L3.gif");
        g.drawImage(LoadingL3.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        if(!levelLoadingTimerStarted) {
            levelLoadingTimerStarted = true;
            System.out.println("Loading Level 3...");
            timerDelayed(5000, () -> gameState = 3);
        }
    }

    // counting down screen
    public void CountingDownScreen(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;

        ImageIcon countingDownBackground = new ImageIcon("assets/images/background/main-background.jpg");
        g2.drawImage(countingDownBackground.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 140));

        String text = String.valueOf(countdownValue);
        FontMetrics fm = g2.getFontMetrics();
        int x = (c.getWidth() - fm.stringWidth(text)) / 2;
        int y = (c.getHeight() + fm.getAscent()) / 2;

        g2.drawString(text, x, y);
    }

    // times up screen
    public void ResultScreen(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;

        if (results.equals("TIME")) {
            ImageIcon timesUpBackground = new ImageIcon("assets/images/background/main-background.jpg");
            g2.drawImage(timesUpBackground.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 100));

            String text = "TIME IS UP!";
            FontMetrics fm = g2.getFontMetrics();
            int x = (c.getWidth() - fm.stringWidth(text)) / 2;
            int y = 300;

            g2.drawString(text, x, y);
        }

        if (results.equals("SUCCESS")) {
            ImageIcon successBackground = new ImageIcon("assets/images/background/main-background.jpg");
            g2.drawImage(successBackground.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());

            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 100));

            String text = "MISSION SUCCESS!";
            FontMetrics fm = g2.getFontMetrics();
            int x = (c.getWidth() - fm.stringWidth(text)) / 2;
            int y = 300;

            g2.drawString(text, x, y);
        }

        if (results.equals("FLOODED")) {
            ImageIcon floodedBackground = new ImageIcon("assets/images/background/main-background.jpg");
            g2.drawImage(floodedBackground.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());

            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 80));

            String text = "GAME OVER! YOU FLOODED";
            FontMetrics fm = g2.getFontMetrics();
            int x = (c.getWidth() - fm.stringWidth(text)) / 2;
            int y = 300;

            g2.drawString(text, x, y);
        }

        
    }

    // level 1 screen
    public void Level1_Screen(Graphics g, Component c) {
    
        if (!level1Inizialized) {
            level1Inizialized = true;
            loadCasesFromTXT("assets/data/case/level1-case.txt");
            totalEvacuation();
            loadMap();
            startCountdown();
        }

        if (isCountingDown) {
            CountingDownScreen(g, c);
            return;
        }

        drawTileMap(g, c);
        drawCharacter(g, playerX, playerY, direction, spriteNum);
        drawEmergencies(g, c);
        drawGameStatus(g, c);

        if (showMessage && !warningMessage) {
            showMessage(g, c, message);
        }
        if (warningMessage) {
            drawWarningMessage(g, c, message);
        }
    }

    // level 2 screen
    public void Level2_Screen(Graphics g, Component c) {
        if (!level2Inizialized) {
            level2Inizialized = true;
            loadCasesFromTXT("assets/data/case/level2-case.txt");
            totalEvacuation();
            loadMap();
            startCountdown();
        }

        if (isCountingDown) {
            CountingDownScreen(g, c);
            return;
        }

        drawTileMap(g, c);
        drawCharacter(g, playerX, playerY, direction, spriteNum);
        drawEmergencies(g, c);
        drawGameStatus(g, c);
        if (showMessage && !warningMessage) {
            showMessage(g, c, message);
        }
        if (warningMessage) {
            drawWarningMessage(g, c, message);
        }
    }

    public void Level3_Screen(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;
        ImageIcon successBackground = new ImageIcon("assets/images/background/main-background.jpg");
        g2.drawImage(successBackground.getImage(), 0, 0, c.getWidth(), c.getHeight(), c);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 100));

        String text = "COMING SOON";
        FontMetrics fm = g2.getFontMetrics();
        int x = (c.getWidth() - fm.stringWidth(text)) / 2;
        int y = 400;

        g2.drawString(text, x, y);
    }

    //#endregion

    //#region TILEMAP & PLAYERS FUNCTIONS

    // load images function
    public void loadTileImages() {
        try {
            // character
            up1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_up_1.png"));
            up2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_up_2.png"));
            down1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_down_1.png"));
            down2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_down_2.png"));
            left1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_left_1.png"));
            left2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_left_2.png"));
            right1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_right_1.png"));
            right2 = javax.imageio.ImageIO.read(new java.io.File("assets/images/character/boy_right_2.png"));

            // tiles
            grass = javax.imageio.ImageIO.read(new java.io.File("assets/images/house/house1-green-up.png"));
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
            earthquakeBlock = javax.imageio.ImageIO.read(new java.io.File("assets/images/interactive/crate.png"));
            house1 = javax.imageio.ImageIO.read(new java.io.File("assets/images/house/house1.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // generate map from text file
    public void loadMap() {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("assets/data/maps/level1-map.txt"));
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

    // generate warning points
    public void generateWarningPoints() {

        int total = Emergency.size();
        warningPoints = new int[total][4];

        int count = 0;

        while (count < total) {

            int col = random.nextInt(map[0].length);
            int row = random.nextInt(map.length);

            if (map[row][col] == '1' && row > 0) {

                warningPoints[count][0] = col * tileSize;
                warningPoints[count][1] = (row - 1) * tileSize;
                warningPoints[count][2] = count;

                count++;
            }
        }
    }


    // draw tile map function
    public void drawTileMap(Graphics g, Component c) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                char tile = map[row][col];
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
                if (tile == 'X') {  
                    g.drawImage(earthquakeBlock, col * tileSize, row * tileSize, tileSize, tileSize, c);
                }
            }
        }
    }

    // draw character function
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

    // spawn character function
    public void spawnCharacter() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col] == '0') {
                    playerX = col * tileSize;
                    playerY = row * tileSize;
                    return;
                }
            }
        }
    }

    // draw emergencies function
    void drawEmergencies(Graphics g, Component c) {
        for (int i = 0; i < warningPoints.length; i++) {

            if(warningPoints[i][3] == 1) continue;
            
            int x = warningPoints[i][0];
            int y = warningPoints[i][1];

            String[] data = Emergency.get(warningPoints[i][2]);
            String type = data[0];

            BufferedImage img = null;
            if (type.equals("GREEN")) {
                img = warningGreen;
            } else if (type.equals("YELLOW")) {
                img = warningYellow;
            } else if (type.equals("RED")) {
                img = warningRed;
            }

            g.drawImage(img, x, y, tileSize, tileSize, c);
        }
    }

    static void startFlooding() {

        if (floodTimer != null) floodTimer.stop();

        floodStep = 0;
        isFlooding = true;

        floodTimer = new Timer(10000, e -> {
            floodMap();
            floodStep++;
        });

        floodTimer.start();
    }

    // start earthquake
    static void startEarthquake() {

        if (earthquakeTimer != null) earthquakeTimer.stop();

        earthquakeTimer = new Timer(10000, e -> {
            earthquakeMap();
        });

        earthquakeTimer.start();
    }


    // earthquake map
    static void earthquakeMap() {

        if (isMapFrozen) return;

        int row, col;
        int attempts = 0;

        do {
            row = random.nextInt(map.length);
            col = random.nextInt(map[0].length);
            attempts++;
            if (attempts > 100) return; // anti infinite loop
        }
        while (
            map[row][col] != '0' ||           // hanya road
            (row == playerY / tileSize &&
            col == playerX / tileSize)       // jangan timpa player
        );

        // ubah road jadi obstacle gempa
        map[row][col] = 'X';
    }

    // flood map
    static void floodMap() {

    if (isMapFrozen) return;

    int rows = map.length;
    int cols = map[0].length;

    int playerRow = playerY / tileSize;
    int playerCol = playerX / tileSize;

    int s = floodStep;

    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {

            boolean isOuter =
                r == s ||
                c == s ||
                r == rows - 1 - s ||
                c == cols - 1 - s;

            if (isOuter && map[r][c] != '2') {
                map[r][c] = '2'; // jadi air

                // === PLAYER KENA BANJIR ===
                if (r == playerRow && c == playerCol) {

                    isMapFrozen = true;

                    if (gameTimeTimer != null) gameTimeTimer.stop();
                    if (floodTimer != null) floodTimer.stop();

                    warningMessage = true;
                    message = "YOU ARE FLOODED!";
                    gameState = 6;
                    results = "FLOODED";
                    return;
                }
            }
        }
    }
}


    //#endregion

    //#region MUSIC FUNCTIONS
    public static void playBGM(String path) {
        try {
            AudioInputStream audioStream =
            AudioSystem.getAudioInputStream(new File(path));

            menuBGM = AudioSystem.getClip();
            menuBGM.open(audioStream);

            volumeControl = (FloatControl)
            menuBGM.getControl(FloatControl.Type.MASTER_GAIN);

            menuBGM.loop(Clip.LOOP_CONTINUOUSLY);
            menuBGM.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void stopBGM() {
        if (menuBGM != null && menuBGM.isRunning()) {
            menuBGM.stop();
            menuBGM.close();
        }
    }
    //#endregion

    //#region TIMER FUNCTIONS

    // format timer function
    static String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    // gamer timer function
    static void startGameTimer() {

    if (gameTimeTimer != null) gameTimeTimer.stop();
    if (floodTimer != null) floodTimer.stop();
    if (earthquakeTimer != null) earthquakeTimer.stop();

    timeLeft = timeLimitSeconds;

    // reset flood state
    isFlooding = false;
    isMapFrozen = false;
    floodStep = 0;

    gameTimeTimer = new Timer(1000, e -> {
        timeLeft--;

        // === LEVEL 1 - FLOOD ===
        if (timeLeft == 70 && !isFlooding && gameState == 1) {
            isFlooding = true;
            startFlooding();
        }

        if (timeLeft <= 60 && gameState == 1) {
            warningMessage = true;
            message = "FLOOD INCOMING";
        }

        // === LEVEL 2 - EARTHQUAKE ===
        if (timeLeft == 60 && gameState == 2) {
            warningMessage = true;
            message = "EARTHQUAKE! ROADS ARE COLLAPSING!";
            startEarthquake();
        }


        // === TIME UP ===
        if (timeLeft <= 0) {
            timeLeft = 0;
            isMapFrozen = true;

            gameTimeTimer.stop();
            if (floodTimer != null) floodTimer.stop();
            if (earthquakeTimer != null) earthquakeTimer.stop();

            warningMessage = true;
            message = "TIME IS UP!";
            gameState = 6;
            results = "TIME";
        }
    });

    gameTimeTimer.start();
}

    // delayed timer function
    public static void timerDelayed(int delayMs, Runnable action) {
        Timer t = new Timer(delayMs, e -> {
            action.run();
            ((Timer) e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    // countdown timer function
    public static void startCountdown() {
        countdownValue = 3;
        isCountingDown = true;
        stopBGM();

        if (countdownTimer != null) {
            countdownTimer.stop();
        }

        countdownTimer = new Timer(1000, e -> {
            countdownValue--;

            if (countdownValue <= 0) {
                countdownTimer.stop();
                isCountingDown = false;
                startGameTimer(); 
                playBGM("assets/sounds/BGMmenu.wav");
            }
        });

        countdownTimer.start();
    }
    //#endregion

    //#region MESSAGE FUNCTIONS
    public void showMessage(Graphics g, Component c, String message) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int margin = 20;          
        int boxHeight = 150;       
        int rounded = 20;             

        int boxX = margin;
        int boxY = c.getHeight() - boxHeight - margin - 35;
        int boxW = c.getWidth() - (margin * 2);
        int boxH = boxHeight;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(boxX, boxY, boxW, boxH, rounded, rounded);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, rounded, rounded);

        int paddingUp = 40;
        int paddingLeft = 20;
        boxX += paddingLeft;
        boxY += paddingUp;

        g2.setFont(getGameFont(24f));

        int textX = boxX;
        int textY = boxY;

        g2.drawString(message, textX, textY);
        g2.drawString("1. Take or Save", textX, textY + 50);
        g2.drawString("2. Close", textX, textY + 75);
    }

    // warning message function
    public void drawWarningMessage(Graphics g, Component c, String message) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(getGameFont(32f));
        g2.setColor(Color.RED);

        g2.drawString(message, 20, 80);
    }

    // timer and backpack status game function
    public void drawGameStatus(Graphics g, Component c) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(getGameFont(24f));
        g2.setColor(Color.WHITE);

        String timeStatus = "TIME: " + formatTime(timeLeft);
        g2.drawString(timeStatus, 20, 35);

        String backpackStatus = "BACKPACK SIZE: " + backpack + "/" + backpackValue.length;
        g2.drawString(backpackStatus, 150, 35);

        String evacuationStatus= "EVACUATED: " + evacuated+ "/" + totalEvacuated;
        g2.drawString(evacuationStatus, 350, 35);
    }

    // interact action
    public void cekPoint() {
        int playerCol = playerX / tileSize;
        int playerRow = playerY / tileSize;

        for (int i = 0; i < warningPoints.length; i++) {

            if (warningPoints[i][3] == 1) continue;

            int warningCenterX = warningPoints[i][0] + tileSize / 2;
            int warningCenterY = warningPoints[i][1] + tileSize / 2;

            int warningCol = warningCenterX / tileSize;
            int warningRow = (warningCenterY / tileSize) + 1;

            int diffCol = Math.abs(playerCol - warningCol);
            int diffRow = Math.abs(playerRow - warningRow);
            if ((diffCol <= 1 && diffRow <= 1)) {
                showMessage = true;

                currentEmergencyIndex = warningPoints[i][2];
                String[] data = Emergency.get(warningPoints[i][2]);

                message = data[2];
                return;
            }
        }
    }

    // character action function
    public void characterAction() {
        if (currentEmergencyIndex == -1) return;

        String[] data = Emergency.get(currentEmergencyIndex);
        String type = data[0];
        String itemRewards = data[3];
        String requiredItem = data[4];

        if (type.equals("GREEN")){
            if (backpack < backpackValue.length) {
                backpack += 1;
                backpackValue[backpack - 1] = warningPoints[currentEmergencyIndex][2];
                backpackItems[backpack - 1] = itemRewards;
                warningPoints[currentEmergencyIndex][3] = 1;
                message = "YOU GOT " + itemRewards + "!";
                warningMessage = true;
            } else {
                message = "BACKPACK IS FULL! PLEASE DELIVER THE ITEMS FIRST.";
                warningMessage = true;
            }
        }

        if (type.equals("YELLOW")) {
            warningPoints[currentEmergencyIndex][3] = 1;
            evacuated += 1;
            message = "EMERGENCY EVACUATED!";
            warningMessage = true;
        }
        
        if (type.equals("RED")) {
            boolean success = false;
        
            if (requiredItem == null || requiredItem.equals("none")) {
                success = true;
            } 
        
            else {
                int foundIndex = -1;
                
                for (int j = 0; j < backpackItems.length; j++) {
                    if (backpackItems[j] != null && backpackItems[j].equals(requiredItem)) {
                        foundIndex = j;
                        break;
                    }
                }

            if (foundIndex != -1) {
                
                backpackItems[foundIndex] = null;
                backpackValue[foundIndex] = 0;
                
                for(int k = foundIndex; k < backpack - 1; k++){
                    backpackItems[k] = backpackItems[k+1];
                    backpackValue[k] = backpackValue[k+1];
                }
                backpackItems[backpack-1] = null;
                backpackValue[backpack-1] = 0;

                backpack -= 1;
                success = true;
            } else {
                message = "REQUIRED " + requiredItem + " NOT FOUND!";
                warningMessage = true;
            }
        }

        if (success) {
            warningPoints[currentEmergencyIndex][3] = 1;
            evacuated += 1;
            message = "EMERGENCY EVACUATED!";
            warningMessage = true;
            }
        }

        if (evacuated >= totalEvacuated) {
            if (gameTimeTimer != null) gameTimeTimer.stop(); // Stop Timer
            warningMessage = true;
            message = "ALL EMERGENCIES EVACUATED! WELL DONE!";
            if (unlockedLevels == 1) {
                unlockedLevels += 1;
                saveUserData();
            }
            gameState = 6;
            results = "SUCCESS";
        }

        currentEmergencyIndex = -1;
        showMessage = false;
    }
    // endregion

    //#region USER DATA FUNCTIONS

    static void loadUserData() {
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("assets/data/user/user-data.txt"));
            String line = reader.readLine();
            if (line != null && line.startsWith("unlocked_levels=")) {
                unlockedLevels = Integer.parseInt(line.substring(16));
            }
            reader.close();
        } catch (Exception e) {
            unlockedLevels = 1; // default
        }
    }

    static void saveUserData() {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter("assets/data/user/user-data.txt");
            writer.println("unlocked_levels=" + unlockedLevels);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////#endregion

    //#region UTILITY FUNCTIONS

    // centering function
    public static int centerX(int width) {
        return (frameWidth - width) / 2;
    }

    static void loadCasesFromTXT(String path) {
        Emergency.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            String[] current = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) continue;

                if (line.equals("---")) {
                    if (current != null) {
                        Emergency.add(current);
                    }
                    current = new String[5];
                    continue;
                }

                if (current == null) continue;

                if (line.startsWith("TYPE=")) {
                    current[0] = line.substring(5);
                }
                else if (line.startsWith("TITLE=")) {
                    current[1] = line.substring(6);
                }
                else if (line.startsWith("DESCRIPTION=")) {
                    current[2] = line.substring(12);
                }
                else if (line.startsWith("ITEM_REWARD=")) {
                    current[3] = line.substring(12);
                }
                else if (line.startsWith("REQUIRED_ITEM=")) {
                    current[4] = line.substring(14);
                }
            }

            if (current != null) {
                Emergency.add(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Font getGameFont(float size) {
        try {
            if (gameFont == null) {
                gameFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    new File("assets/fonts/VT323-Regular.ttf")
                );
            }
            return gameFont.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, (int) size);
        }
    }

    public static boolean canMove(int x, int y) {
        int left = x;
        int right = x + tileSize - 1;
        int top = y;
        int bottom = y + tileSize - 1;
        return isWalkable(left, top) && isWalkable(right, top) && isWalkable(left, bottom) && isWalkable(right, bottom);
    }

    public static boolean isWalkable(int x, int y) {
        int col = x / tileSize;
        int row = y / tileSize;
        if (col < 0 || col >= map[0].length || row < 0 || row >= map.length) {
            return false;
        }
        return map[row][col] == '0';
    }

    public static void updateSprites() {
        if (isMoving) {
            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        } else {
            spriteNum = 1;
        }
    }

    // image button creation function
    public static JButton createImageButton(
        String imagePath,
        int x, int y, int w, int h,
        Runnable onClick,
        JPanel panel
    ) {
        ImageIcon img = new ImageIcon(imagePath);
        JButton btn = new JButton(img);

        btn.setBounds(x, y, w, h);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);

        btn.addActionListener(e -> onClick.run());

        panel.add(btn);
        return btn;
    }

    public static void totalEvacuation() {
        totalEvacuated = 0;

        for (String[] caseData : Emergency) {
            if ("RED".equals(caseData[0]) || "YELLOW".equals(caseData[0])) {
                totalEvacuated++;
            }
        }
    }

    static void refreshLevelUI() {

    // LEVEL 2
    if (unlockedLevels >= 2) {
        level_2.setIcon(new ImageIcon(
            "assets/images/buttons/level2-button.png"
        ));
    } else {
        level_2.setIcon(new ImageIcon(
            "assets/images/buttons/level2-locked.png"
        ));
    }

    // LEVEL 3
    if (unlockedLevels >= 3) {
        level_3.setIcon(new ImageIcon(
            "assets/images/buttons/level3-button.png"
        ));
    } else {
        level_3.setIcon(new ImageIcon(
            "assets/images/buttons/level3-locked.png"
        ));
    }
}


    public void resetGame() {

    if (gameTimeTimer != null) gameTimeTimer.stop();
    if (floodTimer != null) floodTimer.stop();
    if (earthquakeTimer != null) earthquakeTimer.stop();
    if (countdownTimer != null) countdownTimer.stop();

    // reset flood
    isFlooding = false;
    isMapFrozen = false;
    floodStep = 0;

    // reset gameplay
    timeLeft = timeLimitSeconds;
    backpack = 0;
    evacuated = 0;
    currentEmergencyIndex = -1;

    for (int i = 0; i < backpackValue.length; i++) {
        backpackValue[i] = 0;
        backpackItems[i] = null;
    }

    warningMessage = false;
    showMessage = false;
    message = "";
    results = "";

    // IMPORTANT: reload map
    loadMap();
    spawnCharacter();
}

    //#endregion


}
