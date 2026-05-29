package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;
import main.SoundManager;

public class BugEnemy extends Enemy {

    private int animTimer = 0;
    private int animFrame = 0;
    private boolean wasAlive = true;

    public BugEnemy(int x, int y, GamePanel gp, int speed) {
        super(x, y, gp);
        this.speed      = speed; // configurable par niveau
        this.maxHp      = 30;
        this.hp         = maxHp;
        this.scoreValue = 100;
        this.shootDelay = 70;
    }

    @Override
    public void update() {
        if (isDead()) {
            if (wasAlive) { SoundManager.playEnemyDie(); wasAlive = false; }
            return;
        }
        int dx = gp.player.x - x;
        int dy = gp.player.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            x += (int)(dx / dist * speed);
            y += (int)(dy / dist * speed);
        }
        animTimer++;
        if (animTimer >= 6) { animTimer = 0; animFrame = (animFrame + 1) % 4; }
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
            gp.player.hp -= 1;
            SoundManager.playPlayerHit();
        }
        shootTimer++;
        if (shootTimer >= shootDelay) { shootAtPlayer(); shootTimer = 0; }
    }

    @Override
    public void draw(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;
        int bob = (animFrame == 1 || animFrame == 3) ? 2 : 0;
        g2.setColor(new Color(0, 150, 30));
        g2.fillOval(x + 4, y + 8 + bob, ts - 8, ts - 14);
        g2.setColor(new Color(0, 100, 0));
        g2.fillOval(x + 10, y + bob, ts - 20, 16);
        g2.setColor(Color.RED);
        g2.fillOval(x + 8,  y + 4 + bob, 10, 10);
        g2.fillOval(x + 28, y + 4 + bob, 10, 10);
        g2.setColor(new Color(255, 150, 150));
        g2.fillOval(x + 10, y + 6 + bob, 4, 4);
        g2.fillOval(x + 30, y + 6 + bob, 4, 4);
        g2.setColor(new Color(0, 80, 0));
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawLine(x + 13, y + 3 + bob, x + 4,  y - 6);
        g2.drawLine(x + 33, y + 3 + bob, x + 42, y - 6);
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.setColor(new Color(0, 100, 20));
        int legBob = (animFrame % 2 == 0) ? 3 : -3;
        g2.drawLine(x + 8,  y + 20 + bob, x,      y + 28 + bob + legBob);
        g2.drawLine(x + 8,  y + 26 + bob, x,      y + 34 + bob - legBob);
        g2.drawLine(x + 38, y + 20 + bob, x + 48, y + 28 + bob + legBob);
        g2.drawLine(x + 38, y + 26 + bob, x + 48, y + 34 + bob - legBob);
        g2.setColor(new Color(100, 255, 100, 120));
        g2.fillOval(x - 6, y + 10 + bob, 20, 10);
        g2.fillOval(x + 34, y + 10 + bob, 20, 10);
        g2.setColor(Color.RED);
        g2.fillRect(x, y - 8, ts, 5);
        g2.setColor(Color.GREEN);
        g2.fillRect(x, y - 8, (int)((double) hp / maxHp * ts), 5);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y - 8, ts, 5);
    }
}
