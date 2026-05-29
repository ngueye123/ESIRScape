package entity;

import java.awt.Color;
import java.awt.Graphics2D;

import main.GamePanel;
import main.KeyHandler;
import main.SoundManager;
import projectile.LaserSwordProjectile;
import projectile.PrintProjectile;
import projectile.UsbProjectile;

public class Player extends Entity {

    private GamePanel gp;
    private KeyHandler keyH;

    public int currentWeapon = 0;
    public boolean usbUpgraded = false;
    public int coffeeLevel = 0;

    // 0 = Étudiant, 1 = Ninja
    public int characterType = 0;

    private int shootCooldown  = 0;
    private int switchCooldown = 0;
    private int swordSlashTimer = 0; // animation attaque ninja

    // Animation
    private int animTimer = 0;
    private int animFrame = 0;
    private boolean moving = false;

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp   = gp;
        this.keyH = keyH;
        setDefaultValues();
    }

    private void setDefaultValues() {
        x     = 80;
        y     = 250;
        speed = 4;
        maxHp = 100;
        hp    = maxHp;
    }

    public String getWeaponName() {
        if (characterType == 1) {
            return "Épée Laser";
        }
        if (currentWeapon == 0) return usbUpgraded ? "Clé USB x3" : "Clé USB";
        return usbUpgraded ? "println() x2" : "System.out.println()";
    }

    @Override
    public void update() {
        int nextX = x;
        int nextY = y;
        moving = false;

        if (keyH.upPressed)    { nextY -= speed; moving = true; }
        if (keyH.downPressed)  { nextY += speed; moving = true; }
        if (keyH.leftPressed)  { nextX -= speed; moving = true; }
        if (keyH.rightPressed) { nextX += speed; moving = true; }

        // Collision horizontale
        int colGauche = nextX / GamePanel.TILE_SIZE;
        int colDroite = (nextX + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;
        int rowHaut   = y / GamePanel.TILE_SIZE;
        int rowBas    = (y + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;

        boolean blocX = gp.tileManager.isSolid(colGauche, rowHaut)
                || gp.tileManager.isSolid(colGauche, rowBas)
                || gp.tileManager.isSolid(colDroite, rowHaut)
                || gp.tileManager.isSolid(colDroite, rowBas);
        if (!blocX) x = nextX;

        // Collision verticale
        int colG = x / GamePanel.TILE_SIZE;
        int colD = (x + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;
        int rowH = nextY / GamePanel.TILE_SIZE;
        int rowB = (nextY + GamePanel.TILE_SIZE - 1) / GamePanel.TILE_SIZE;

        boolean blocY = gp.tileManager.isSolid(colG, rowH)
                || gp.tileManager.isSolid(colG, rowB)
                || gp.tileManager.isSolid(colD, rowH)
                || gp.tileManager.isSolid(colD, rowB);
        if (!blocY) y = nextY;

        // Limites écran
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE) x = GamePanel.SCREEN_WIDTH  - GamePanel.TILE_SIZE;
        if (y > GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE) y = GamePanel.SCREEN_HEIGHT - GamePanel.TILE_SIZE;

        // Animation
        if (moving) {
            animTimer++;
            if (animTimer >= 8) { animTimer = 0; animFrame = (animFrame + 1) % 4; }
        } else {
            animFrame = 0;
        }

        if (swordSlashTimer > 0) swordSlashTimer--;

        // Ninja : pas de changement d'arme, toujours épée laser
        if (characterType != 1) {
            if (keyH.ePressed && switchCooldown == 0) {
                currentWeapon  = (currentWeapon == 0) ? 1 : 0;
                switchCooldown = 20;
                keyH.ePressed = false;
            }
        }
        if (switchCooldown > 0) switchCooldown--;

        // Tir
        if (keyH.spacePressed && shootCooldown == 0) {
            shoot();
            SoundManager.playShoot();
            shootCooldown = (characterType == 1) ? 8 : 12; // ninja tire plus vite
        }
        if (shootCooldown > 0) shootCooldown--;
    }

    private void shoot() {
        int centreX = x + GamePanel.TILE_SIZE / 2;
        int centreY = y + GamePanel.TILE_SIZE / 2;

        // Tir intelligent : direction vers l'ennemi le plus proche
        double[] dir = gp.getNearestEnemyDirectionFloat(centreX, centreY);
        double fdx = dir[0];
        double fdy = dir[1];
        int dx = (int)Math.round(fdx);
        int dy = (int)Math.round(fdy);

        if (characterType == 1) {
            // Ninja : attaque avec épée laser
            swordSlashTimer = 15;
            // L'épée laser envoie un projectile rapide vers la cible
            gp.projectiles.add(new LaserSwordProjectile(centreX, centreY, fdx, fdy, gp));
            // Dégâts de zone de mêlée si ennemi proche
            for (entity.Enemy e : gp.enemies) {
                double d = Math.sqrt(Math.pow(e.x - x, 2) + Math.pow(e.y - y, 2));
                if (d < GamePanel.TILE_SIZE * 1.5) {
                    e.hp -= 25;
                    gp.spawnParticles(e.x, e.y, new Color(180, 80, 255), 6);
                }
            }
        } else {
            if (currentWeapon == 0) {
                if (usbUpgraded) {
                    gp.projectiles.add(new UsbProjectile(centreX, centreY, dx, dy, gp));
                    int pdx = -dy, pdy = dx;
                    gp.projectiles.add(new UsbProjectile(centreX, centreY, dx + pdx, dy + pdy, gp));
                    gp.projectiles.add(new UsbProjectile(centreX, centreY, dx - pdx, dy - pdy, gp));
                } else {
                    gp.projectiles.add(new UsbProjectile(centreX, centreY, dx, dy, gp));
                }
            } else {
                int degats = usbUpgraded ? 40 : 20;
                gp.projectiles.add(new PrintProjectile(centreX, centreY, dx, dy, degats, gp));
                if (usbUpgraded) {
                    gp.projectiles.add(new PrintProjectile(centreX, centreY, dx, dy, degats, gp) {{
                        this.x += 8; this.y += 8;
                    }});
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (characterType == 1) {
            drawNinja(g2);
        } else {
            drawStudent(g2);
        }
    }

    private void drawStudent(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;
        int bob = (animFrame == 1 || animFrame == 3) ? 2 : 0;

        // Corps
        g2.setColor(new Color(30, 80, 180));
        g2.fillRect(x + 10, y + 18 + bob, 28, 20);
        // Tête
        g2.setColor(new Color(255, 210, 170));
        g2.fillRoundRect(x + 12, y + 2, 24, 22, 8, 8);
        // Cheveux
        g2.setColor(new Color(80, 40, 10));
        g2.fillRect(x + 12, y + 2, 24, 8);
        g2.fillRect(x + 12, y + 2, 5, 14);
        // Yeux
        g2.setColor(Color.WHITE);
        g2.fillOval(x + 15, y + 10, 7, 6);
        g2.fillOval(x + 25, y + 10, 7, 6);
        g2.setColor(new Color(50, 50, 200));
        g2.fillOval(x + 17, y + 11, 4, 4);
        g2.fillOval(x + 27, y + 11, 4, 4);
        // Bouche
        g2.setColor(new Color(180, 90, 80));
        g2.fillRect(x + 19, y + 19, 10, 3);
        // Sac à dos
        g2.setColor(new Color(160, 100, 30));
        g2.fillRect(x + 2, y + 16 + bob, 10, 18);
        g2.setColor(new Color(200, 140, 50));
        g2.fillRect(x + 3, y + 20 + bob, 7, 3);
        g2.fillRect(x + 3, y + 26 + bob, 7, 3);
        // Jambes
        int legOffset = (animFrame % 2 == 0) ? 0 : 3;
        g2.setColor(new Color(50, 90, 160));
        g2.fillRect(x + 12, y + 36 + bob, 11, 10 - legOffset);
        g2.fillRect(x + 25, y + 36 + bob, 11, 10 + legOffset);
        // Chaussures
        g2.setColor(new Color(40, 40, 40));
        g2.fillRect(x + 10, y + 44 + bob, 14, 4);
        g2.fillRect(x + 24, y + 44 + bob, 14, 4);
        // Bras
        g2.setColor(new Color(255, 210, 170));
        g2.fillRect(x + 36, y + 18 + bob, 7, 14);
        // Indicateur direction
        int idx = keyH.lastDirX;
        int idy = keyH.lastDirY;
        int arrowX = x + ts/2 + idx * 28;
        int arrowY = y + ts/2 + idy * 28;
        g2.setColor(new Color(255, 255, 0, 180));
        g2.fillOval(arrowX - 3, arrowY - 3, 6, 6);

        if (currentWeapon == 0) {
            g2.setColor(new Color(180, 180, 180));
            g2.fillRect(x + 40, y + 14 + bob, 6, 8);
            g2.setColor(new Color(50, 50, 200));
            g2.fillRect(x + 42, y + 12 + bob, 3, 4);
        } else {
            g2.setColor(new Color(200, 50, 50));
            g2.fillRect(x + 38, y + 10 + bob, 8, 10);
            g2.setColor(Color.WHITE);
            g2.fillRect(x + 40, y + 12 + bob, 4, 1);
            g2.fillRect(x + 40, y + 14 + bob, 4, 1);
        }

        // Barre de vie
        drawHpBar(g2, ts);
    }

    private void drawNinja(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;
        int bob = (animFrame == 1 || animFrame == 3) ? 2 : 0;

        // Teint noir clair
        Color skin = new Color(160, 120, 90);

        // Corps - combinaison sombre
        g2.setColor(new Color(25, 15, 35));
        g2.fillRect(x + 8, y + 18 + bob, 32, 24);
        // Ceinture
        g2.setColor(new Color(80, 0, 100));
        g2.fillRect(x + 8, y + 36 + bob, 32, 5);

        // Tête
        g2.setColor(skin);
        g2.fillRoundRect(x + 10, y + 2, 28, 22, 10, 10);
        // Masque ninja
        g2.setColor(new Color(15, 8, 25));
        g2.fillRect(x + 10, y + 2, 28, 9);
        g2.fillRect(x + 10, y + 17, 28, 7);
        // Yeux violets lumineux
        g2.setColor(new Color(180, 80, 255));
        g2.fillOval(x + 12, y + 10, 9, 8);
        g2.fillOval(x + 27, y + 10, 9, 8);
        g2.setColor(new Color(230, 180, 255));
        g2.fillOval(x + 14, y + 11, 5, 5);
        g2.fillOval(x + 29, y + 11, 5, 5);

        // Bandeau frontal violet
        g2.setColor(new Color(100, 0, 140));
        g2.fillRect(x + 10, y + 3, 28, 5);
        g2.setColor(new Color(180, 0, 220));
        g2.drawRect(x + 10, y + 3, 28, 5);
        // Kanji sur le bandeau
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 7));
        g2.drawString("忍", x + 22, y + 8);

        // Jambes animées
        int legOffset = (animFrame % 2 == 0) ? 0 : 3;
        g2.setColor(new Color(20, 12, 30));
        g2.fillRect(x + 10, y + 40 + bob, 12, 12 - legOffset);
        g2.fillRect(x + 26, y + 40 + bob, 12, 12 + legOffset);
        // Chaussures
        g2.setColor(new Color(30, 20, 40));
        g2.fillRect(x + 8, y + 50 + bob, 16, 5);
        g2.fillRect(x + 24, y + 50 + bob, 16, 5);

        // Bras gauche
        g2.setColor(skin);
        g2.fillRect(x + 2, y + 20 + bob, 8, 14);

        // ÉPÉE LASER (à droite du perso) avec animation d'attaque
        int swordX = x + 42;
        int swordY = y + 2 + bob;
        int swordLen = swordSlashTimer > 0 ? 65 : 52;

        // Aura d'attaque
        if (swordSlashTimer > 0) {
            int slashAlpha = swordSlashTimer * 12;
            g2.setColor(new Color(180, 80, 255, Math.min(200, slashAlpha)));
            g2.fillOval(x + 20 - 20, y - 20, 90, 90);
            g2.setColor(new Color(255, 255, 255, Math.min(100, slashAlpha / 2)));
            g2.fillOval(x + 20 - 10, y - 10, 70, 70);
        }

        gp.drawLaserSword(g2, swordX, swordY, swordLen, true);

        // Barre de vie
        drawHpBar(g2, ts);
    }

    private void drawHpBar(Graphics2D g2, int ts) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y - 10, ts, 5);
        g2.setColor(Color.RED);
        int lv = (int)((double) hp / maxHp * ts);
        g2.fillRect(x, y - 10, lv, 5);
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y - 10, ts, 5);
    }
}
