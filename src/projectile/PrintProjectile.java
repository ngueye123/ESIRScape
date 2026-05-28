package projectile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import entity.Enemy;
import main.GamePanel;

// Projectile de type System.out.println() : lent, gros degats
// Affiche "sout" comme dans une console Java
public class PrintProjectile extends Projectile {

    // Constructeur pour le joueur
    public PrintProjectile(int x, int y, int dx, int dy, int degats, GamePanel gp) {
        super(x, y, dx, dy, 3, degats, gp, false);
    }

    // Constructeur generique joueur ou ennemi
    public PrintProjectile(int x, int y, int dx, int dy, int degats, GamePanel gp, boolean ennemi) {
        super(x, y, dx, dy, 3, degats, gp, ennemi);
    }

    @Override
    public void update() {
        x += dx * speed;
        y += dy * speed;

        // Stoppe le projectile s il touche un mur
        int col = x / GamePanel.TILE_SIZE;
        int row = y / GamePanel.TILE_SIZE;
        if (gp.tileManager.isSolid(col, row)) {
            x = -100;
            return;
        }

        if (ennemi) {
            // Touche le joueur
            if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
                    Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
                gp.player.hp -= degats;
                x = -100;
            }
        } else {
            // Touche les ennemis
            for (Enemy e : gp.enemies) {
                if (Math.abs(x - e.x) < GamePanel.TILE_SIZE &&
                        Math.abs(y - e.y) < GamePanel.TILE_SIZE) {
                    e.takeDamage(degats);
                    x = -100;
                    break;
                }
            }
            // Touche le boss : ne lui fait pas de degats, juste s arrete
            if (gp.boss != null) {
                if (Math.abs(x - gp.boss.x) < GamePanel.TILE_SIZE * 2 &&
                        Math.abs(y - gp.boss.y) < GamePanel.TILE_SIZE * 2) {
                    x = -100;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(ennemi ? Color.RED : Color.GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.drawString("sout", x - 10, y);
    }
}