package entity;

import main.GamePanel;
import projectile.UsbProjectile;

public abstract class Enemy extends Entity {

    protected GamePanel gp;
    protected int scoreValue;
    protected int shootDelay;
    protected int shootTimer = 0;

    public Enemy(int x, int y, GamePanel gp) {
        this.x  = x;
        this.y  = y;
        this.gp = gp;
    }

    protected void shootAtPlayer() {
        int cx = x + GamePanel.TILE_SIZE / 2;
        int cy = y + GamePanel.TILE_SIZE / 2;
        int px = gp.player.x + GamePanel.TILE_SIZE / 2;
        int py = gp.player.y + GamePanel.TILE_SIZE / 2;

        double dist = Math.sqrt((px-cx)*(px-cx) + (py-cy)*(py-cy));
        if (dist == 0) return;

        // Direction précise (pas juste -1/0/1)
        final double ndx = (px - cx) / dist;
        final double ndy = (py - cy) / dist;

        gp.projectiles.add(new UsbProjectile(cx, cy, (int)Math.round(ndx), (int)Math.round(ndy), gp) {
            // Surcharge pour conserver direction flottante
            private double fdx = ndx, fdy = ndy;
            @Override
            public void update() {
                x += (int)(fdx * speed);
                y += (int)(fdy * speed);
            }
            { this.ennemi = true; this.degats = 5; this.speed = 5; }
        });
    }

    public boolean isDead() { return hp <= 0; }
    public int getScoreValue() { return scoreValue; }
}
