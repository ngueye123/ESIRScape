package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import entity.Player;
import tile.TileManager;

// Panel principal : contient la boucle de jeu a 60 FPS
public class GamePanel extends JPanel implements Runnable {

    // Taille d'une tuile a l'ecran (16px de base * echelle 3 = 48px)
    public static final int TILE_SIZE        = 48;
    public static final int MAX_SCREEN_COL   = 16;
    public static final int MAX_SCREEN_ROW   = 12;
    public static final int SCREEN_WIDTH     = TILE_SIZE * MAX_SCREEN_COL; // 768
    public static final int SCREEN_HEIGHT    = TILE_SIZE * MAX_SCREEN_ROW; // 576

    // FPS cible
    private static final int FPS = 60;

    // Etat du jeu : MENU, PLAYING, GAME_OVER, WIN
    public String gameState = "MENU";

    // Niveau actuel (1 ou 2)
    public int currentLevel = 1;

    // Score du joueur
    public int score = 0;

    // Composants principaux
    public KeyHandler keyH;
    private Thread gameThread;
    public Player player;
    public TileManager tileManager;

    public GamePanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); // evite le scintillement

        keyH = new KeyHandler();
        this.addKeyListener(keyH);
        this.setFocusable(true);

        // Initialisation du joueur et de la map
        player = new Player(this, keyH);
        tileManager = new TileManager(this);
    }

    // Lance le thread principal du jeu
    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Intervalle entre deux frames en nanosecondes
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

    // Met a jour toutes les entites selon l'etat du jeu
    public void update() {
        if (gameState.equals("MENU")) {
            // On detecte l'appui sur Entree pour demarrer
            // (sera gere dans KeyHandler a la prochaine etape)
        }

        if (gameState.equals("PLAYING")) {
            player.update();
            // La mise a jour des ennemis et projectiles viendra dans les etapes suivantes
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
            player.draw(g2);
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

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("ESIR Escape", 250, 180);

        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        g2.drawString("Appuie sur ENTREE pour jouer", 200, 300);
        g2.drawString("Appuie sur ECHAP pour quitter", 200, 350);
    }

    // Affiche le HUD : vie et score
    private void drawHUD(Graphics2D g2) {
        // Barre de vie rouge
        g2.setColor(Color.RED);
        g2.fillRect(10, 10, player.hp * 2, 15);

        // Contour de la barre
        g2.setColor(Color.WHITE);
        g2.drawRect(10, 10, player.maxHp * 2, 15);

        // Score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Score : " + score, SCREEN_WIDTH - 150, 25);

        // Arme active
        g2.drawString("Arme : " + player.getWeaponName(), 10, 45);
    }

    // Ecran de fin de partie perdu
    private void drawGameOver(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.drawString("Game Over !", 270, 250);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Score final : " + score, 310, 310);
        g2.drawString("Appuie sur ENTREE pour rejouer", 210, 360);
    }

    // Ecran de victoire
    private void drawWin(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.drawString("Projet Valide !", 240, 250);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString("Score final : " + score, 310, 310);
        g2.drawString("Appuie sur ENTREE pour le menu", 205, 360);
    }
}