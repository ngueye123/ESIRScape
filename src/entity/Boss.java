package entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.GamePanel;
import projectile.PrintProjectile;

// Boss final : le Serveur Moodle
// Phase 1 : tire en ligne droite vers le joueur
// Phase 2 (moins de 50% de vie) : tire en croix dans 4 directions
public class Boss extends Entity {

    private GamePanel gp;

    // Compteur de tir
    private int shootTimer = 0;
    private int shootDelay = 80;

    // Phase du boss (1 ou 2)
    private int phase = 1;

    // Deplacement du boss de gauche a droite
    private int direction = 1;

    public Boss(int x, int y, GamePanel gp) {
        this.x   = x;
        this.y   = y;
        this.gp  = gp;
        this.speed = 1;
        this.maxHp = 300;
        this.hp    = maxHp;
    }

    // Indique si le boss est mort
    public boolean isDead() {
        return hp <= 0;
    }

    // Reduit les HP du boss
    public void takeDamage(int degats) {
        hp -= degats;
    }

    @Override
    public void update() {

        // Deplacement de gauche a droite
        x += direction * speed;
        if (x > GamePanel.SCREEN_WIDTH - GamePanel.TILE_SIZE * 2) direction = -1;
        if (x < 0)                                                 direction =  1;

        // Passage en phase 2 si moins de 50% de vie
        if (hp < maxHp / 2) {
            phase      = 2;
            shootDelay = 50; // tire plus vite en phase 2
        }

        // Tir periodique
        shootTimer++;
        if (shootTimer >= shootDelay) {
            shoot();
            shootTimer = 0;
        }

        // Collision avec le joueur
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE * 2 &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE * 2) {
            gp.player.hp -= 2;
        }
    }

    // Tir selon la phase
    private void shoot() {
        int cx = x + GamePanel.TILE_SIZE;
        int cy = y + GamePanel.TILE_SIZE;

        if (phase == 1) {
            // Tire vers le joueur
            int dx = gp.player.x - cx;
            int dy = gp.player.y - cy;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                int ndx = (int)(dx / dist);
                int ndy = (int)(dy / dist);
                gp.projectiles.add(new PrintProjectile(cx, cy, ndx, ndy, 15, gp, true));
            }
        } else {
            // Tire en croix dans les 4 directions
            gp.projectiles.add(new PrintProjectile(cx, cy,  1,  0, 15, gp, true));
            gp.projectiles.add(new PrintProjectile(cx, cy, -1,  0, 15, gp, true));
            gp.projectiles.add(new PrintProjectile(cx, cy,  0,  1, 15, gp, true));
            gp.projectiles.add(new PrintProjectile(cx, cy,  0, -1, 15, gp, true));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int taille = GamePanel.TILE_SIZE * 2;

        // Corps du boss : gros rectangle bleu fonce
        g2.setColor(new Color(0, 40, 120));
        g2.fillRect(x, y, taille, taille);

        // Contour
        g2.setColor(phase == 2 ? Color.RED : Color.CYAN);
        g2.drawRect(x, y, taille, taille);

        // Texte Moodle
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("MOODLE", x + 14, y + taille / 2 - 5);
        g2.drawString("SERVER", x + 14, y + taille / 2 + 10);

        // Yeux rouges clignotants en phase 2
        if (phase == 2) {
            g2.setColor(Color.RED);
            g2.fillOval(x + 12, y + 15, 14, 14);
            g2.fillOval(x + 46, y + 15, 14, 14);
        }

        // Barre de vie du boss en bas de l ecran
        int largeurBarre = 400;
        int xBarre       = GamePanel.SCREEN_WIDTH / 2 - largeurBarre / 2;
        int yBarre       = GamePanel.SCREEN_HEIGHT - 30;

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(xBarre, yBarre, largeurBarre, 18);

        g2.setColor(phase == 2 ? Color.RED : Color.CYAN);
        int largeurVie = (int)((double) hp / maxHp * largeurBarre);
        g2.fillRect(xBarre, yBarre, largeurVie, 18);

        g2.setColor(Color.WHITE);
        g2.drawRect(xBarre, yBarre, largeurBarre, 18);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("Serveur Moodle - Phase " + phase, xBarre + 120, yBarre + 13);
    }
}
