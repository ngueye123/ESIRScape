package powerup;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import main.GamePanel;

// Power up Cle USB amelioree
// Si arme active = Cle USB : tir en eventail (3 projectiles au lieu de 1)
// Si arme active = System.out.println() : degats doubles
public class UsbUp extends PowerUp {

    public UsbUp(int x, int y, GamePanel gp) {
        super(x, y, gp);
    }

    @Override
    protected void applyEffect() {
        // Active l amelioration sur le joueur
        gp.player.usbUpgraded = true;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Rectangle gris representant une cle USB
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(x + 5, y + 12, 35, 20);

        // Connecteur USB
        g2.setColor(Color.GRAY);
        g2.fillRect(x + 30, y + 17, 14, 10);

        // Etoile pour indiquer l amelioration
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("+", x + 13, y + 27);
    }
}
