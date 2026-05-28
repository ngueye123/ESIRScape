package projectile;

import java.awt.Graphics2D;

import main.GamePanel;

// Classe abstraite representant un projectile dans le jeu
// UsbProjectile et PrintProjectile en heritent
public abstract class Projectile {

    protected int x, y;        // position du projectile
    protected int dx, dy;      // direction de deplacement
    protected int speed;       // vitesse
    protected int degats;      // degats infliges
    protected GamePanel gp;

    // Indique si ce projectile vient d un ennemi ou du joueur
    protected boolean ennemi;

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

    // Retourne true si le projectile est sorti de l ecran
    public boolean isOutOfScreen() {
        return x < 0 || x > GamePanel.SCREEN_WIDTH || y < 0 || y > GamePanel.SCREEN_HEIGHT;
    }

    public int getDegats() {
        return degats;
    }

    public boolean isEnnemi() {
        return ennemi;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public abstract void update();
    public abstract void draw(Graphics2D g2);
}
