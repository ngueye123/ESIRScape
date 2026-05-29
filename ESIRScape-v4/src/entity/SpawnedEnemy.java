package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;
import main.SoundManager;

// Ennemi pondu par le Boss : petit et rapide, fonce immédiatement
public class SpawnedEnemy extends Enemy {

    private int animTimer = 0;
    private int animFrame = 0;
    private boolean wasAlive = true;
    private int spawnAnim = 0; // animation d'apparition

    public SpawnedEnemy(int x, int y, GamePanel gp) {
        super(x, y, gp);
        this.speed      = 4; // très rapide
        this.maxHp      = 15;
        this.hp         = maxHp;
        this.scoreValue = 50;
        this.shootDelay = 50;
    }

    @Override
    public void update() {
        if (isDead()) {
            if (wasAlive) { SoundManager.playHit(); wasAlive = false; }
            return;
        }

        if (spawnAnim < 20) { spawnAnim++; return; } // mini-délai d'apparition

        // Fonce vers le joueur sans hésiter
        int dx = gp.player.x - x;
        int dy = gp.player.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            x += (int)(dx / dist * speed);
            y += (int)(dy / dist * speed);
        }

        animTimer++;
        if (animTimer >= 4) { animTimer = 0; animFrame = (animFrame + 1) % 4; }

        // Collision
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
            gp.player.hp -= 2;
            SoundManager.playPlayerHit();
        }

        // Tire aussi
        shootTimer++;
        if (shootTimer >= shootDelay) {
            shootAtPlayer();
            shootTimer = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;
        float scale = Math.min(1f, spawnAnim / 20f);
        int size = (int)(ts * 0.65 * scale);
        int offset = (ts - size) / 2;
        int bob = (animFrame == 1 || animFrame == 3) ? 1 : 0;

        // Petit ennemi rouge pondu
        g2.setColor(new Color(220, 30, 30));
        g2.fillOval(x + offset, y + offset + bob, size, size);

        // Yeux blancs
        g2.setColor(Color.WHITE);
        g2.fillOval(x + offset + size/5, y + offset + size/4 + bob, size/5, size/5);
        g2.fillOval(x + offset + size*3/5, y + offset + size/4 + bob, size/5, size/5);

        // Bouche méchante
        g2.setColor(Color.BLACK);
        g2.drawArc(x + offset + size/4, y + offset + size/2 + bob, size/2, size/4, 0, -180);

        // Barre de vie
        g2.setColor(Color.RED);
        g2.fillRect(x, y - 6, ts, 4);
        g2.setColor(new Color(255, 100, 0));
        g2.fillRect(x, y - 6, (int)((double) hp / maxHp * ts), 4);
    }
}
