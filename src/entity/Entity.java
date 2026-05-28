package entity;

import java.awt.image.BufferedImage;

// Classe de base pour toutes les entites du jeu (joueur, ennemis...)
public abstract class Entity {

    public int x, y;          // position en pixels
    public int speed;         // vitesse de deplacement
    public int hp;            // points de vie actuels
    public int maxHp;         // points de vie maximum
    public BufferedImage image; // image affichee

    // Chaque entite doit pouvoir se mettre a jour et s'afficher
    public abstract void update();
    public abstract void draw(java.awt.Graphics2D g2);
}