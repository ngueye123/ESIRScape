package entity;

import java.awt.Graphics2D;

import main.GamePanel;
import projectile.UsbProjectile;

// Classe abstraite representant un ennemi generique
// BugEnemy et ExamenEnemy en heritent
public abstract class Enemy extends Entity {

    protected GamePanel gp;

    // Compteur pour gerer le tir de l ennemi
    protected int shootTimer   = 0;
    protected int shootDelay   = 120; // tire toutes les 2 secondes environ

    // Nombre de points accordes au joueur quand l ennemi meurt
    protected int scoreValue = 100;

    public Enemy(int x, int y, GamePanel gp) {
        this.x  = x;
        this.y  = y;
        this.gp = gp;
    }

    // Retourne true si l ennemi est mort
    public boolean isDead() {
        return hp <= 0;
    }

    // Retourne le score donne par cet ennemi
    public int getScoreValue() {
        return scoreValue;
    }

    // Reduit les HP de l ennemi quand il est touche
    public void takeDamage(int degats) {
        hp -= degats;
    }

    // Tire un projectile vers le joueur
    protected void shootAtPlayer() {
        int cx = x + GamePanel.TILE_SIZE / 2;
        int cy = y + GamePanel.TILE_SIZE / 2;

        int dx = gp.player.x - cx;
        int dy = gp.player.y - cy;

        // Normalise la direction
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        int ndx = (int)(dx / dist);
        int ndy = (int)(dy / dist);

        // Ajoute un projectile ennemi dans la liste
        gp.projectiles.add(new UsbProjectile(cx, cy, ndx, ndy, gp, true));
    }

    @Override
    public abstract void update();

    @Override
    public abstract void draw(Graphics2D g2);
}
