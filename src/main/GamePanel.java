package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

// Panel principal : contient la boucle de jeu a 60 FPS
// C est le chef d orchestre du jeu
public class GamePanel extends JPanel implements Runnable {

    public static final int TILE_SIZE      = 48;
    public static final int MAX_SCREEN_COL = 16;
    public static final int MAX_SCREEN_ROW = 12;
    public static final int SCREEN_WIDTH   = TILE_SIZE * MAX_SCREEN_COL;
    public static final int SCREEN_HEIGHT  = TILE_SIZE * MAX_SCREEN_ROW;

    private static final int FPS = 60;

    // Etat du jeu
    public String gameState = "MENU";

    // Niveau actuel
    public int currentLevel = 1;

    // Score
    public int score = 0;

    // Timer de jeu : 3 minutes pour deposer le projet
    public int tempsRestant  = 180;
    private int timerCounter = 0;

    // Composants principaux
    public KeyHandler keyH;
    private Thread gameThread;
    public Player player;
    public TileManager tileManager;

    // Listes des entites
    public ArrayList<Projectile> projectiles;
    public ArrayList<Enemy> enemies;
    public ArrayList<PowerUp> powerUps;

    // Boss du niveau 2
    public Boss boss;
    public boolean bossSpawned = false;

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

    // Initialise les entites pour un niveau donne
    public void initLevel(int level) {
        currentLevel = level;
        projectiles.clear();
        enemies.clear();
        powerUps.clear();
        bossSpawned = false;
        boss        = null;

        // Replace le joueur en debut de carte
        player.x  = 80;
        player.y  = 250;

        if (level == 1) {
            tileManager.loadMap("/maps/map1.txt");
            enemies.add(new BugEnemy(300, 200, this));
            enemies.add(new BugEnemy(500, 150, this));
            enemies.add(new BugEnemy(600, 300, this));
            enemies.add(new ExamenEnemy(400, 350, this));
        } else {
            tileManager.loadMap("/maps/map2.txt");
            enemies.add(new BugEnemy(200, 150, this));
            enemies.add(new BugEnemy(500, 200, this));
            enemies.add(new ExamenEnemy(350, 250, this));
            enemies.add(new ExamenEnemy(550, 350, this));
            enemies.add(new BugEnemy(400, 100, this));

            // Le serveur Moodle est fixe dans le coin haut droit
            boss        = new Boss(SCREEN_WIDTH - TILE_SIZE * 3, TILE_SIZE, this);
            bossSpawned = true;
        }
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
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1_000_000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {

        if (gameState.equals("MENU")) {
            if (keyH.enterPressed) {
                gameState    = "PLAYING";
                score        = 0;
                tempsRestant = 180;
                timerCounter = 0;
                player       = new Player(this, keyH);
                initLevel(1);
            }
            if (keyH.escapePressed) {
                System.exit(0);
            }
        }

        if (gameState.equals("PLAYING")) {
            player.update();

            // Decompte du timer : une seconde = 60 frames
            timerCounter++;
            if (timerCounter >= 60) {
                timerCounter = 0;
                tempsRestant--;
                if (tempsRestant <= 0) {
                    gameState = "GAME_OVER";
                }
            }

            // Mise a jour des projectiles
            ArrayList<Projectile> projASupprimer = new ArrayList<>();
            for (Projectile p : projectiles) {
                p.update();
                if (p.isOutOfScreen()) {
                    projASupprimer.add(p);
                }
            }
            projectiles.removeAll(projASupprimer);

            // Mise a jour des ennemis
            ArrayList<Enemy> ennemisASupprimer = new ArrayList<>();
            for (Enemy e : enemies) {
                e.update();
                if (e.isDead()) {
                    ennemisASupprimer.add(e);
                    score += e.getScoreValue();
                    dropPowerUp(e.x, e.y);
                }
            }
            enemies.removeAll(ennemisASupprimer);

            // Mise a jour du boss si present
            if (boss != null) {
                boss.update();
                // Victoire si le joueur touche le serveur Moodle
                if (boss.isTouched()) {
                    score    += 500;
                    gameState = "WIN";
                }
            }

            // Mise a jour des power ups
            ArrayList<PowerUp> puASupprimer = new ArrayList<>();
            for (PowerUp pu : powerUps) {
                pu.update();
                if (pu.isCollected()) {
                    puASupprimer.add(pu);
                }
            }
            powerUps.removeAll(puASupprimer);

            // Passage au niveau suivant
            checkLevelComplete();

            // Game over si plus de vie
            if (player.hp <= 0) {
                gameState = "GAME_OVER";
            }
        }

        if (gameState.equals("GAME_OVER")) {
            if (keyH.enterPressed) {
                gameState    = "PLAYING";
                score        = 0;
                tempsRestant = 180;
                timerCounter = 0;
                player       = new Player(this, keyH);
                initLevel(1);
            }
        }

        if (gameState.equals("WIN")) {
            if (keyH.enterPressed) {
                gameState = "MENU";
            }
        }
    }

    // Drop aleatoire d un power up a la mort d un ennemi
    private void dropPowerUp(int x, int y) {
        double rand = Math.random();
        if (rand < 0.3) {
            powerUps.add(new CoffeeUp(x, y, this));
        } else if (rand < 0.5) {
            powerUps.add(new UsbUp(x, y, this));
        }
    }

    // Verifie si le niveau est termine pour passer au suivant
    private void checkLevelComplete() {
        if (currentLevel == 1 && enemies.isEmpty()) {
            // Remet le timer a zero pour le niveau 2
            tempsRestant = 180;
            timerCounter = 0;
            initLevel(2);
        }
        // Au niveau 2 le boss est deja place dans initLevel
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState.equals("MENU")) {
            drawMenu(g2);
        } else if (gameState.equals("PLAYING")) {
            tileManager.draw(g2);

            for (PowerUp pu : powerUps) {
                pu.draw(g2);
            }
            for (Enemy e : enemies) {
                e.draw(g2);
            }
            if (boss != null) {
                boss.draw(g2);
            }
            for (Projectile p : projectiles) {
                p.draw(g2);
            }

            player.draw(g2);
            drawHUD(g2);

        } else if (gameState.equals("GAME_OVER")) {
            drawGameOver(g2);
        } else if (gameState.equals("WIN")) {
            drawWin(g2);
        }

        g2.dispose();
    }

    private void drawMenu(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString("ESIR Escape", 210, 160);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Rends ton projet avant 10h00 !", 220, 220);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        g2.drawString("ENTREE  -  Jouer", 280, 320);

        g2.setColor(Color.RED);
        g2.drawString("ECHAP   -  Quitter", 280, 370);

        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("ZQSD / fleches : deplacer   |   Espace : tirer   |   E : changer d arme", 120, 530);
    }

    private void drawHUD(Graphics2D g2) {
        // Barre de vie
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(10, 10, player.maxHp * 2, 15);
        g2.setColor(Color.RED);
        g2.fillRect(10, 10, player.hp * 2, 15);
        g2.setColor(Color.WHITE);
        g2.drawRect(10, 10, player.maxHp * 2, 15);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString("PV : " + player.hp + "/" + player.maxHp, 15, 23);

        // Score
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Score : " + score, SCREEN_WIDTH - 160, 25);

        // Arme active
        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("Arme : " + player.getWeaponName(), 10, 48);

        // Niveau
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        g2.drawString("Niveau " + currentLevel, SCREEN_WIDTH / 2 - 30, 25);

        // Timer : rouge si moins de 30 secondes
        int minutes  = tempsRestant / 60;
        int secondes = tempsRestant % 60;
        String temps = String.format("%d:%02d", minutes, secondes);

        if (tempsRestant <= 30) {
            g2.setColor(Color.RED);
        } else {
            g2.setColor(Color.WHITE);
        }
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString(temps, SCREEN_WIDTH / 2 - 18, 48);

        // Message indicatif au niveau 2
        if (currentLevel == 2) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            g2.drawString("Depose ton projet sur le serveur Moodle !", SCREEN_WIDTH / 2 - 160, SCREEN_HEIGHT - 10);
        }
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString("Game Over !", 230, 230);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));

        // Message different selon la cause de la defaite
        if (tempsRestant <= 0) {
            g2.drawString("Temps ecoule ! Projet non rendu.", 190, 300);
        } else {
            g2.drawString("Ton etudiant est tombe au combat.", 190, 300);
        }

        g2.drawString("Score final : " + score, 300, 350);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("ENTREE pour recommencer", 250, 420);
    }

    private void drawWin(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 44));
        g2.drawString("Projet Valide !", 210, 220);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        g2.drawString("Depose a temps. Bravo !", 240, 290);
        g2.drawString("Score final : " + score, 300, 340);

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("ENTREE pour revenir au menu", 230, 420);
    }
}