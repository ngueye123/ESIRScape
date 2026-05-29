package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import entity.Boss;
import entity.BugEnemy;
import entity.Enemy;
import entity.ExamenEnemy;
import entity.Player;
import powerup.CoffeeUp;
import powerup.PowerUp;
import powerup.UsbUp;
import projectile.Projectile;
import tile.TileManager;

public class GamePanel extends JPanel implements Runnable {

    public static final int TILE_SIZE      = 48;
    public static final int MAX_SCREEN_COL = 16;
    public static final int MAX_SCREEN_ROW = 12;
    public static final int SCREEN_WIDTH   = TILE_SIZE * MAX_SCREEN_COL;
    public static final int SCREEN_HEIGHT  = TILE_SIZE * MAX_SCREEN_ROW;

    private static final int FPS = 60;

    // === ÉTATS DU JEU ===
    public String gameState = "CHAR_SELECT";
    public int currentLevel = 1;
    public int score = 0;

    // Choix de personnage : 0 = Étudiant, 1 = Ninja
    public int selectedChar = 0;

    public int tempsRestant  = 180;
    private int timerCounter = 0;

    public int bossTimer    = 20; // 20 secondes pour le boss (serveur explose!)
    private int bossTimerCounter = 0;

    private int introTimer = 0;
    private static final int INTRO_DURATION = 200;

    public KeyHandler keyH;
    private Thread gameThread;
    public Player player;
    public TileManager tileManager;

    public ArrayList<Projectile> projectiles;
    public ArrayList<Enemy> enemies;
    public ArrayList<PowerUp> powerUps;

    public Boss boss;
    public boolean bossSpawned = false;

    private boolean nearDoor = false;
    private boolean doorUsed = false;

    // Power-ups conservés entre les niveaux
    private boolean savedUsbUpgraded = false;
    private int savedCoffeeLevel = 0;
    private int savedSpeed = 4;

    // Particules visuelles
    private ArrayList<int[]> particles = new ArrayList<>();

    // Animation d'explosion serveur
    private boolean serverExploding = false;
    private int explosionTimer = 0;
    private ArrayList<int[]> explosionParticles = new ArrayList<>();

    // Animation sélection de personnage
    private int charSelectAnim = 0;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);

        keyH = new KeyHandler();
        this.addKeyListener(keyH);
        this.setFocusable(true);

        player      = new Player(this, keyH);
        tileManager = new TileManager(this);
        projectiles = new ArrayList<>();
        enemies     = new ArrayList<>();
        powerUps    = new ArrayList<>();
    }

    public void initLevel(int level) {
        currentLevel = level;
        projectiles.clear();
        enemies.clear();
        powerUps.clear();
        particles.clear();
        bossSpawned = false;
        boss        = null;
        doorUsed    = false;
        nearDoor    = false;
        serverExploding = false;
        explosionTimer  = 0;
        explosionParticles.clear();

        player.x = 80;
        player.y = 250;

        // Conserver les power-ups entre niveaux
        if (level > 1) {
            player.usbUpgraded = savedUsbUpgraded;
            player.coffeeLevel = savedCoffeeLevel;
            player.speed       = savedSpeed;
        }

        if (level == 1) {
            tileManager.loadMap("/maps/map1.txt");
            // Niveau 1 : ennemis LENTS (speed réduit)
            enemies.add(new BugEnemy(350, 150, this, 1));   // speed=1 au lieu de 2
            enemies.add(new BugEnemy(550, 100, this, 1));
            enemies.add(new BugEnemy(600, 350, this, 1));
            enemies.add(new ExamenEnemy(420, 300, this, 1)); // speed=1 au lieu de 3
            enemies.add(new ExamenEnemy(200, 400, this, 1));
            enemies.add(new ExamenEnemy(500, 420, this, 1));
        } else if (level == 2) {
            tileManager.loadMap("/maps/map2.txt");
            // Niveau 2 : ennemis un peu plus rapides (speed=2)
            enemies.add(new BugEnemy(300, 130, this, 2));
            enemies.add(new BugEnemy(500, 80,  this, 2));
            enemies.add(new BugEnemy(650, 300, this, 2));
            enemies.add(new BugEnemy(400, 400, this, 2));
            enemies.add(new ExamenEnemy(450, 280, this, 2));
            enemies.add(new ExamenEnemy(220, 380, this, 2));
            enemies.add(new ExamenEnemy(530, 380, this, 2));
        } else if (level == 3) {
            tileManager.loadMap("/maps/map2.txt");
            boss = new Boss(SCREEN_WIDTH / 2 - TILE_SIZE, SCREEN_HEIGHT / 2, this);
            bossSpawned = true;
            bossTimer = 20;
            bossTimerCounter = 0;
        }
    }

    /** Sauvegarde les power-ups avant de passer au niveau suivant */
    private void savePowerUps() {
        savedUsbUpgraded = player.usbUpgraded;
        savedCoffeeLevel = player.coffeeLevel;
        savedSpeed       = player.speed;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();
            try {
                double remaining = (nextDrawTime - System.nanoTime()) / 1_000_000;
                if (remaining < 0) remaining = 0;
                Thread.sleep((long) remaining);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void update() {

        charSelectAnim++;

        // ===================== CHAR_SELECT =====================
        if (gameState.equals("CHAR_SELECT")) {
            if (keyH.leftPressed || keyH.rightPressed) {
                selectedChar = (selectedChar == 0) ? 1 : 0;
                // Consume key press to avoid rapid toggle
                keyH.leftPressed = false;
                keyH.rightPressed = false;
            }
            if (keyH.enterPressed) {
                keyH.enterPressed = false;
                gameState    = "PLAYING";
                score        = 0;
                tempsRestant = 180;
                timerCounter = 0;
                savedUsbUpgraded = false;
                savedCoffeeLevel = 0;
                savedSpeed       = 4;
                player = new Player(this, keyH);
                player.characterType = selectedChar;
                initLevel(1);
            }
            if (keyH.escapePressed) System.exit(0);
        }

        // ===================== MENU (legacy) =====================
        if (gameState.equals("MENU")) {
            if (keyH.enterPressed) {
                gameState = "CHAR_SELECT";
                keyH.enterPressed = false;
            }
            if (keyH.escapePressed) System.exit(0);
        }

        // ===================== PLAYING (Niveau 1 ou 2) =====================
        if (gameState.equals("PLAYING")) {
            player.update();
            timerCounter++;
            if (timerCounter >= 60) { timerCounter = 0; tempsRestant--; }
            if (tempsRestant <= 0) gameState = "GAME_OVER";

            updateProjectiles();
            updateEnemies();
            updatePowerUps();
            updateParticles();
            checkDoor();

            if (player.hp <= 0) gameState = "GAME_OVER";
        }

        // ===================== LEVEL2_INTRO =====================
        if (gameState.equals("LEVEL2_INTRO")) {
            introTimer++;
            if (introTimer >= INTRO_DURATION || keyH.enterPressed) {
                keyH.enterPressed = false;
                gameState  = "PLAYING";
                introTimer = 0;
                initLevel(2);
            }
        }

        // ===================== BOSS_INTRO =====================
        if (gameState.equals("BOSS_INTRO")) {
            introTimer++;
            if (introTimer >= INTRO_DURATION || keyH.enterPressed) {
                keyH.enterPressed = false;
                gameState  = "BOSS_FIGHT";
                introTimer = 0;
                initLevel(3);
            }
        }

        // ===================== BOSS_FIGHT =====================
        if (gameState.equals("BOSS_FIGHT")) {
            player.update();

            if (!serverExploding) {
                bossTimerCounter++;
                if (bossTimerCounter >= 60) { bossTimerCounter = 0; bossTimer--; }
                if (bossTimer <= 0) {
                    // Serveur explose !
                    serverExploding = true;
                    explosionTimer  = 0;
                    triggerServerExplosion();
                    SoundManager.playBossSpawn(); // son dramatique
                }
            } else {
                explosionTimer++;
                updateExplosionParticles();
                if (explosionTimer > 120) {
                    gameState = "GAME_OVER";
                }
            }

            updateProjectiles();
            updateEnemies();
            updatePowerUps();
            updateParticles();

            if (boss != null) {
                boss.update();
                for (Projectile p : new ArrayList<>(projectiles)) {
                    if (!p.isEnnemi()) {
                        if (Math.abs(p.getX() - boss.x) < TILE_SIZE * 2 &&
                            Math.abs(p.getY() - boss.y) < TILE_SIZE * 2 &&
                            !boss.beaten) {
                            boss.takeDamage(p.getDegats());
                            spawnParticles(p.getX(), p.getY(), Color.ORANGE, 6);
                            projectiles.remove(p);
                            score += 10;
                        }
                    }
                }
                if (boss.isServerReached()) {
                    score += 500;
                    gameState = "WIN";
                }
            }
            if (player.hp <= 0) gameState = "GAME_OVER";
        }

        // ===================== GAME_OVER =====================
        if (gameState.equals("GAME_OVER")) {
            if (keyH.enterPressed) {
                keyH.enterPressed = false;
                gameState = "CHAR_SELECT";
            }
        }

        // ===================== WIN =====================
        if (gameState.equals("WIN")) {
            if (keyH.enterPressed) {
                keyH.enterPressed = false;
                gameState = "CHAR_SELECT";
            }
        }
    }

    // ── Tir intelligent : toujours cibler l'ennemi le plus proche ─────────────
    public int[] getNearestEnemyDirection(int fromX, int fromY) {
        if (enemies.isEmpty() && boss == null) return new int[]{keyH.lastDirX, keyH.lastDirY};
        double bestDist = Double.MAX_VALUE;
        int targetX = fromX + keyH.lastDirX * 100;
        int targetY = fromY + keyH.lastDirY * 100;

        for (Enemy e : enemies) {
            double d = Math.sqrt(Math.pow(e.x - fromX, 2) + Math.pow(e.y - fromY, 2));
            if (d < bestDist) { bestDist = d; targetX = e.x + TILE_SIZE/2; targetY = e.y + TILE_SIZE/2; }
        }
        if (boss != null && !boss.beaten) {
            double d = Math.sqrt(Math.pow(boss.x - fromX, 2) + Math.pow(boss.y - fromY, 2));
            if (d < bestDist) { targetX = boss.x + TILE_SIZE; targetY = boss.y + TILE_SIZE; }
        }

        double dx = targetX - fromX;
        double dy = targetY - fromY;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist == 0) return new int[]{1, 0};
        return new int[]{(int)Math.round(dx/dist), (int)Math.round(dy/dist)};
    }

    public double[] getNearestEnemyDirectionFloat(int fromX, int fromY) {
        if (enemies.isEmpty() && boss == null) return new double[]{keyH.lastDirX, keyH.lastDirY};
        double bestDist = Double.MAX_VALUE;
        double targetX = fromX + keyH.lastDirX * 100;
        double targetY = fromY + keyH.lastDirY * 100;

        for (Enemy e : enemies) {
            double d = Math.sqrt(Math.pow(e.x - fromX, 2) + Math.pow(e.y - fromY, 2));
            if (d < bestDist) { bestDist = d; targetX = e.x + TILE_SIZE/2; targetY = e.y + TILE_SIZE/2; }
        }
        if (boss != null && !boss.beaten) {
            double d = Math.sqrt(Math.pow(boss.x - fromX, 2) + Math.pow(boss.y - fromY, 2));
            if (d < bestDist) { targetX = boss.x + TILE_SIZE; targetY = boss.y + TILE_SIZE; }
        }

        double dx = targetX - fromX;
        double dy = targetY - fromY;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist == 0) return new double[]{1, 0};
        return new double[]{dx/dist, dy/dist};
    }

    private void updateProjectiles() {
        ArrayList<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : projectiles) {
            p.update();
            if (p.isOutOfScreen()) { toRemove.add(p); continue; }
            if (p.isEnnemi()) {
                if (Math.abs(p.getX() - player.x) < TILE_SIZE &&
                    Math.abs(p.getY() - player.y) < TILE_SIZE) {
                    player.hp -= p.getDegats();
                    spawnParticles(p.getX(), p.getY(), Color.RED, 4);
                    SoundManager.playPlayerHit();
                    toRemove.add(p);
                }
            } else {
                for (Enemy e : enemies) {
                    if (Math.abs(p.getX() - e.x) < TILE_SIZE &&
                        Math.abs(p.getY() - e.y) < TILE_SIZE) {
                        e.hp -= p.getDegats();
                        spawnParticles(p.getX(), p.getY(), Color.YELLOW, 4);
                        toRemove.add(p);
                        break;
                    }
                }
            }
        }
        projectiles.removeAll(toRemove);
    }

    private void updateEnemies() {
        ArrayList<Enemy> toRemove = new ArrayList<>();
        for (Enemy e : enemies) {
            e.update();
            if (e.isDead()) {
                toRemove.add(e);
                score += e.getScoreValue();
                spawnParticles(e.x + TILE_SIZE/2, e.y + TILE_SIZE/2, Color.GREEN, 10);
                dropPowerUp(e.x, e.y);
            }
        }
        enemies.removeAll(toRemove);
    }

    private void updatePowerUps() {
        ArrayList<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp pu : powerUps) {
            pu.update();
            if (pu.isCollected()) toRemove.add(pu);
        }
        powerUps.removeAll(toRemove);
    }

    public void spawnParticles(int x, int y, Color c, int count) {
        for (int i = 0; i < count; i++) {
            int vx = (int)((Math.random() * 6) - 3);
            int vy = (int)((Math.random() * 6) - 3);
            particles.add(new int[]{x, y, vx, vy, 20, c.getRed(), c.getGreen(), c.getBlue()});
        }
    }

    private void triggerServerExplosion() {
        for (int i = 0; i < 80; i++) {
            int vx = (int)((Math.random() * 20) - 10);
            int vy = (int)((Math.random() * 20) - 10);
            int life = 40 + (int)(Math.random() * 40);
            Color c = Math.random() < 0.5 ? Color.ORANGE : (Math.random() < 0.5 ? Color.RED : Color.YELLOW);
            explosionParticles.add(new int[]{SCREEN_WIDTH/2, SCREEN_HEIGHT/2, vx, vy, life, c.getRed(), c.getGreen(), c.getBlue()});
        }
    }

    private void updateExplosionParticles() {
        ArrayList<int[]> toRemove = new ArrayList<>();
        for (int[] p : explosionParticles) {
            p[0] += p[2]; p[1] += p[3]; p[4]--;
            p[2] = (int)(p[2] * 0.95); p[3] = (int)(p[3] * 0.95) + 1; // gravité
            if (p[4] <= 0) toRemove.add(p);
        }
        explosionParticles.removeAll(toRemove);
    }

    private void updateParticles() {
        ArrayList<int[]> toRemove = new ArrayList<>();
        for (int[] p : particles) {
            p[0] += p[2]; p[1] += p[3]; p[4]--;
            if (p[4] <= 0) toRemove.add(p);
        }
        particles.removeAll(toRemove);
    }

    private void checkDoor() {
        if (doorUsed) return;
        int pCol = (player.x + TILE_SIZE/2) / TILE_SIZE;
        int pRow = (player.y + TILE_SIZE/2) / TILE_SIZE;
        boolean near = false;
        for (int dc = -1; dc <= 1 && !near; dc++)
            for (int dr = -1; dr <= 1; dr++)
                if (tileManager.getTileType(pCol + dc, pRow + dr) == 5) { near = true; break; }
        nearDoor = near;
        if (near && keyH.ePressed) {
            if (!enemies.isEmpty()) return; // doit éliminer tous les ennemis d'abord
            savePowerUps();
            doorUsed  = true;
            introTimer = 0;
            keyH.ePressed = false;
            if (currentLevel == 1) {
                gameState = "LEVEL2_INTRO";
            } else if (currentLevel == 2) {
                gameState = "BOSS_INTRO";
            }
        }
    }

    private void dropPowerUp(int x, int y) {
        double r = Math.random();
        if (r < 0.3)      powerUps.add(new CoffeeUp(x, y, this));
        else if (r < 0.5) powerUps.add(new UsbUp(x, y, this));
    }

    // ════════════════════════════════════════════
    //  RENDU
    // ════════════════════════════════════════════
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        switch (gameState) {
            case "CHAR_SELECT":  drawCharSelect(g2);   break;
            case "MENU":         drawMenu(g2);          break;
            case "PLAYING":      drawGame(g2);          break;
            case "LEVEL2_INTRO": drawLevel2Intro(g2);   break;
            case "BOSS_INTRO":   drawBossIntro(g2);     break;
            case "BOSS_FIGHT":   drawBossFight(g2);     break;
            case "GAME_OVER":    drawGameOver(g2);      break;
            case "WIN":          drawWin(g2);           break;
        }
        g2.dispose();
    }

    // ── Écran de sélection de personnage ─────────────────────────────────────
    private void drawCharSelect(Graphics2D g2) {
        // Fond dégradé sombre
        for (int i = 0; i < SCREEN_HEIGHT; i++) {
            float t = (float)i / SCREEN_HEIGHT;
            g2.setColor(new Color((int)(10 + 20*t), (int)(5 + 10*t), (int)(30 + 40*t)));
            g2.fillRect(0, i, SCREEN_WIDTH, 1);
        }

        // Titre
        g2.setColor(new Color(255, 220, 50));
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        drawCentered(g2, "CHOISISSEZ VOTRE HÉROS", 55);

        g2.setColor(new Color(180, 180, 255));
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        drawCentered(g2, "← → pour choisir   ENTRÉE pour confirmer", 80);

        int bob = (int)(Math.sin(charSelectAnim * 0.08) * 5);

        // ── Perso 0 : Étudiant ──
        int c0x = SCREEN_WIDTH / 4 - TILE_SIZE / 2;
        int c0y = SCREEN_HEIGHT / 2 - 60 + bob;
        boolean sel0 = selectedChar == 0;

        if (sel0) {
            g2.setColor(new Color(255, 220, 50, 60));
            g2.fillRoundRect(c0x - 30, c0y - 20, 140, 200, 20, 20);
            g2.setColor(new Color(255, 220, 50));
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRoundRect(c0x - 30, c0y - 20, 140, 200, 20, 20);
            g2.setStroke(new java.awt.BasicStroke(1));
        }

        drawStudentCharPreview(g2, c0x, c0y);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(sel0 ? new Color(255, 220, 50) : new Color(150, 150, 150));
        drawCenteredAt(g2, "ÉTUDIANT", c0x + 40, c0y + 140);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(180, 220, 255));
        drawCenteredAt(g2, "Clé USB & println()", c0x + 40, c0y + 160);
        drawCenteredAt(g2, "Polyvalent", c0x + 40, c0y + 176);

        // ── Perso 1 : Ninja ──
        int c1x = SCREEN_WIDTH * 3 / 4 - TILE_SIZE / 2;
        int c1y = SCREEN_HEIGHT / 2 - 60 + (selectedChar == 1 ? bob : 0);
        boolean sel1 = selectedChar == 1;

        if (sel1) {
            g2.setColor(new Color(150, 50, 255, 60));
            g2.fillRoundRect(c1x - 30, c1y - 20, 140, 200, 20, 20);
            g2.setColor(new Color(180, 80, 255));
            g2.setStroke(new java.awt.BasicStroke(3));
            g2.drawRoundRect(c1x - 30, c1y - 20, 140, 200, 20, 20);
            g2.setStroke(new java.awt.BasicStroke(1));
        }

        drawNinjaCharPreview(g2, c1x, c1y);

        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(sel1 ? new Color(180, 80, 255) : new Color(150, 150, 150));
        drawCenteredAt(g2, "NINJA", c1x + 40, c1y + 140);

        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(220, 180, 255));
        drawCenteredAt(g2, "Épée Laser", c1x + 40, c1y + 160);
        drawCenteredAt(g2, "Frappe rapide", c1x + 40, c1y + 176);

        // Flèche de sélection
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(255, 220, 50));
        int arrowX = sel0 ? c0x + 40 : c1x + 40;
        int arrowY = sel0 ? c0y - 30 : c1y - 30;
        drawCenteredAt(g2, "▼", arrowX, arrowY);

        // Contrôles bas
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(200, 200, 200));
        drawCentered(g2, "ÉCHAP — Quitter", SCREEN_HEIGHT - 15);
    }

    private void drawStudentCharPreview(Graphics2D g2, int x, int y) {
        int ts = TILE_SIZE;
        // Corps
        g2.setColor(new Color(30, 80, 180));
        g2.fillRect(x + 10, y + 30, 28, 30);
        // Tête
        g2.setColor(new Color(255, 210, 170));
        g2.fillRoundRect(x + 12, y + 8, 24, 24, 8, 8);
        // Cheveux
        g2.setColor(new Color(80, 40, 10));
        g2.fillRect(x + 12, y + 8, 24, 8);
        // Yeux
        g2.setColor(Color.WHITE);
        g2.fillOval(x + 15, y + 16, 7, 6);
        g2.fillOval(x + 25, y + 16, 7, 6);
        g2.setColor(new Color(50, 50, 200));
        g2.fillOval(x + 17, y + 17, 4, 4);
        g2.fillOval(x + 27, y + 17, 4, 4);
        // Jambes
        g2.setColor(new Color(50, 90, 160));
        g2.fillRect(x + 12, y + 58, 11, 16);
        g2.fillRect(x + 25, y + 58, 11, 16);
        // Chaussures
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(x + 10, y + 72, 14, 5);
        g2.fillRect(x + 24, y + 72, 14, 5);
        // Sac
        g2.setColor(new Color(160, 100, 30));
        g2.fillRect(x + 2, y + 28, 10, 22);
        // Clé USB
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(x + 38, y + 28, 16, 8);
        g2.setColor(Color.GRAY);
        g2.fillRect(x + 50, y + 30, 6, 4);
    }

    private void drawNinjaCharPreview(Graphics2D g2, int x, int y) {
        // Teint noir clair (gris-beige foncé)
        Color skin = new Color(160, 120, 90);
        // Corps - combinaison sombre
        g2.setColor(new Color(30, 20, 40));
        g2.fillRect(x + 8, y + 28, 32, 32);
        // Ceinture
        g2.setColor(new Color(80, 0, 80));
        g2.fillRect(x + 8, y + 44, 32, 6);
        // Tête
        g2.setColor(skin);
        g2.fillRoundRect(x + 10, y + 6, 28, 26, 10, 10);
        // Masque ninja partiel
        g2.setColor(new Color(20, 10, 30));
        g2.fillRect(x + 10, y + 6, 28, 10);
        g2.fillRect(x + 10, y + 20, 28, 10);
        // Yeux violets lumineux
        g2.setColor(new Color(180, 80, 255));
        g2.fillOval(x + 13, y + 14, 8, 7);
        g2.fillOval(x + 27, y + 14, 8, 7);
        g2.setColor(new Color(220, 160, 255));
        g2.fillOval(x + 15, y + 15, 4, 4);
        g2.fillOval(x + 29, y + 15, 4, 4);
        // Jambes
        g2.setColor(new Color(20, 15, 30));
        g2.fillRect(x + 10, y + 58, 12, 18);
        g2.fillRect(x + 26, y + 58, 12, 18);
        // Chaussures sombres
        g2.setColor(new Color(25, 15, 35));
        g2.fillRect(x + 8, y + 72, 16, 6);
        g2.fillRect(x + 24, y + 72, 16, 6);
        // ÉPÉE LASER (longue, violette/cyan)
        drawLaserSword(g2, x + 42, y + 10, 70, true);
    }

    public void drawLaserSword(Graphics2D g2, int sx, int sy, int length, boolean vertical) {
        // Poignée
        g2.setColor(new Color(80, 60, 100));
        if (vertical) g2.fillRect(sx - 3, sy + length - 14, 6, 14);
        else          g2.fillRect(sx, sy - 3, 14, 6);

        // Garde
        g2.setColor(new Color(120, 0, 160));
        if (vertical) g2.fillRect(sx - 6, sy + length - 16, 12, 4);
        else          g2.fillRect(sx + 12, sy - 6, 4, 12);

        // Lame (dégradé violet→cyan)
        for (int i = 0; i < length - 16; i++) {
            float t = (float)i / (length - 16);
            int r = (int)(180 * (1-t) + 0 * t);
            int gv = (int)(0 * (1-t) + 220 * t);
            int b = (int)(255 * (1-t) + 255 * t);
            g2.setColor(new Color(r, gv, b, 220));
            if (vertical) {
                g2.fillRect(sx - 2, sy + i, 4, 1);
                if (i % 6 == 0) {
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.fillRect(sx - 1, sy + i, 2, 1);
                }
            } else {
                g2.fillRect(sx + i, sy - 2, 1, 4);
            }
        }
        // Pointe brillante
        g2.setColor(new Color(200, 255, 255, 200));
        if (vertical) g2.fillOval(sx - 2, sy, 4, 4);
        else          g2.fillOval(sx + length - 16, sy - 2, 4, 4);
    }

    private void drawGame(Graphics2D g2) {
        if (currentLevel == 1) {
            drawSchoolBackground(g2);
        } else {
            drawLabBackground(g2);
        }
        tileManager.draw(g2);
        for (PowerUp pu : powerUps) pu.draw(g2);
        for (Enemy e : enemies) e.draw(g2);
        for (Projectile p : projectiles) p.draw(g2);
        drawParticles(g2);
        player.draw(g2);
        drawHUD(g2);

        if (nearDoor && !doorUsed) {
            g2.setColor(new Color(255, 255, 0, 220));
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            String msg = enemies.isEmpty() ? "Appuie sur [E] pour passer au niveau suivant !"
                                           : "Élimine tous les ennemis d'abord !";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, SCREEN_WIDTH/2 - w/2, SCREEN_HEIGHT - 20);
        }
    }

    private void drawBossFight(Graphics2D g2) {
        drawServerRoomBackground(g2);
        tileManager.draw(g2);
        for (PowerUp pu : powerUps) pu.draw(g2);
        for (Enemy e : enemies) e.draw(g2);
        if (boss != null) { boss.drawServer(g2); boss.draw(g2); }
        for (Projectile p : projectiles) p.draw(g2);
        drawParticles(g2);
        player.draw(g2);
        drawBossHUD(g2);

        // Animation explosion serveur
        if (serverExploding) {
            drawServerExplosion(g2);
        }
    }

    private void drawServerExplosion(Graphics2D g2) {
        // Flash blanc
        int alpha = Math.min(180, explosionTimer * 8);
        g2.setColor(new Color(255, 100, 0, Math.max(0, 180 - explosionTimer * 3)));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Particules
        for (int[] p : explosionParticles) {
            int a = (int)(255.0 * p[4] / 80.0);
            g2.setColor(new Color(p[5], p[6], p[7], Math.max(0, a)));
            g2.fillOval(p[0] - 4, p[1] - 4, 8, 8);
        }

        // Texte dramatique
        if (explosionTimer > 30) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 40));
            drawCentered(g2, "💥 SERVEUR EN FLAMMES ! 💥", SCREEN_HEIGHT/2 - 20);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            drawCentered(g2, "Le Prof Duroc a détruit Moodle !", SCREEN_HEIGHT/2 + 30);
        }
    }

    // ── Décor Niveau 1 : salle de classe ──────────────────────────────────────
    private void drawSchoolBackground(Graphics2D g2) {
        for (int row = 0; row < MAX_SCREEN_ROW; row++) {
            for (int col = 0; col < MAX_SCREEN_COL; col++) {
                boolean alt = (row + col) % 2 == 0;
                g2.setColor(alt ? new Color(235, 235, 230) : new Color(215, 215, 210));
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        g2.setColor(new Color(25, 45, 25));
        g2.fillRect(TILE_SIZE * 3, 5, TILE_SIZE * 10, 40);
        g2.setColor(new Color(40, 70, 40));
        g2.drawRect(TILE_SIZE * 3, 5, TILE_SIZE * 10, 40);
        g2.setColor(new Color(220, 220, 200));
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString("ESIR — Projet Java dû dans", TILE_SIZE * 3 + 12, 25);
        int min = tempsRestant / 60, sec = tempsRestant % 60;
        g2.setColor(tempsRestant <= 30 ? new Color(255, 150, 150) : new Color(220, 220, 200));
        g2.drawString(String.format("%d:%02d !", min, sec), TILE_SIZE * 3 + 12, 40);
        g2.setColor(new Color(150, 200, 255, 180));
        g2.fillRect(10, 8, 60, 34);
        g2.fillRect(SCREEN_WIDTH - 70, 8, 60, 34);
        g2.setColor(new Color(100, 150, 200));
        g2.drawRect(10, 8, 60, 34);
        g2.drawRect(SCREEN_WIDTH - 70, 8, 60, 34);
        g2.drawLine(40, 8, 40, 42); g2.drawLine(10, 25, 70, 25);
        g2.drawLine(SCREEN_WIDTH - 40, 8, SCREEN_WIDTH - 40, 42);
        g2.drawLine(SCREEN_WIDTH - 70, 25, SCREEN_WIDTH - 10, 25);
    }

    // ── Décor Niveau 2 : couloir de laboratoire ───────────────────────────────
    private void drawLabBackground(Graphics2D g2) {
        // Fond labo : carrelage gris-vert
        for (int row = 0; row < MAX_SCREEN_ROW; row++) {
            for (int col = 0; col < MAX_SCREEN_COL; col++) {
                boolean alt = (row + col) % 2 == 0;
                g2.setColor(alt ? new Color(50, 70, 55) : new Color(40, 60, 45));
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        // Lignes de grille
        g2.setColor(new Color(20, 40, 25, 80));
        for (int c = 0; c <= MAX_SCREEN_COL; c++) g2.drawLine(c*TILE_SIZE, 0, c*TILE_SIZE, SCREEN_HEIGHT);
        for (int r = 0; r <= MAX_SCREEN_ROW; r++) g2.drawLine(0, r*TILE_SIZE, SCREEN_WIDTH, r*TILE_SIZE);

        // Tuyaux en haut
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(0, 0, SCREEN_WIDTH, 12);
        g2.setColor(new Color(60, 60, 60));
        for (int x = 0; x < SCREEN_WIDTH; x += 80) {
            g2.setColor(new Color(100, 100, 100));
            g2.fillRect(x, 0, 16, 20);
            // Boulons
            g2.setColor(new Color(150, 150, 150));
            g2.fillOval(x + 3, 3, 5, 5);
            g2.fillOval(x + 8, 3, 5, 5);
        }

        // Néons au plafond (clignotants)
        long t = System.currentTimeMillis() / 800;
        for (int nx = 60; nx < SCREEN_WIDTH - 60; nx += 160) {
            boolean on = (nx / 160 + t) % 7 != 0; // clignotement rare
            g2.setColor(on ? new Color(200, 255, 200, 180) : new Color(50, 80, 50));
            g2.fillRect(nx, 0, 80, 6);
        }

        // Cuves côté gauche
        for (int sy = 60; sy < SCREEN_HEIGHT - 80; sy += 100) {
            g2.setColor(new Color(30, 60, 50));
            g2.fillRoundRect(4, sy, 36, 70, 8, 8);
            g2.setColor(new Color(0, 120, 80));
            g2.drawRoundRect(4, sy, 36, 70, 8, 8);
            // Liquide
            g2.setColor(new Color(0, 180, 100, 150));
            g2.fillRoundRect(8, sy + 20, 28, 44, 6, 6);
            // Bulles
            long bt = System.currentTimeMillis() / 300 + sy;
            g2.setColor(new Color(0, 255, 150, 180));
            g2.fillOval(14, (int)(sy + 30 + (bt % 20)), 5, 5);
            g2.fillOval(22, (int)(sy + 20 + (bt % 30)), 4, 4);
        }

        // Signalétique de danger
        g2.setColor(new Color(200, 150, 0));
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("⚠ LAB 404 — ACCÈS RESTREINT", 50, SCREEN_HEIGHT - 8);

        // Panneau niveau 2
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(SCREEN_WIDTH - 180, 5, 170, 38, 8, 8);
        g2.setColor(new Color(0, 200, 100));
        g2.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2.drawString("NIVEAU 2 — LABO", SCREEN_WIDTH - 175, 22);
        g2.setColor(new Color(255, 255, 100));
        int min2 = tempsRestant / 60, sec2 = tempsRestant % 60;
        g2.drawString(String.format("Temps: %d:%02d", min2, sec2), SCREEN_WIDTH - 175, 38);
    }

    // ── Décor Boss : salle serveur ─────────────────────────────────────────────
    private void drawServerRoomBackground(Graphics2D g2) {
        g2.setColor(new Color(10, 12, 25));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2.setColor(new Color(0, 40, 80, 60));
        for (int i = 0; i < SCREEN_WIDTH; i += TILE_SIZE)
            g2.drawLine(i, 0, i, SCREEN_HEIGHT);
        for (int i = 0; i < SCREEN_HEIGHT; i += TILE_SIZE)
            g2.drawLine(0, i, SCREEN_WIDTH, i);

        // Pulsation rouge si temps faible
        if (bossTimer <= 10 && !serverExploding) {
            long now = System.currentTimeMillis();
            int pulse = (int)(Math.sin(now * 0.01) * 40 + 60);
            g2.setColor(new Color(200, 0, 0, pulse));
            g2.setStroke(new java.awt.BasicStroke(8));
            g2.drawRect(4, 4, SCREEN_WIDTH - 8, SCREEN_HEIGHT - 8);
            g2.setStroke(new java.awt.BasicStroke(1));
        }

        for (int sy = 60; sy < SCREEN_HEIGHT - 60; sy += 80) {
            drawDecorServer(g2, 2, sy);
            drawDecorServer(g2, SCREEN_WIDTH - 50, sy);
        }
    }

    private void drawDecorServer(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(20, 30, 60));
        g2.fillRect(x, y, 48, 70);
        g2.setColor(new Color(0, 80, 150));
        g2.drawRect(x, y, 48, 70);
        long t = System.currentTimeMillis() / 400;
        for (int i = 0; i < 3; i++) {
            g2.setColor((t + i) % 2 == 0 ? Color.GREEN : new Color(0, 50, 0));
            g2.fillOval(x + 6 + i * 14, y + 8, 6, 6);
        }
        g2.setColor(new Color(0, 60, 100));
        for (int i = 0; i < 4; i++)
            g2.fillRect(x + 4, y + 20 + i * 12, 40, 4);
    }

    private void drawParticles(Graphics2D g2) {
        for (int[] p : particles) {
            int alpha = (int)(255.0 * p[4] / 20.0);
            g2.setColor(new Color(p[5], p[6], p[7], Math.max(0, alpha)));
            g2.fillOval(p[0] - 3, p[1] - 3, 6, 6);
        }
    }

    private void drawHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(0, 0, SCREEN_WIDTH, 58, 0, 0);

        // Barre de vie
        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(10, 10, player.maxHp * 2, 16, 6, 6);
        Color hpColor = player.hp > 60 ? new Color(50, 200, 50)
                      : player.hp > 30 ? new Color(200, 150, 0) : Color.RED;
        g2.setColor(hpColor);
        g2.fillRoundRect(10, 10, player.hp * 2, 16, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(10, 10, player.maxHp * 2, 16, 6, 6);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("PV " + player.hp + "/" + player.maxHp, 14, 23);

        // Indicateur personnage
        g2.setColor(player.characterType == 1 ? new Color(180, 80, 255) : new Color(100, 150, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString(player.characterType == 1 ? "⚔ NINJA" : "🎒 ÉTUDIANT", 10, 50);

        // Score
        g2.setColor(new Color(255, 210, 50));
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Score: " + score, SCREEN_WIDTH - 160, 24);

        // Timer
        int min = tempsRestant / 60, sec = tempsRestant % 60;
        g2.setColor(tempsRestant <= 30 ? Color.RED : Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String timeStr = String.format("%d:%02d", min, sec);
        int tw = g2.getFontMetrics().stringWidth(timeStr);
        g2.drawString(timeStr, SCREEN_WIDTH/2 - tw/2, 42);

        // Arme
        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("[ " + player.getWeaponName() + " ]", SCREEN_WIDTH - 160, 46);

        if (!enemies.isEmpty()) {
            g2.setColor(new Color(255, 140, 0));
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.drawString("👾 " + enemies.size(), SCREEN_WIDTH - 60, 46);
        }

        // Niveau actuel
        g2.setColor(new Color(150, 255, 150));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("Niv." + currentLevel, SCREEN_WIDTH/2 + 50, 18);

        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("ZQSD déplacer  ESPACE tirer  E porte/arme", SCREEN_WIDTH/2 - 120, SCREEN_HEIGHT - 4);
    }

    private void drawBossHUD(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(0, 0, SCREEN_WIDTH, 58, 0, 0);

        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(10, 10, player.maxHp * 2, 16, 6, 6);
        Color hpColor = player.hp > 60 ? new Color(50, 200, 50)
                      : player.hp > 30 ? new Color(200, 150, 0) : Color.RED;
        g2.setColor(hpColor);
        g2.fillRoundRect(10, 10, player.hp * 2, 16, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(10, 10, player.maxHp * 2, 16, 6, 6);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        g2.drawString("PV " + player.hp, 14, 23);

        // Timer BOSS avec danger
        Color timerColor = bossTimer <= 10 ? Color.RED : (bossTimer <= 20 ? new Color(255, 150, 0) : Color.CYAN);
        g2.setColor(timerColor);
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        String t = bossTimer + "s ⚠";
        int tw = g2.getFontMetrics().stringWidth(t);
        g2.drawString(t, SCREEN_WIDTH/2 - tw/2, 42);

        g2.setColor(new Color(255, 210, 50));
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Score: " + score, SCREEN_WIDTH - 160, 24);

        if (boss != null && boss.beaten) {
            g2.setColor(new Color(50, 255, 100));
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            String msg = "Boss éliminé ! Monte déposer ton projet !";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, SCREEN_WIDTH/2 - w/2, SCREEN_HEIGHT - 18);
        } else {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String warn = "⚠ SERVEUR EXPLOSE DANS " + bossTimer + "s !";
            int w = g2.getFontMetrics().stringWidth(warn);
            g2.drawString(warn, SCREEN_WIDTH/2 - w/2, SCREEN_HEIGHT - 8);
        }

        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("[ " + player.getWeaponName() + " ]  E=changer", 10, 46);

        if (!enemies.isEmpty()) {
            g2.setColor(new Color(255, 100, 100));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("Sbires: " + enemies.size(), SCREEN_WIDTH - 160, 46);
        }

        // Indicateur de rage du boss
        if (boss != null && !boss.beaten) {
            if (boss.isRaging()) {
                // Bandeau rouge clignotant RAGE
                long now = System.currentTimeMillis();
                int alpha = (int)(Math.sin(now * 0.015) * 80 + 130);
                g2.setColor(new Color(255, 0, 0, alpha));
                g2.fillRect(0, SCREEN_HEIGHT/2 - 20, SCREEN_WIDTH, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                drawCentered(g2, "⚡ LE PROF EST EN RAGE ! ⚡", SCREEN_HEIGHT/2 + 10);
            }
        }
    }

    private void drawLevel2Intro(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2.setColor(new Color(0, 20, 60, 80));
        for (int i = 0; i < SCREEN_HEIGHT; i += 3) g2.fillRect(0, i, SCREEN_WIDTH, 1);

        long t = System.currentTimeMillis();
        int pulse = (int)(Math.sin(t * 0.005) * 30 + 100);
        g2.setColor(new Color(0, 150, 80, pulse));
        g2.setStroke(new java.awt.BasicStroke(6));
        g2.drawRect(3, 3, SCREEN_WIDTH - 6, SCREEN_HEIGHT - 6);
        g2.setStroke(new java.awt.BasicStroke(1));

        g2.setColor(new Color(0, 220, 100));
        g2.setFont(new Font("Monospaced", Font.BOLD, 36));
        drawCentered(g2, "NIVEAU 2 — LABO 404", SCREEN_HEIGHT/2 - 90);

        g2.setColor(new Color(0, 150, 80));
        g2.fillRect(80, SCREEN_HEIGHT/2 - 70, SCREEN_WIDTH - 160, 2);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        drawCentered(g2, "Les ennemis se sont renforcés dans le labo !", SCREEN_HEIGHT/2 - 30);

        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        g2.setColor(new Color(100, 255, 180));

        // Affichage power-ups conservés
        String powerupMsg = "Power-ups conservés : ";
        if (savedUsbUpgraded) powerupMsg += "🔵 Clé USB+ ";
        if (savedCoffeeLevel > 0) powerupMsg += "☕ Café x" + savedCoffeeLevel;
        if (!savedUsbUpgraded && savedCoffeeLevel == 0) powerupMsg += "(aucun)";
        drawCentered(g2, powerupMsg, SCREEN_HEIGHT/2 + 10);
        drawCentered(g2, "Tes power-ups sont conservés pour cette salle !", SCREEN_HEIGHT/2 + 38);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        drawCentered(g2, "[ENTRÉE] pour continuer", SCREEN_HEIGHT/2 + 90);

        int prog = (int)((double)introTimer / INTRO_DURATION * (SCREEN_WIDTH - 60));
        g2.setColor(new Color(0, 50, 30));
        g2.fillRoundRect(30, SCREEN_HEIGHT - 22, SCREEN_WIDTH - 60, 10, 6, 6);
        g2.setColor(new Color(0, 200, 80));
        g2.fillRoundRect(30, SCREEN_HEIGHT - 22, prog, 10, 6, 6);
        g2.setColor(Color.GREEN);
        g2.drawRoundRect(30, SCREEN_HEIGHT - 22, SCREEN_WIDTH - 60, 10, 6, 6);
    }

    private void drawBossIntro(Graphics2D g2) {
        g2.setColor(new Color(5, 0, 10));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2.setColor(new Color(60, 0, 80, 80));
        for (int i = 0; i < SCREEN_HEIGHT; i += 3) g2.fillRect(0, i, SCREEN_WIDTH, 1);

        long t = System.currentTimeMillis();
        int pulse = (int)(Math.sin(t * 0.007) * 40 + 120);
        g2.setColor(new Color(150, 0, 200, pulse));
        g2.setStroke(new java.awt.BasicStroke(6));
        g2.drawRect(3, 3, SCREEN_WIDTH - 6, SCREEN_HEIGHT - 6);
        g2.setStroke(new java.awt.BasicStroke(1));

        g2.setColor(new Color(200, 0, 255));
        g2.setFont(new Font("Monospaced", Font.BOLD, 40));
        drawCentered(g2, "💀 SALLE DES SERVEURS 💀", SCREEN_HEIGHT/2 - 90);

        g2.setColor(new Color(150, 0, 200));
        g2.fillRect(40, SCREEN_HEIGHT/2 - 68, SCREEN_WIDTH - 80, 2);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        drawCentered(g2, "Le Prof DUROC bloque l'accès au serveur Moodle !", SCREEN_HEIGHT/2 - 40);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(new Color(255, 150, 80));
        drawCentered(g2, "Il peut TIRER et INVOQUER des sbires.", SCREEN_HEIGHT/2 + 0);
        drawCentered(g2, "Élimine-le, puis dépose ton projet sur le serveur !", SCREEN_HEIGHT/2 + 28);

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        drawCentered(g2, "⚠ TEMPS LIMITE : 20 SECONDES ! ⚠", SCREEN_HEIGHT/2 + 65);
        g2.setColor(new Color(255, 200, 100));
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        drawCentered(g2, "Si le temps expire — le SERVEUR EXPLOSE !", SCREEN_HEIGHT/2 + 90);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        drawCentered(g2, "[ENTRÉE] pour commencer", SCREEN_HEIGHT/2 + 128);

        int prog = (int)((double)introTimer / INTRO_DURATION * (SCREEN_WIDTH - 60));
        g2.setColor(new Color(50, 0, 60));
        g2.fillRoundRect(30, SCREEN_HEIGHT - 22, SCREEN_WIDTH - 60, 10, 6, 6);
        g2.setColor(new Color(180, 0, 255));
        g2.fillRoundRect(30, SCREEN_HEIGHT - 22, prog, 10, 6, 6);
        g2.setColor(Color.MAGENTA);
        g2.drawRoundRect(30, SCREEN_HEIGHT - 22, SCREEN_WIDTH - 60, 10, 6, 6);
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, SCREEN_WIDTH/2 - w/2, y);
    }

    private void drawCenteredAt(Graphics2D g2, String text, int cx, int y) {
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, cx - w/2, y);
    }

    private void drawMenu(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        drawCentered(g2, "ESIRScape v4", SCREEN_HEIGHT/2);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        drawCentered(g2, "ENTRÉE pour continuer", SCREEN_HEIGHT/2 + 50);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(20, 0, 0));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        for (int i = 0; i < 200; i++) {
            int gx = (int)(Math.random() * SCREEN_WIDTH);
            int gy = (int)(Math.random() * SCREEN_HEIGHT);
            g2.setColor(new Color(80, 0, 0, 80));
            g2.fillRect(gx, gy, 2, 2);
        }
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 58));
        drawCentered(g2, "GAME OVER", SCREEN_HEIGHT/2 - 80);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        String reason = serverExploding || bossTimer <= 0
            ? "Le serveur a explosé — Projet non rendu !"
            : (tempsRestant <= 0 ? "Temps écoulé — Projet non rendu !" : "L'étudiant est tombé au combat !");
        drawCentered(g2, reason, SCREEN_HEIGHT/2 - 10);
        drawCentered(g2, "Score final : " + score, SCREEN_HEIGHT/2 + 40);

        g2.setColor(new Color(80, 200, 80));
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        drawCentered(g2, "ENTRÉE pour recommencer", SCREEN_HEIGHT/2 + 110);
    }

    private void drawWin(Graphics2D g2) {
        g2.setColor(new Color(0, 20, 0));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        for (int i = 0; i < 150; i++) {
            int cx = (int)(Math.random() * SCREEN_WIDTH);
            int cy = (int)(Math.random() * SCREEN_HEIGHT);
            Color[] cols = {Color.YELLOW, Color.GREEN, Color.CYAN, Color.ORANGE, Color.WHITE};
            g2.setColor(cols[(int)(Math.random() * cols.length)]);
            g2.fillRect(cx, cy, 4, 8);
        }
        g2.setColor(new Color(80, 255, 80));
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        drawCentered(g2, "PROJET VALIDÉ !", SCREEN_HEIGHT/2 - 90);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        drawCentered(g2, "Rendu à temps — Mention Très Bien !", SCREEN_HEIGHT/2 - 20);
        drawCentered(g2, "GG, t'as survécu au Prof Duroc !", SCREEN_HEIGHT/2 + 20);

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        drawCentered(g2, "Score final : " + score, SCREEN_HEIGHT/2 + 70);

        g2.setColor(new Color(100, 200, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        drawCentered(g2, "ENTRÉE pour revenir à la sélection", SCREEN_HEIGHT/2 + 120);
    }
}
