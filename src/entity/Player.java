package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GamePanel;
import main.KeyHandler;

// Represente le joueur (l'etudiant de l'ESIR)
public class Player extends Entity {

    private GamePanel gp;
    private KeyHandler keyH;

    // Arme active : 0 = cle USB, 1 = System.out.println()
    public int currentWeapon = 0;

    // Compteur pour eviter de tirer en continu
    private int shootCooldown = 0;

    // Compteur pour eviter de changer d'arme en continu
    private int switchCooldown = 0;

    // Niveau de cafe : 0 = normal, 1 = rapide, 2 = sprint
    public int coffeeLevel = 0;

    // Si la cle USB est amelioree
    public boolean usbUpgraded = false;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp   = gp;
        this.keyH = keyH;
        setDefaultValues();
        loadImage();
    }

    private void setDefaultValues() {
        x     = 100;
        y     = 100;
        speed = 3;
        maxHp = 100;
        hp    = maxHp;
    }

    // Charge l'image du joueur depuis les ressources
    private void loadImage() {
        try {
            image = ImageIO.read(getClass().getResource("/player/superhero.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Retourne le nom de l'arme active (pour l'affichage HUD)
    public String getWeaponName() {
        if (currentWeapon == 0) return "Cle USB";
        return "System.out.println()";
    }

    @Override
    public void update() {
        // Deplacement dans les 4 directions
        if (keyH.upPressed)    y -= speed;
        if (keyH.downPressed)  y += speed;
        if (keyH.leftPressed)  x -= speed;
        if (keyH.rightPressed) x += speed;

        // Empêche le joueur de sortir de l'ecran
        if (x < 0)                               x = 0;
        if (y < 0)                               y = 0;
        if (x > GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE) x = GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE;
        if (y > GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE) y = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE;

        // Changement d'arme avec E (cooldown pour eviter le spam)
        if (keyH.ePressed && switchCooldown == 0) {
            currentWeapon  = (currentWeapon == 0) ? 1 : 0;
            switchCooldown = 20;
        }
        if (switchCooldown > 0) switchCooldown--;

        // Tir avec espace (la creation des projectiles sera ajoutee a l'etape suivante)
        if (keyH.spacePressed && shootCooldown == 0) {
            // TODO : creer un projectile et l'ajouter a GamePanel
            shootCooldown = 15;
        }
        if (shootCooldown > 0) shootCooldown--;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, null);
    }
}