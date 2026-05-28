package projectile;

import java.awt.Color;
import java.awt.Graphics2D;

import entity.Enemy;
import main.GamePanel;

// Projectile de type Cle USB
// Rapide mais faibles degats (10)
// Peut etre tire par le joueur ou par un ennemi
public class UsbProjectile extends Projectile {

    // Constructeur pour le joueur
    public UsbProjectile(int x, int y, int dx, int dy, GamePanel gp) {
        super(x, y, dx, dy, 8, 10, gp, false);
    }

    // Constructeur generique (joueur ou ennemi)
    public UsbProjectile(int x, int y, int dx, int dy, GamePanel gp, boolean ennemi) {
        super(x, y, dx, dy, 5, 10, gp, ennemi);
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
                x = -100; // sort de l ecran pour etre supprime
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
        // Petit carre jaune representant la cle USB
        g2.setColor(ennemi ? Color.ORANGE : Color.YELLOW);
        g2.fillRect(x - 4, y - 4, 8, 8);
    }
}
