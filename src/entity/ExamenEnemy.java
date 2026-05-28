package entity;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

// Ennemi de type Examen Surprise
// Invisible la plupart du temps, apparait brievement avant d attaquer
// Repond a la consigne : ennemis invisibles
public class ExamenEnemy extends Enemy {

    // Compteur de visibilite
    private int visibilityTimer = 0;

    // Duree d invisibilite et de visibilite en frames
    private static final int DUREE_INVISIBLE = 150;
    private static final int DUREE_VISIBLE   = 60;

    // Indique si l ennemi est actuellement visible
    private boolean visible = false;

    public ExamenEnemy(int x, int y, GamePanel gp) {
        super(x, y, gp);
        this.speed      = 2;
        this.maxHp      = 20;
        this.hp         = maxHp;
        this.scoreValue = 200;
        this.shootDelay = 60;
    }

    @Override
    public void update() {

        // Gestion de la visibilite par alternance
        visibilityTimer++;
        if (!visible && visibilityTimer >= DUREE_INVISIBLE) {
            visible         = true;
            visibilityTimer = 0;
        } else if (visible && visibilityTimer >= DUREE_VISIBLE) {
            visible         = false;
            visibilityTimer = 0;
        }

        // Quand visible : se rapproche du joueur et tire
        if (visible) {
            int dx = gp.player.x - x;
            int dy = gp.player.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0) {
                x += (int)(dx / dist * speed);
                y += (int)(dy / dist * speed);
            }

            // Collision avec le joueur
            if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
                Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
                gp.player.hp -= 2;
            }

            // Tir
            shootTimer++;
            if (shootTimer >= shootDelay) {
                shootAtPlayer();
                shootTimer = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Si invisible : on ne dessine rien
        if (!visible) return;

        // Corps : rectangle violet avec point d exclamation
        g2.setColor(new Color(130, 0, 180));
        g2.fillRect(x, y, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

        // Point d exclamation blanc
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        g2.drawString("?!", x + 10, y + 34);

        // Barre de vie
        g2.setColor(Color.RED);
        g2.fillRect(x, y - 8, GamePanel.TILE_SIZE, 5);
        g2.setColor(Color.GREEN);
        int largeurVie = (int)((double) hp / maxHp * GamePanel.TILE_SIZE);
        g2.fillRect(x, y - 8, largeurVie, 5);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y - 8, GamePanel.TILE_SIZE, 5);
    }
}
