package tile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import main.GamePanel;

// TileManager avec décor d'école dessiné en code (sans images)
public class TileManager {

    private GamePanel gp;
    private Tile[] tiles;
    public int[][] mapData;

    public TileManager(GamePanel gp) {
        this.gp   = gp;
        tiles     = new Tile[15];
        mapData   = new int[GamePanel.MAX_SCREEN_COL][GamePanel.MAX_SCREEN_ROW];
        setupTiles();
        loadMap("/maps/map1.txt");
    }

    private void setupTiles() {
        // 0 = sol carrelage
        tiles[0] = new Tile(); tiles[0].collision = false;
        // 1 = mur (collision)
        tiles[1] = new Tile(); tiles[1].collision = true;
        // 2 = bureau (collision)
        tiles[2] = new Tile(); tiles[2].collision = true;
        // 3 = mur solide
        tiles[3] = new Tile(); tiles[3].collision = true;
        // 5 = porte (traversable)
        tiles[5] = new Tile(); tiles[5].collision = false;
        // 6 = plateforme boss (collision)
        tiles[6] = new Tile(); tiles[6].collision = true;
    }

    public void loadMap(String filePath) {
        try {
            InputStream is    = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (int row = 0; row < GamePanel.MAX_SCREEN_ROW; row++) {
                String line     = br.readLine();
                String[] tokens = line.trim().split(" ");
                for (int col = 0; col < GamePanel.MAX_SCREEN_COL; col++) {
                    mapData[col][row] = Integer.parseInt(tokens[col]);
                }
            }
            br.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isSolid(int col, int row) {
        if (col < 0 || col >= GamePanel.MAX_SCREEN_COL) return true;
        if (row < 0 || row >= GamePanel.MAX_SCREEN_ROW) return true;
        int index = mapData[col][row];
        if (index < 0 || index >= tiles.length || tiles[index] == null) return false;
        return tiles[index].collision;
    }

    public int getTileType(int col, int row) {
        if (col < 0 || col >= GamePanel.MAX_SCREEN_COL) return -1;
        if (row < 0 || row >= GamePanel.MAX_SCREEN_ROW) return -1;
        return mapData[col][row];
    }

    public void draw(Graphics2D g2) {
        int ts = GamePanel.TILE_SIZE;

        // Déterminer si on est en salle boss (niveau 2)
        boolean isBoss = (gp.currentLevel == 2 || gp.gameState.equals("BOSS_FIGHT"));

        for (int row = 0; row < GamePanel.MAX_SCREEN_ROW; row++) {
            for (int col = 0; col < GamePanel.MAX_SCREEN_COL; col++) {
                int tileType = mapData[col][row];
                int x = col * ts;
                int y = row * ts;

                switch (tileType) {
                    case 0: drawFloor(g2, x, y, ts, isBoss, col, row); break;
                    case 1: drawWall(g2, x, y, ts, isBoss); break;
                    case 2: drawDesk(g2, x, y, ts); break;
                    case 5: drawDoor(g2, x, y, ts); break;
                    case 6: drawPlatform(g2, x, y, ts); break;
                    default: drawFloor(g2, x, y, ts, isBoss, col, row); break;
                }
            }
        }
    }

    private void drawFloor(Graphics2D g2, int x, int y, int ts, boolean isBoss, int col, int row) {
        if (isBoss) {
            // Sol salle serveur : carrelage sombre
            boolean alt = (col + row) % 2 == 0;
            g2.setColor(alt ? new Color(25, 30, 50) : new Color(30, 36, 60));
            g2.fillRect(x, y, ts, ts);
            // Grille
            g2.setColor(new Color(40, 50, 80, 80));
            g2.drawRect(x, y, ts, ts);
        } else {
            // Sol carrelage d'école blanc/gris
            boolean alt = (col + row) % 2 == 0;
            g2.setColor(alt ? new Color(235, 235, 230) : new Color(215, 215, 210));
            g2.fillRect(x, y, ts, ts);
            // Joint de carrelage
            g2.setColor(new Color(180, 180, 175, 120));
            g2.drawRect(x + 2, y + 2, ts - 4, ts - 4);
        }
    }

    private void drawWall(Graphics2D g2, int x, int y, int ts, boolean isBoss) {
        if (isBoss) {
            // Mur serveur : panneaux métalliques
            g2.setColor(new Color(40, 50, 80));
            g2.fillRect(x, y, ts, ts);
            g2.setColor(new Color(60, 70, 110));
            g2.drawRect(x + 2, y + 2, ts - 4, ts - 4);
            // Rivets
            g2.setColor(new Color(80, 90, 130));
            g2.fillOval(x + 4, y + 4, 5, 5);
            g2.fillOval(x + ts - 9, y + 4, 5, 5);
            g2.fillOval(x + 4, y + ts - 9, 5, 5);
            g2.fillOval(x + ts - 9, y + ts - 9, 5, 5);
        } else {
            // Mur école : briques avec contour
            g2.setColor(new Color(160, 90, 60));
            g2.fillRect(x, y, ts, ts);
            // Motif brique
            g2.setColor(new Color(130, 70, 45));
            for (int by = 0; by < ts; by += 12) {
                int off = ((by / 12) % 2 == 0) ? 0 : ts / 2;
                for (int bx = -ts/2; bx < ts; bx += ts) {
                    g2.fillRect(x + bx + off + 1, y + by + 1, ts - 4, 10);
                }
            }
            // Contour
            g2.setColor(new Color(100, 50, 30));
            g2.drawRect(x, y, ts, ts);
        }
    }

    private void drawDesk(Graphics2D g2, int x, int y, int ts) {
        // Bureau scolaire : surface bois
        g2.setColor(new Color(160, 110, 60));
        g2.fillRect(x + 2, y + 4, ts - 4, ts - 8);
        // Dessus du bureau
        g2.setColor(new Color(200, 150, 90));
        g2.fillRect(x + 2, y + 4, ts - 4, 6);
        // Pieds
        g2.setColor(new Color(100, 70, 30));
        g2.fillRect(x + 4, y + ts - 8, 6, 8);
        g2.fillRect(x + ts - 10, y + ts - 8, 6, 8);
        // Objet sur le bureau (livre ou feuille)
        if ((x / ts + y / ts) % 2 == 0) {
            // Livre
            g2.setColor(new Color(50, 80, 180));
            g2.fillRect(x + 8, y + 8, 14, 18);
            g2.setColor(Color.WHITE);
            g2.fillRect(x + 10, y + 12, 10, 2);
            g2.fillRect(x + 10, y + 16, 10, 2);
        } else {
            // Feuille
            g2.setColor(Color.WHITE);
            g2.fillRect(x + 10, y + 10, 16, 20);
            g2.setColor(new Color(200, 200, 200));
            for (int i = 0; i < 3; i++)
                g2.fillRect(x + 12, y + 13 + i * 5, 12, 1);
        }
        // Contour
        g2.setColor(new Color(80, 50, 20));
        g2.drawRect(x + 2, y + 4, ts - 4, ts - 8);
    }

    private void drawDoor(Graphics2D g2, int x, int y, int ts) {
        // Porte de salle de classe
        g2.setColor(new Color(120, 80, 30));
        g2.fillRect(x + 4, y, ts - 8, ts);
        g2.setColor(new Color(160, 110, 50));
        g2.fillRect(x + 6, y + 2, ts - 12, ts - 2);
        // Panneau de verre
        g2.setColor(new Color(200, 230, 255, 180));
        g2.fillRect(x + 12, y + 6, ts - 24, 18);
        g2.setColor(new Color(150, 200, 255, 100));
        g2.fillRect(x + 13, y + 7, ts - 26, 16);
        // Poignée
        g2.setColor(new Color(220, 180, 50));
        g2.fillOval(x + ts/2 - 3, y + ts/2, 6, 6);
        g2.setColor(new Color(180, 140, 30));
        g2.drawOval(x + ts/2 - 3, y + ts/2, 6, 6);
        // Contour
        g2.setColor(new Color(80, 50, 10));
        g2.drawRect(x + 4, y, ts - 8, ts);
        // Texte SORTIE
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 7));
        g2.drawString("SORTIE", x + 5, y + ts - 4);
    }

    private void drawPlatform(Graphics2D g2, int x, int y, int ts) {
        // Plateforme salle serveur : rack serveur
        g2.setColor(new Color(30, 35, 60));
        g2.fillRect(x, y, ts, ts);
        g2.setColor(new Color(0, 150, 200));
        g2.fillRect(x + 2, y + 2, ts - 4, 6);
        g2.setColor(new Color(0, 100, 150));
        g2.fillRect(x + 2, y + 10, ts - 4, 6);
        // Leds clignotantes
        g2.setColor((System.currentTimeMillis() / 300 % 2 == 0) ? Color.GREEN : Color.BLUE);
        g2.fillOval(x + 4, y + 4, 3, 3);
        g2.setColor(new Color(0, 80, 120));
        g2.drawRect(x, y, ts, ts);
    }
}
