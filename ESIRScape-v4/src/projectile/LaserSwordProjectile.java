package projectile;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;

public class LaserSwordProjectile extends Projectile {

    private double fdx, fdy;
    private int trailTimer = 0;

    public LaserSwordProjectile(int x, int y, double fdx, double fdy, GamePanel gp) {
        super(x, y, (int)Math.round(fdx), (int)Math.round(fdy), 10, 35, gp, false);
        this.fdx = fdx;
        this.fdy = fdy;
    }

    @Override
    public void update() {
        x += (int)(fdx * speed);
        y += (int)(fdy * speed);
        trailTimer++;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Lame de lumière
        for (int i = 0; i < 8; i++) {
            float t = (float)i / 8;
            int r = (int)(180 * (1-t));
            int gv = (int)(80 * (1-t));
            int b = 255;
            int a = (int)(220 * (1-t));
            g2.setColor(new Color(r, gv, b, a));
            g2.fillOval(x - (int)(fdx * i * 2) - 3, y - (int)(fdy * i * 2) - 3, 6, 6);
        }
        // Pointe brillante
        g2.setColor(new Color(220, 200, 255, 240));
        g2.fillOval(x - 4, y - 4, 8, 8);
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(x - 2, y - 2, 4, 4);
    }
}
