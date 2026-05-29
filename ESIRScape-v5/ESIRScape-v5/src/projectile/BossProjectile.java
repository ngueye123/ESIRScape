package projectile;

import java.awt.Color;
import java.awt.Graphics2D;
import main.GamePanel;

public class BossProjectile extends Projectile {

    private double fdx, fdy;

    public BossProjectile(int x, int y, double fdx, double fdy, GamePanel gp) {
        super(x, y, 0, 0, 5, 15, gp, true);
        this.fdx = fdx;
        this.fdy = fdy;
    }

    @Override
    public void update() {
        x += (int)(fdx * speed);
        y += (int)(fdy * speed);
    }

    @Override
    public void draw(Graphics2D g2) {
        // Projectile rouge du boss
        g2.setColor(new Color(255, 50, 50));
        g2.fillOval(x - 6, y - 6, 12, 12);
        g2.setColor(new Color(255, 150, 50, 180));
        g2.fillOval(x - 9, y - 9, 18, 18);
        g2.setColor(Color.WHITE);
        g2.fillOval(x - 3, y - 3, 6, 6);
    }
}
