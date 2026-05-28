package entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.GamePanel;

// Le Serveur Moodle : boss final statique dans un coin
// Le joueur doit aller le toucher pour gagner, pas le tuer
public class Boss extends Entity {

    private GamePanel gp;

    // Indique si le joueur a reussi a toucher le serveur
    public boolean touched = false;

    public Boss(int x, int y, GamePanel gp) {
        this.x     = x;
        this.y     = y;
        this.gp    = gp;
        this.maxHp = 1;
        this.hp    = 1;
    }

    // Le boss ne bouge pas et ne tire pas
    @Override
    public void update() {
        // Verifie si le joueur touche le serveur
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE * 2 &&
                Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE * 2) {
            touched = true;
        }
    }

    // Le boss n est jamais tue
    public boolean isDead() {
        return false;
    }

    // Retourne true si le joueur a depose son projet
    public boolean isTouched() {
        return touched;
    }

    @Override
    public void draw(Graphics2D g2) {
        int taille = GamePanel.TILE_SIZE * 2;

        // Corps du serveur
        g2.setColor(new Color(0, 40, 120));
        g2.fillRect(x, y, taille, taille);

        // Contour cyan
        g2.setColor(Color.CYAN);
        g2.drawRect(x, y, taille, taille);

        // Texte
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("MOODLE", x + 12, y + taille / 2 - 5);
        g2.drawString("SERVER", x + 12, y + taille / 2 + 10);

        // Petites lumieres vertes
        g2.setColor(Color.GREEN);
        g2.fillOval(x + 10, y + 10, 6, 6);
        g2.fillOval(x + 22, y + 10, 6, 6);

        // Indicateur : fleche ou texte pour guider le joueur
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("DEPOSE ICI", x + 4, y + taille + 14);
    }
}