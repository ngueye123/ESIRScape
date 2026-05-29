package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;
import main.SoundManager;

public class ExamenEnemy extends Enemy {

    private int animTimer = 0;
    private int animFrame = 0;
    private boolean wasAlive = true;
    private float alpha = 0f;
    private int spawnTimer = 0;

    public ExamenEnemy(int x, int y, GamePanel gp, int speed) {
        super(x, y, gp);
        this.speed      = speed;
        this.maxHp      = 25;
        this.hp         = maxHp;
        this.scoreValue = 200;
        this.shootDelay = 45;
    }

    @Override
    public void update() {
        if (isDead()) {
            if (wasAlive) { SoundManager.playEnemyDie(); wasAlive = false; }
            return;
        }
        if (alpha < 1f) { spawnTimer++; alpha = Math.min(1f, spawnTimer / 30f); }
        int dx = gp.player.x - x;
        int dy = gp.player.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            x += (int)(dx / dist * speed);
            y += (int)(dy / dist * speed);
        }
        animTimer++;
        if (animTimer >= 5) { animTimer = 0; animFrame = (animFrame + 1) % 4; }
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
            gp.player.hp -= 2;
            SoundManager.playPlayerHit();
        }
        shootTimer++;
        if (shootTimer >= shootDelay) { shootAtPlayer(); shootTimer = 0; }
    }

    @Override
    public void draw(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;
        int bob = (animFrame == 1 || animFrame == 3) ? 2 : 0;
        g2.setColor(new Color(180, 60, 200, (int)(alpha * 220)));
        g2.fillRoundRect(x + 4, y + 4 + bob, ts - 8, ts - 8, 10, 10);
        g2.setColor(new Color(255, 255, 255, (int)(alpha * 200)));
        for (int i = 0; i < 4; i++)
            g2.fillRect(x + 8, y + 10 + i * 6 + bob, ts - 16 - (i % 2 == 0 ? 0 : 8), 2);
        g2.setColor(new Color(255, 50, 50, (int)(alpha * 255)));
        g2.fillOval(x + 12, y + 12 + bob, 8, 8);
        g2.fillOval(x + 26, y + 12 + bob, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(x + 14, y + 14 + bob, 4, 4);
        g2.fillOval(x + 28, y + 14 + bob, 4, 4);
        g2.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawLine(x + 11, y + 10 + bob, x + 16, y + 13 + bob);
        g2.drawLine(x + 25, y + 13 + bob, x + 35, y + 10 + bob);
        g2.drawArc(x + 14, y + 28 + bob, 20, 8, 0, -180);
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.setColor(new Color(255, 200, 0, (int)(alpha * 255)));
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        g2.drawString("0/20", x + 12, y + 5 + bob);
        g2.setColor(Color.RED);
        g2.fillRect(x, y - 8, ts, 5);
        g2.setColor(new Color(180, 0, 220));
        g2.fillRect(x, y - 8, (int)((double) hp / maxHp * ts), 5);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y - 8, ts, 5);
    }
}
