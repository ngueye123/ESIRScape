package tile;

import java.awt.image.BufferedImage;

// Represente une tuile de la carte
public class Tile {
    public BufferedImage image;
    public boolean collision; // vrai si on ne peut pas traverser cette tuile

    public Tile() {
        collision = false;
    }
}