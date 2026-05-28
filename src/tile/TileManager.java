package tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import main.GamePanel;

// Gere le chargement et l affichage de la carte du jeu
// La carte est stockee dans un fichier texte avec des numeros de tuiles
public class TileManager {

    private GamePanel gp;
    private Tile[] tiles;    // les types de tuiles disponibles
    private int[][] mapData; // la grille de la carte (col, row)

    public TileManager(GamePanel gp) {
        this.gp  = gp;
        tiles    = new Tile[10];
        mapData  = new int[GamePanel.MAX_SCREEN_COL][GamePanel.MAX_SCREEN_ROW];
        loadTileImages();
        loadMap("/maps/map1.txt");
    }

    // Charge les images des tuiles depuis le dossier res/tiles
    private void loadTileImages() {
        try {
            // 0 = sol du couloir (pas de collision)
            tiles[0] = new Tile();
            tiles[0].image = ImageIO.read(getClass().getResource("/tiles/GRASS.png"));

            // 1 = mur (collision active)
            tiles[1] = new Tile();
            tiles[1].image     = ImageIO.read(getClass().getResource("/tiles/BRICK.png"));
            tiles[1].collision = true;

            // 2 = bureau (decoration, pas de collision)
            tiles[2] = new Tile();
            tiles[2].image = ImageIO.read(getClass().getResource("/tiles/SAND.png"));

            // 3 = serveur / bloc solide (collision active)
            tiles[3] = new Tile();
            tiles[3].image     = ImageIO.read(getClass().getResource("/tiles/BRICK2.png"));
            tiles[3].collision = true;

            // 4 = eau (decoration)
            tiles[4] = new Tile();
            tiles[4].image = ImageIO.read(getClass().getResource("/tiles/WATER.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Charge une carte depuis un fichier texte
    // Chaque ligne du fichier correspond a une ligne de tuiles
    // Chaque chiffre correspond a un type de tuile
    public void loadMap(String filePath) {
        try {
            InputStream is     = getClass().getResourceAsStream(filePath);
            BufferedReader br  = new BufferedReader(new InputStreamReader(is));

            for (int row = 0; row < GamePanel.MAX_SCREEN_ROW; row++) {
                String line      = br.readLine();
                String[] tokens  = line.trim().split(" ");
                for (int col = 0; col < GamePanel.MAX_SCREEN_COL; col++) {
                    mapData[col][row] = Integer.parseInt(tokens[col]);
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Retourne true si la tuile a cette position bloque le deplacement
    public boolean isSolid(int col, int row) {
        if (col < 0 || col >= GamePanel.MAX_SCREEN_COL) return true;
        if (row < 0 || row >= GamePanel.MAX_SCREEN_ROW) return true;
        int index = mapData[col][row];
        return tiles[index].collision;
    }

    // Dessine toutes les tuiles de la carte
    public void draw(Graphics2D g2) {
        for (int row = 0; row < GamePanel.MAX_SCREEN_ROW; row++) {
            for (int col = 0; col < GamePanel.MAX_SCREEN_COL; col++) {
                int tileIndex = mapData[col][row];
                int x         = col * GamePanel.TILE_SIZE;
                int y         = row * GamePanel.TILE_SIZE;
                g2.drawImage(tiles[tileIndex].image, x, y, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE, null);
            }
        }
    }
}
