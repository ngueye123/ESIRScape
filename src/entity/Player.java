package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.GamePanel;
import main.KeyHandler;
import projectile.PrintProjectile;
import projectile.UsbProjectile;

// Represente le joueur : l etudiant de l ESIR
public class Player extends Entity {

    private GamePanel gp;
    private KeyHandler keyH;

    // Arme active : 0 = cle USB, 1 = System.out.println()
    public int currentWeapon = 0;

    // Ameliorations
    public boolean usbUpgraded = false;
    public int coffeeLevel     = 0;

    // Cooldowns pour eviter le spam
    private int shootCooldown  = 0;
    private int switchCooldown = 0;

    // Direction du dernier deplacement : sert a orienter le tir
    private int lastDirX = 1;
    private int lastDirY = 0;

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

    // Retourne le nom de l arme active pour le HUD
    public String getWeaponName() {
        if (currentWeapon == 0) {
            return usbUpgraded ? "Cle USB x3" : "Cle USB";
        }
        return usbUpgraded ? "println() x2" : "System.out.println()";
    }

    @Override
    public void update() {

        // Calcule la prochaine position selon les touches pressees
        int nextX = x;
        int nextY = y;

        // Deplacement dans les 4 directions + memorisation de la direction
        if (keyH.upPressed)    { nextY -= speed; lastDirX =  0; lastDirY = -1; }
        if (keyH.downPressed)  { nextY += speed; lastDirX =  0; lastDirY =  1; }
        if (keyH.leftPressed)  { nextX -= speed; lastDirX = -1; lastDirY =  0; }
        if (keyH.rightPressed) { nextX += speed; lastDirX =  1; lastDirY =  0; }

        // Collision horizontale : verifie les 4 coins du joueur sur l axe X
        int colGauche = nextX / GamePanel.TILE_SIZE;
        int colDroite = (nextX + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;
        int rowHaut   = y / GamePanel.TILE_SIZE;
        int rowBas    = (y + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;

        boolean blocX = gp.tileManager.isSolid(colGauche, rowHaut)
                || gp.tileManager.isSolid(colGauche, rowBas)
                || gp.tileManager.isSolid(colDroite, rowHaut)
                || gp.tileManager.isSolid(colDroite, rowBas);

        if (!blocX) {
            x = nextX;
        }

        // Collision verticale : verifie les 4 coins du joueur sur l axe Y
        int colG = x / GamePanel.TILE_SIZE;
        int colD = (x + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;
        int rowH = nextY / GamePanel.TILE_SIZE;
        int rowB = (nextY + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;

        boolean blocY = gp.tileManager.isSolid(colG, rowH)
                || gp.tileManager.isSolid(colG, rowB)
                || gp.tileManager.isSolid(colD, rowH)
                || gp.tileManager.isSolid(colD, rowB);

        if (!blocY) {
            y = nextY;
        }

        // Empêche de sortir de l ecran
        if (x < 0)                                             x = 0;
        if (y < 0)                                             y = 0;
        if (x > GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE) x = GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE;
        if (y > GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE) y = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE;

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
                // Tir en eventail : direction principale + deux diagonales
                gp.projectiles.add(new UsbProjectile(centreX, centreY, lastDirX,             lastDirY,             gp));
                gp.projectiles.add(new UsbProjectile(centreX, centreY, lastDirX - lastDirY,  lastDirY + lastDirX,  gp));
                gp.projectiles.add(new UsbProjectile(centreX, centreY, lastDirX + lastDirY,  lastDirY - lastDirX,  gp));
            } else {
                gp.projectiles.add(new UsbProjectile(centreX, centreY, lastDirX, lastDirY, gp));
            }
        } else {
            // System.out.println()
            int degats = usbUpgraded ? 40 : 20;
            gp.projectiles.add(new PrintProjectile(centreX, centreY, lastDirX, lastDirY, degats, gp));
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.drawImage(image, x, y, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, null);
    }
}