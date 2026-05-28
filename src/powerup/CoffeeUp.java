package powerup;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.GamePanel;

// Power up Tasse de Cafe
// 1ere tasse : vitesse moderement augmentee
// 2eme tasse : vitesse encore augmentee (mode sprint)
// La vitesse maximale est limitee a 7 pour rester jouable
public class CoffeeUp extends PowerUp {

    private static final int VITESSE_MAX = 7;

    public CoffeeUp(int x, int y, GamePanel gp) {
        super(x, y, gp);
    }

    @Override
    protected void applyEffect() {
        if (gp.player.coffeeLevel < 2) {
            gp.player.coffeeLevel++;
            // Augmente la vitesse selon le niveau de cafe
            if (gp.player.speed < VITESSE_MAX) {
                gp.player.speed += 2;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        // Carre marron representant une tasse
        g2.setColor(new Color(139, 69, 19));
        g2.fillRect(x + 8, y + 8, 30, 30);

        // Vapeur en haut
        g2.setColor(Color.WHITE);
        g2.drawArc(x + 12, y, 10, 12, 0, 180);

        // Texte cafe
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.drawString("cafe", x + 9, y + 27);
    }
}
