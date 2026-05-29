package projectile;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;

public class UsbProjectile extends Projectile {

    public UsbProjectile(int x, int y, int dx, int dy, GamePanel gp) {
        super(x, y, dx, dy, 7, 10, gp, false);
        // Normalise la direction si nécessaire
        if (dx == 0 && dy == 0) { this.dx = 1; this.dy = 0; }
    }

    @Override
    public void update() {
        x += dx * speed;
        y += dy * speed;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Clé USB selon direction
        g2.setColor(new Color(100, 100, 255));
        if (dy != 0 && dx == 0) {
            g2.fillRect(x - 3, y - 6, 6, 12);
            g2.setColor(new Color(200, 200, 255));
            g2.fillRect(x - 2, y - 9, 4, 4);
        } else {
            g2.fillRect(x - 6, y - 3, 12, 6);
            g2.setColor(new Color(200, 200, 255));
            g2.fillRect(x + (dx > 0 ? 5 : -9), y - 2, 4, 4);
        }
        // Traînée
        g2.setColor(new Color(150, 150, 255, 120));
        g2.fillOval(x - dx*4 - 2, y - dy*4 - 2, 4, 4);
    }
}
