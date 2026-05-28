package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Classe de base pour toutes les entites du jeu
// Joueur, ennemis et boss en heritent
public abstract class Entity {

    public int x, y;           // position en pixels
    public int speed;          // vitesse de deplacement
    public int hp;             // points de vie actuels
    public int maxHp;          // points de vie maximum
    public BufferedImage image; // image affichee a l ecran

    // Chaque entite doit savoir se mettre a jour et se dessiner
    public abstract void update();
    public abstract void draw(Graphics2D g2);
}
