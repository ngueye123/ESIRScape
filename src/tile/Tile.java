package tile;

import java.awt.image.BufferedImage;

// Represente une tuile de la carte du jeu
public class Tile {

    public BufferedImage image;
    public boolean collision; // si true, on ne peut pas traverser cette tuile

    public Tile() {
        collision = false;
    }
}
