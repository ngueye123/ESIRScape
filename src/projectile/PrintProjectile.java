package projectile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import entity.Enemy;
import main.GamePanel;

// Projectile de type System.out.println()
// Lent mais gros degats (20 par defaut, doublable avec amelioration)
// Affiche du texte sur l ecran pour l effet visuel
public class PrintProjectile extends Projectile {

    // Constructeur pour le joueur (sans flag ennemi)
    public PrintProjectile(int x, int y, int dx, int dy, int degats, GamePanel gp) {
        super(x, y, dx, dy, 3, degats, gp, false);
    }

    // Constructeur generique (joueur ou ennemi)
    public PrintProjectile(int x, int y, int dx, int dy, int degats, GamePanel gp, boolean ennemi) {
        super(x, y, dx, dy, 3, degats, gp, ennemi);
    }

    @Override
    public void update() {
        x += dx * speed;
        y += dy * speed;

        if (ennemi) {
            // Projectile ennemi : touche le joueur
            if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
                Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
                gp.player.hp -= degats;
                x = -100;
            }
        } else {
            // Projectile joueur : touche les ennemis et le boss
            for (Enemy e : gp.enemies) {
                if (Math.abs(x - e.x) < GamePanel.TILE_SIZE &&
                    Math.abs(y - e.y) < GamePanel.TILE_SIZE) {
                    e.takeDamage(degats);
                    x = -100;
                    break;
                }
            }
            if (gp.boss != null) {
                if (Math.abs(x - gp.boss.x) < GamePanel.TILE_SIZE * 2 &&
                    Math.abs(y - gp.boss.y) < GamePanel.TILE_SIZE * 2) {
                    gp.boss.takeDamage(degats);
                    x = -100;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Affiche le texte "sout" en vert comme dans une console Java
        g2.setColor(ennemi ? Color.RED : Color.GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.drawString("sout", x - 10, y);
    }
}
