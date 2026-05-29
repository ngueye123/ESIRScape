package projectile;

import java.awt.Graphics2D;
import main.GamePanel;

public abstract class Projectile {

    public int x, y;
    public int dx, dy;
    public int speed;
    public int degats;
    protected GamePanel gp;
    public boolean ennemi;
    public boolean destroyed = false;

    public Projectile(int x, int y, int dx, int dy, int speed, int degats, GamePanel gp, boolean ennemi) {
        this.x      = x;
        this.y      = y;
        this.dx     = dx;
        this.dy     = dy;
        this.speed  = speed;
        this.degats = degats;
        this.gp     = gp;
        this.ennemi = ennemi;
    }

    public boolean isOutOfScreen() {
        if (x < -20 || x > GamePanel.SCREEN_WIDTH + 20) return true;
        if (y < -20 || y > GamePanel.SCREEN_HEIGHT + 20) return true;
        if (destroyed) return true;
        // Collision mur
        int col = x / GamePanel.TILE_SIZE;
        int row = y / GamePanel.TILE_SIZE;
        if (gp.tileManager.isSolid(col, row)) { destroyed = true; return true; }
        return false;
    }

    public int getDegats()   { return degats; }
    public boolean isEnnemi() { return ennemi; }
    public int getX()        { return x; }
    public int getY()        { return y; }

    public abstract void update();
    public abstract void draw(Graphics2D g2);
}
