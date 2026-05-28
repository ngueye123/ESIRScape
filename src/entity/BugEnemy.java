package entity;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;

// Ennemi de type Bug Informatique
// Visible en permanence, se dirige vers le joueur et tire occasionnellement
public class BugEnemy extends Enemy {

    public BugEnemy(int x, int y, GamePanel gp) {
        super(x, y, gp);
        this.speed      = 1;
        this.maxHp      = 30;
        this.hp         = maxHp;
        this.scoreValue = 100;
        this.shootDelay = 120;
    }

    @Override
    public void update() {

        // Se deplace vers le joueur
        int dx = gp.player.x - x;
        int dy = gp.player.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            x += (int)(dx / dist * speed);
            y += (int)(dy / dist * speed);
        }

        // Collision avec le joueur : lui inflige des degats
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
            gp.player.hp -= 1;
        }

        // Tir periodique vers le joueur
        shootTimer++;
        if (shootTimer >= shootDelay) {
            shootAtPlayer();
            shootTimer = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Corps du bug : carre vert fonce
        g2.setColor(new Color(0, 130, 0));
        g2.fillRect(x, y, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);

        // Yeux rouges
        g2.setColor(Color.RED);
        g2.fillOval(x + 8,  y + 10, 10, 10);
        g2.fillOval(x + 28, y + 10, 10, 10);

        // Antennes
        g2.setColor(Color.BLACK);
        g2.drawLine(x + 13, y + 10, x + 5,  y);
        g2.drawLine(x + 33, y + 10, x + 40, y);

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
