package powerup;

import java.awt.Graphics2D;

import main.GamePanel;

// Classe abstraite representant un power up ramassable sur la map
// CoffeeUp et UsbUp en heritent
public abstract class PowerUp {

    protected int x, y;
    protected GamePanel gp;
    protected boolean collected = false; // true quand le joueur l a ramasse

    public PowerUp(int x, int y, GamePanel gp) {
        this.x  = x;
        this.y  = y;
        this.gp = gp;
    }

    // Retourne true si le power up a ete ramasse
    public boolean isCollected() {
        return collected;
    }

    // Verifie si le joueur est sur le power up et applique l effet
    public void update() {
        if (Math.abs(x - gp.player.x) < GamePanel.TILE_SIZE &&
            Math.abs(y - gp.player.y) < GamePanel.TILE_SIZE) {
            applyEffect();
            collected = true;
        }
    }

    // Applique l effet specifique du power up sur le joueur
    protected abstract void applyEffect();

    public abstract void draw(Graphics2D g2);
}
