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

    // Taille d une tuile a l ecran (16px de base * echelle 3 = 48px)
    public static final int TILE_SIZE       = 48;
    public static final int MAX_SCREEN_COL  = 16;
    public static final int MAX_SCREEN_ROW  = 12;
    public static final int SCREEN_WIDTH    = TILE_SIZE * MAX_SCREEN_COL;
    public static final int SCREEN_HEIGHT   = TILE_SIZE * MAX_SCREEN_ROW;

    // FPS cible
    private static final int FPS = 60;

    // Etat du jeu
    public String gameState = "MENU";

    // Niveau actuel
    public int currentLevel = 1;

    // Score
    public int score = 0;

    // Composants principaux
    public KeyHandler keyH;
    private Thread gameThread;
    public Player player;
    public TileManager tileManager;

    // Listes des entites du jeu
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

    // Initialise ou reinitialise le jeu pour un niveau donne
    public void initLevel(int level) {
        currentLevel = level;
        projectiles.clear();
        enemies.clear();
        powerUps.clear();
        bossSpawned = false;
        boss        = null;

        if (level == 1) {
            tileManager.loadMap("/maps/map1.txt");
            // Quelques bugs au niveau 1
            enemies.add(new BugEnemy(300, 200, this));
            enemies.add(new BugEnemy(500, 150, this));
            enemies.add(new BugEnemy(600, 300, this));
            enemies.add(new ExamenEnemy(400, 350, this));
        } else {
            tileManager.loadMap("/maps/map2.txt");
            // Plus d ennemis au niveau 2
            enemies.add(new BugEnemy(200, 150, this));
            enemies.add(new BugEnemy(500, 200, this));
            enemies.add(new ExamenEnemy(350, 250, this));
            enemies.add(new ExamenEnemy(550, 350, this));
            enemies.add(new BugEnemy(400, 100, this));
        }
    }

    // Lance le thread principal du jeu
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

    // Met a jour toutes les entites du jeu
    public void update() {

        if (gameState.equals("MENU")) {
            if (keyH.enterPressed) {
                gameState = "PLAYING";
                score     = 0;
                player    = new Player(this, keyH);
                initLevel(1);
            }
            if (keyH.escapePressed) {
                System.exit(0);
            }
        }

        if (gameState.equals("PLAYING")) {
            player.update();

            // Mise a jour des projectiles du joueur
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
                    // Drop aleatoire d un power up
                    dropPowerUp(e.x, e.y);
                }
            }
            enemies.removeAll(ennemisASupprimer);

            // Mise a jour du boss si present
            if (boss != null) {
                boss.update();
                if (boss.isDead()) {
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

            // Verification si le niveau est termine
            checkLevelComplete();

            // Verification game over
            if (player.hp <= 0) {
                gameState = "GAME_OVER";
            }
        }

        if (gameState.equals("GAME_OVER")) {
            if (keyH.enterPressed) {
                gameState = "PLAYING";
                score     = 0;
                player    = new Player(this, keyH);
                initLevel(1);
            }
        }

        if (gameState.equals("WIN")) {
            if (keyH.enterPressed) {
                gameState = "MENU";
            }
        }
    }

    // Fait tomber un power up aleatoirement a la mort d un ennemi
    private void dropPowerUp(int x, int y) {
        double rand = Math.random();
        if (rand < 0.3) {
            powerUps.add(new CoffeeUp(x, y, this));
        } else if (rand < 0.5) {
            powerUps.add(new UsbUp(x, y, this));
        }
    }

    // Verifie si tous les ennemis sont morts pour passer au niveau suivant
    private void checkLevelComplete() {
        if (currentLevel == 1 && enemies.isEmpty()) {
            initLevel(2);
        } else if (currentLevel == 2 && enemies.isEmpty() && !bossSpawned) {
            // Fait apparaitre le boss
            boss        = new Boss(SCREEN_WIDTH / 2 - 48, 80, this);
            bossSpawned = true;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (gameState.equals("MENU")) {
            drawMenu(g2);
        } else if (gameState.equals("PLAYING")) {
            tileManager.draw(g2);

            // Affichage des power ups
            for (PowerUp pu : powerUps) {
                pu.draw(g2);
            }

            // Affichage des ennemis
            for (Enemy e : enemies) {
                e.draw(g2);
            }

            // Affichage du boss
            if (boss != null) {
                boss.draw(g2);
            }

            // Affichage des projectiles
            for (Projectile p : projectiles) {
                p.draw(g2);
            }

            // Affichage du joueur
            player.draw(g2);

            // HUD
            drawHUD(g2);

        } else if (gameState.equals("GAME_OVER")) {
            drawGameOver(g2);
        } else if (gameState.equals("WIN")) {
            drawWin(g2);
        }

        g2.dispose();
    }

    // Affiche le menu principal
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

    // Affiche le HUD en haut de l ecran
    private void drawHUD(Graphics2D g2) {
        // Fond de la barre de vie
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(10, 10, player.maxHp * 2, 15);

        // Barre de vie
        g2.setColor(Color.RED);
        g2.fillRect(10, 10, player.hp * 2, 15);

        // Contour
        g2.setColor(Color.WHITE);
        g2.drawRect(10, 10, player.maxHp * 2, 15);

        // Texte vie
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
        g2.drawString("Niveau " + currentLevel, SCREEN_WIDTH / 2 - 30, 25);
    }

    // Ecran de game over
    private void drawGameOver(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString("Game Over !", 230, 230);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        g2.drawString("Ton projet n a pas ete rendu a temps...", 160, 300);
        g2.drawString("Score final : " + score, 300, 350);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("ENTREE pour recommencer", 250, 420);
    }

    // Ecran de victoire
    private void drawWin(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 44));
        g2.drawString("Projet Valide !", 210, 220);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        g2.drawString("Tu as rendu a temps. Bravo !", 220, 290);
        g2.drawString("Score final : " + score, 300, 340);

        g2.setColor(Color.CYAN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("ENTREE pour revenir au menu", 230, 420);
    }
}
