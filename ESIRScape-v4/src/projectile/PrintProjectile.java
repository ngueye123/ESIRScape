package projectile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import main.GamePanel;

public class PrintProjectile extends Projectile {

    public PrintProjectile(int x, int y, int dx, int dy, int degats, GamePanel gp) {
        super(x, y, dx, dy, 9, degats, gp, false);
        if (dx == 0 && dy == 0) { this.dx = 1; this.dy = 0; }
    }

    @Override
    public void update() {
        x += dx * speed;
        y += dy * speed;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(255, 200, 50));
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        // Rotation simulée selon direction
        String[] labels = {">>", "vv", "<<", "^^"};
        String label = (dy < 0) ? "^^" : (dy > 0) ? "vv" : (dx < 0) ? "<<" : ">>";
        g2.drawString(label, x - 8, y + 4);
        // Halo
        g2.setColor(new Color(255, 230, 100, 100));
        g2.fillOval(x - 8, y - 8, 16, 16);
    }
}
