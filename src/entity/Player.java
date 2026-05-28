package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;
import projectile.PrintProjectile;
import projectile.Projectile;
import projectile.UsbProjectile;

// Represente le joueur : l etudiant de l ESIR
public class Player extends Entity {

    private GamePanel gp;
    private KeyHandler keyH;

    // Arme active : 0 = cle USB, 1 = System.out.println()
    public int currentWeapon = 0;

    // Ameliorations
    public boolean usbUpgraded   = false;
    public int coffeeLevel       = 0;

    // Cooldowns pour eviter le spam
    private int shootCooldown  = 0;
    private int switchCooldown = 0;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp   = gp;
        this.keyH = keyH;
        setDefaultValues();
        loadImage();
    }

    private void setDefaultValues() {
        x     = 80;
        y     = 250;
        speed = 3;
        maxHp = 100;
        hp    = maxHp;
    }

    private void loadImage() {
        try {
            image = ImageIO.read(getClass().getResource("/player/superhero.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Retourne le nom de l arme active pour l affichage dans le HUD
    public String getWeaponName() {
        if (currentWeapon == 0) {
            return usbUpgraded ? "Cle USB x3" : "Cle USB";
        }
        return usbUpgraded ? "println() x2" : "System.out.println()";
    }

    @Override
    public void update() {

        // Deplacement dans les 4 directions
        if (keyH.upPressed)    y -= speed;
        if (keyH.downPressed)  y += speed;
        if (keyH.leftPressed)  x -= speed;
        if (keyH.rightPressed) x += speed;

        // Empêche de sortir de l ecran
        if (x < 0)                                             x = 0;
        if (y < 0)                                             y = 0;
        if (x > GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE) x = GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE;
        if (y > GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE) y = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE;

        // Collision avec les murs de la tilemap
        int col = x / GamePanel.TILE_SIZE;
        int row = y / GamePanel.TILE_SIZE;
        if (gp.tileManager.isSolid(col, row)) {
            // On recule d un pixel pour sortir du mur
            if (keyH.upPressed)    y += speed;
            if (keyH.downPressed)  y -= speed;
            if (keyH.leftPressed)  x += speed;
            if (keyH.rightPressed) x -= speed;
        }

        // Changement d arme avec E
        if (keyH.ePressed && switchCooldown == 0) {
            currentWeapon  = (currentWeapon == 0) ? 1 : 0;
            switchCooldown = 20;
        }
        if (switchCooldown > 0) switchCooldown--;

        // Tir avec espace
        if (keyH.spacePressed && shootCooldown == 0) {
            shoot();
            shootCooldown = 15;
        }
        if (shootCooldown > 0) shootCooldown--;
    }

    // Cree les projectiles selon l arme active et les ameliorations
    private void shoot() {
        int centreX = x + GamePanel.TILE_SIZE / 2;
        int centreY = y + GamePanel.TILE_SIZE / 2;

        if (currentWeapon == 0) {
            // Cle USB
            if (usbUpgraded) {
                // Tir en eventail : 3 projectiles
                gp.projectiles.add(new UsbProjectile(centreX, centreY, 1, 0, gp));
                gp.projectiles.add(new UsbProjectile(centreX, centreY, 1, -1, gp));
                gp.projectiles.add(new UsbProjectile(centreX, centreY, 1, 1, gp));
            } else {
                gp.projectiles.add(new UsbProjectile(centreX, centreY, 1, 0, gp));
            }
        } else {
            // System.out.println()
            int degats = usbUpgraded ? 40 : 20;
            gp.projectiles.add(new PrintProjectile(centreX, centreY, 1, 0, degats, gp));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, null);
    }
}
