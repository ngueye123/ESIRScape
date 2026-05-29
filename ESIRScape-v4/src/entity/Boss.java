package entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import main.GamePanel;
import main.SoundManager;
import projectile.BossProjectile;

public class Boss extends Entity {

    private GamePanel gp;

    public boolean beaten = false;
    public boolean serverReached = false;

    private int shootTimer  = 0;
    private int shootDelay  = 80;
    private int phase       = 1;
    private int spawnTimer  = 0;
    private int spawnDelay  = 300;

    public int serverX;
    public int serverY;

    private int animTimer = 0;
    private int animFrame = 0;
    private int hitFlash  = 0;

    public Boss(int x, int y, GamePanel gp) {
        this.x       = x;
        this.y       = y;
        this.gp      = gp;
        this.maxHp   = 200;
        this.hp      = maxHp;
        this.speed   = 1;
        this.serverX = GamePanel.SCREEN_WIDTH / 2 - GamePanel.TILE_SIZE;
        this.serverY = GamePanel.TILE_SIZE;
        SoundManager.playBossSpawn();
        SoundManager.startBossMusic(); // musique de boss dès l'apparition
    }

    @Override
    public void update() {
        if (beaten) {
            if (Math.abs(gp.player.x - serverX) < GamePanel.TILE_SIZE * 2 &&
                Math.abs(gp.player.y - serverY) < GamePanel.TILE_SIZE * 2) {
                serverReached = true;
                SoundManager.stopBossMusic();
                SoundManager.playWin();
            }
            return;
        }

        animTimer++;
        if (animTimer >= 12) { animTimer = 0; animFrame = (animFrame + 1) % 4; }
        if (hitFlash > 0) hitFlash--;

        if (hp < maxHp / 2 && phase == 1) {
            phase = 2;
            shootDelay = 40;
            speed = 3;
            spawnDelay = 180;
        }

        int dx = gp.player.x - x;
        int dy = gp.player.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > GamePanel.TILE_SIZE * 3) {
            x += (int)(dx / dist * speed);
            y += (int)(dy / dist * speed);
        }

        shootTimer++;
        if (shootTimer >= shootDelay) { shootAtPlayer(); shootTimer = 0; }

        spawnTimer++;
        if (spawnTimer >= spawnDelay) { spawnEnemy(); spawnTimer = 0; }

        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE * 2 &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE * 2) {
            gp.player.hp -= 2;
            SoundManager.playPlayerHit();
        }
    }

    private void spawnEnemy() {
        int ox = (Math.random() > 0.5) ? -GamePanel.TILE_SIZE * 3 : GamePanel.TILE_SIZE * 3;
        int oy = (Math.random() > 0.5) ? -GamePanel.TILE_SIZE * 2 : GamePanel.TILE_SIZE * 2;
        gp.enemies.add(new SpawnedEnemy(x + ox, y + oy, gp));
        gp.enemies.add(new SpawnedEnemy(x - ox, y - oy, gp));
    }

    private void shootAtPlayer() {
        int cx = x + GamePanel.TILE_SIZE;
        int cy = y + GamePanel.TILE_SIZE;
        int px = gp.player.x + GamePanel.TILE_SIZE / 2;
        int py = gp.player.y + GamePanel.TILE_SIZE / 2;
        double dist = Math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy));
        if (dist == 0) return;
        double ndx = (px - cx) / dist;
        double ndy = (py - cy) / dist;
        gp.projectiles.add(new BossProjectile(cx, cy, ndx, ndy, gp));
        int spread = (phase == 2) ? 3 : 1;
        double angle = Math.PI / 10;
        for (int i = 1; i <= spread; i++) {
            double a = angle * i;
            gp.projectiles.add(new BossProjectile(cx, cy,
                ndx * Math.cos(a) - ndy * Math.sin(a),
                ndx * Math.sin(a) + ndy * Math.cos(a), gp));
            gp.projectiles.add(new BossProjectile(cx, cy,
                ndx * Math.cos(-a) - ndy * Math.sin(-a),
                ndx * Math.sin(-a) + ndy * Math.cos(-a), gp));
        }
    }

    public boolean isDead()         { return false; }
    public boolean isBeaten()       { return beaten; }
    public boolean isServerReached(){ return serverReached; }

    public void takeDamage(int d) {
        if (beaten) return;
        hp -= d;
        hitFlash = 8;
        SoundManager.playHit();
        if (hp <= 0) {
            hp = 0;
            beaten = true;
        }
    }

    public void drawServer(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;
        int sx = serverX, sy = serverY;
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(sx - ts, sy + ts * 2, ts * 4, 8);
        g2.setColor(new Color(0, 40, 120));
        g2.fillRect(sx, sy, ts * 2, ts * 2);
        g2.setColor(Color.CYAN);
        g2.drawRect(sx, sy, ts * 2, ts * 2);
        g2.setColor(beaten ? Color.GREEN : Color.RED);
        g2.fillOval(sx + 8, sy + 8, 8, 8);
        g2.fillOval(sx + 22, sy + 8, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("MOODLE", sx + 6, sy + ts - 6);
        g2.drawString("SERVER", sx + 6, sy + ts + 8);
        if (beaten) {
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("▼ DEPOSE ICI ▼", sx - 30, sy - 6);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (beaten) return;
        int ts = GamePanel.TILE_SIZE;
        int bx = x, by = y;
        boolean rage = (phase == 2);
        int bob = (animFrame == 1 || animFrame == 3) ? 2 : 0;

        if (hitFlash > 0 && hitFlash % 2 == 0) {
            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillRect(bx, by, ts * 2, ts * 2);
            return;
        }

        g2.setColor(rage ? new Color(180, 0, 0) : new Color(0, 0, 120));
        g2.fillRect(bx + 6, by + 18 + bob, ts * 2 - 12, ts + 8);
        g2.setColor(new Color(255, 200, 150));
        g2.fillOval(bx + ts/2 - 8, by + bob, 24, 26);
        g2.setColor(new Color(200, 200, 200));
        g2.fillArc(bx + ts/2 - 8, by + bob - 2, 24, 14, 0, 180);
        g2.setColor(rage ? Color.RED : Color.BLACK);
        g2.fillOval(bx + ts/2 - 6, by + 8 + bob, 6, 6);
        g2.fillOval(bx + ts/2 + 3, by + 8 + bob, 6, 6);
        if (rage) {
            g2.setColor(new Color(255, 150, 0));
            g2.fillOval(bx + ts/2 - 4, by + 10 + bob, 3, 3);
            g2.fillOval(bx + ts/2 + 5, by + 10 + bob, 3, 3);
        }
        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.drawLine(bx + ts/2 - 7, by + 7 + bob, bx + ts/2 - 2, by + 10 + bob);
        g2.drawLine(bx + ts/2 + 2, by + 10 + bob, bx + ts/2 + 8, by + 7 + bob);
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.setColor(new Color(150, 50, 50));
        g2.drawArc(bx + ts/2 - 5, by + 15 + bob, 10, 5, 200, 140);
        g2.setColor(new Color(20, 20, 80));
        g2.fillRect(bx + ts/2 - 12, by + bob - 6, 24, 6);
        g2.fillRect(bx + ts/2 - 8, by + bob - 12, 16, 8);
        g2.setColor(Color.YELLOW);
        g2.fillRect(bx + ts/2 - 3, by + bob - 6, 2, 10);
        g2.setColor(new Color(255, 200, 150));
        g2.fillRect(bx + 2, by + 20 + bob, 6, 14);
        g2.fillRect(bx + ts * 2 - 8, by + 20 + bob, 6, 14);
        g2.setColor(new Color(200, 180, 50));
        g2.fillRect(bx + ts * 2 - 6, by + 14 + bob, 4, 22);
        int legBob = (animFrame % 2 == 0) ? 0 : 4;
        g2.setColor(rage ? new Color(140, 0, 0) : new Color(0, 0, 80));
        g2.fillRect(bx + 8,           by + ts + 18 + bob, 14, 12 + legBob);
        g2.fillRect(bx + ts * 2 - 22, by + ts + 18 + bob, 14, 12 - legBob);
        if (rage) {
            g2.setColor(new Color(255, 50, 0, 60 + (int)(Math.sin(animFrame * 0.5) * 40)));
            g2.fillOval(bx - 10, by - 10, ts * 2 + 20, ts * 2 + 20);
        }
        int barW = ts * 2;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(bx, by - 16, barW, 10);
        g2.setColor(rage ? Color.ORANGE : new Color(220, 0, 0));
        g2.fillRect(bx, by - 16, (int)((double) hp / maxHp * barW), 10);
        g2.setColor(Color.WHITE);
        g2.drawRect(bx, by - 16, barW, 10);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.setColor(Color.WHITE);
        g2.drawString("PROF " + hp + "/" + maxHp + (rage ? " [RAGE!]" : ""), bx, by - 18);
    }
}
