# ESIR Escape — Document de référence complet

> Java · Shmup 2D vue du dessus · Groupe de 5 · Rendu 29/05/2026 à 10h00

---

## 1. Plan de l'architecture finale

```
ESIREscape/
├── src/
│   ├── main/
│   │   ├── Main.java            (fourni, inchangé)
│   │   ├── GamePanel.java       (à modifier — boucle, listes, états)
│   │   ├── KeyHandler.java      (à modifier — E, ESPACE)
│   │   └── UI.java              (à créer — HUD, écrans)
│   │
│   ├── entity/
│   │   ├── Entity.java          (fourni, inchangé)
│   │   ├── Player.java          (à modifier)
│   │   ├── Projectile.java      (à créer)
│   │   ├── Enemy.java           (à créer, abstraite)
│   │   ├── Bug404.java          (à créer)
│   │   ├── SurpriseExam.java    (à créer)
│   │   ├── MoodleBoss.java      (à créer)
│   │   └── PowerUp.java         (à créer)
│   │
│   ├── tile/
│   │   ├── Tile.java            (fourni, inchangé)
│   │   └── TileManager.java     (à modifier — isSolid, changeLevel)
│   │
│   └── manager/
│       └── CollisionChecker.java (à créer)
│
└── res/
    ├── maps/
    │   ├── niveau1.txt
    │   └── niveau2.txt
    ├── tiles/      (PNG des tuiles)
    ├── entities/   (PNG joueur, ennemis)
    └── projectiles/(PNG des tirs)
```

---

## 2. Vérification de cohérence finale

### ✅ Objectifs obligatoires — tous couverts

| Objectif | Classe(s) responsable(s) | Statut |
|---|---|---|
| Jeu fonctionnel (boucle 60 FPS) | `GamePanel` | Base fournie ✅ |
| Monde avec 2 niveaux (fichiers texte) | `TileManager`, `niveau1.txt`, `niveau2.txt` | À faire ✅ |
| Fenêtre de fin | `UI` (état `VICTORY`) | À faire ✅ |
| Personnage principal déplaçable | `Player` + `KeyHandler` | À modifier ✅ |
| 2 types d'ennemis minimum | `Bug404` + `SurpriseExam` | À faire ✅ |
| Ennemi invisible | `SurpriseExam.isVisible` | À faire ✅ |
| Projectiles + combat + barre de vie | `Projectile` + `CollisionChecker` + `UI` | À faire ✅ |
| Évolution du personnage (power-ups) | `PowerUp` (café, clé USB+) | À faire ✅ |
| Système de score | `Player.score` + `UI` | À faire ✅ |
| Gestion des collisions | `CollisionChecker` | À faire ✅ |

### ✅ Objectifs secondaires couverts

- Menu principal (Play / Quit) → `GamePanel.gameState` + `UI`
- Changement de type de tir (touche E) → `Player.activeWeapon`
- Power-ups = inventaire léger → `PowerUp`

### ⚠️ Points de vigilance

1. **`ConcurrentModificationException`** — ne jamais supprimer d'éléments pendant l'itération. Utiliser `removeIf()` après les boucles + un buffer pour les spawns ennemis.
2. **`AlphaComposite`** dans `SurpriseExam.draw()` — toujours restaurer le composite original après le dessin semi-transparent, sinon tout ce qui est dessiné après sera transparent.
3. **`isSolid()`** dans `TileManager` — méthode critique partagée par `Projectile` et `CollisionChecker` ; à implémenter en priorité (Développeur 3).
4. **Ordre d'initialisation** — `CollisionChecker` doit être instancié après `Player` dans `GamePanel`.
5. **Thread safety** — la boucle de jeu tourne dans un `Thread` séparé. Ne jamais modifier les listes depuis le thread Swing (listeners de KeyHandler). `KeyHandler` ne fait que mettre des booléens à jour ; c'est `update()` dans la boucle qui agit dessus.

---

## 3. Codes complets

---

### 3.1 `GamePanel.java` (package main)

```java
package main;

import entity.*;
import manager.CollisionChecker;
import tile.TileManager;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe centrale du jeu. Gère la boucle 60 FPS, les listes d'entités,
 * les états du jeu et la coordination entre tous les systèmes.
 */
public class GamePanel extends JPanel implements Runnable {

    // --- Dimensions ---
    public static final int TILE_SIZE    = 48;
    public static final int MAX_COL      = 16;
    public static final int MAX_ROW      = 12;
    public final int screenWidth  = TILE_SIZE * MAX_COL; // 768px
    public final int screenHeight = TILE_SIZE * MAX_ROW; // 576px

    // --- États du jeu ---
    public static final int STATE_MENU    = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_GAMEOVER= 2;
    public static final int STATE_VICTORY = 3;
    public int gameState = STATE_MENU;

    // --- Systèmes ---
    public TileManager    tileManager;
    public KeyHandler     keyHandler;
    public CollisionChecker collisionChecker;
    public UI             ui;

    // --- Entités ---
    public Player               player;
    public List<Enemy>          enemies     = new ArrayList<>();
    public List<Projectile>     projectiles = new ArrayList<>();
    public List<PowerUp>        powerUps    = new ArrayList<>();

    // Buffer pour les spawns pendant update() — évite ConcurrentModificationException
    private final List<Projectile> spawnBuffer = new ArrayList<>();

    // --- Score global ---
    public int score = 0;

    // Niveau actuel
    private int currentLevel = 1;

    // FPS
    private final int FPS = 60;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);

        keyHandler       = new KeyHandler();
        tileManager      = new TileManager(this);
        player           = new Player(this, keyHandler);
        collisionChecker = new CollisionChecker(this);
        ui               = new UI(this);

        addKeyListener(keyHandler);
        tileManager.loadMap("/maps/niveau1.txt");
    }

    /** Lance le thread de jeu. */
    public void startGameThread() {
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (true) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    /** Met à jour toutes les entités selon l'état courant. */
    public void update() {
        switch (gameState) {
            case STATE_MENU:
                // Navigation menu gérée dans UI via KeyHandler
                if (keyHandler.enterPressed) {
                    gameState = STATE_PLAYING;
                    keyHandler.enterPressed = false;
                }
                break;

            case STATE_PLAYING:
                // 1. Mise à jour du joueur
                player.update();

                // 2. Mise à jour des ennemis (avec buffer pour leurs tirs)
                for (Enemy e : enemies) {
                    e.update(spawnBuffer);
                }

                // 3. Mise à jour des projectiles
                for (Projectile p : projectiles) {
                    p.update();
                }

                // 4. Mise à jour des power-ups
                for (PowerUp pu : powerUps) {
                    pu.update();
                }

                // 5. Détection des collisions
                collisionChecker.checkAll(spawnBuffer);

                // 6. Nettoyage — toujours APRÈS toutes les boucles
                enemies.removeIf(e -> !e.isAlive());
                projectiles.removeIf(p -> !p.isAlive());
                powerUps.removeIf(pu -> !pu.isAlive());

                // 7. Intégration des nouveaux projectiles ennemis
                projectiles.addAll(spawnBuffer);
                spawnBuffer.clear();

                // 8. Vérification fin de niveau / game over
                checkLevelTransition();
                break;

            case STATE_GAMEOVER:
            case STATE_VICTORY:
                if (keyHandler.enterPressed) {
                    resetGame();
                    keyHandler.enterPressed = false;
                }
                break;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch (gameState) {
            case STATE_MENU:
                ui.drawMenu(g2);
                break;

            case STATE_PLAYING:
                tileManager.draw(g2);
                // Dessine d'abord les power-ups (sous les entités)
                for (PowerUp pu : powerUps) pu.draw(g2);
                // Puis les ennemis
                for (Enemy e : enemies)   e.draw(g2);
                // Puis les projectiles
                for (Projectile p : projectiles) p.draw(g2);
                // Puis le joueur (au premier plan)
                player.draw(g2);
                // Enfin le HUD par-dessus tout
                ui.drawHUD(g2);
                break;

            case STATE_GAMEOVER:
                ui.drawGameOver(g2);
                break;

            case STATE_VICTORY:
                ui.drawVictory(g2);
                break;
        }
        g2.dispose();
    }

    /** Fait apparaître un power-up à la position d'un ennemi mort (50% de chance). */
    public void spawnPowerUp(int x, int y) {
        if (Math.random() < 0.5) {
            int type = Math.random() < 0.5 ? PowerUp.TYPE_COFFEE : PowerUp.TYPE_USB;
            powerUps.add(new PowerUp(this, x, y, type));
        }
    }

    /** Vérifie si le niveau doit changer ou si le jeu est terminé. */
    private void checkLevelTransition() {
        if (currentLevel == 1 && enemies.isEmpty()) {
            loadLevel(2);
        } else if (currentLevel == 2 && enemies.isEmpty()) {
            gameState = STATE_VICTORY;
        }
        if (player.getCurrentHp() <= 0) {
            gameState = STATE_GAMEOVER;
        }
    }

    /** Charge un nouveau niveau. */
    public void loadLevel(int level) {
        currentLevel = level;
        enemies.clear();
        projectiles.clear();
        powerUps.clear();
        spawnBuffer.clear();
        tileManager.loadMap("/maps/niveau" + level + ".txt");
        player.resetPosition();
        // Spawner les ennemis du niveau
        if (level == 1) spawnLevel1Enemies();
        else            spawnLevel2Enemies();
    }

    private void spawnLevel1Enemies() {
        for (int i = 0; i < 5; i++) {
            enemies.add(new Bug404(this, 100 + i * 120, 80, player));
        }
        for (int i = 0; i < 3; i++) {
            enemies.add(new SurpriseExam(this, 200 + i * 150, 150, player));
        }
    }

    private void spawnLevel2Enemies() {
        for (int i = 0; i < 4; i++) {
            enemies.add(new Bug404(this, 80 + i * 160, 60, player));
        }
        for (int i = 0; i < 4; i++) {
            enemies.add(new SurpriseExam(this, 120 + i * 160, 100, player));
        }
        enemies.add(new MoodleBoss(this, screenWidth / 2 - 48, 60, player));
    }

    private void resetGame() {
        score = 0;
        currentLevel = 1;
        player = new Player(this, keyHandler);
        loadLevel(1);
        gameState = STATE_PLAYING;
    }
}
```

---

### 3.2 `KeyHandler.java` (package main)

```java
package main;

import java.awt.event.*;

/**
 * Capture les événements clavier. Ne fait QUE mettre des booléens à jour —
 * toute la logique de jeu reste dans update() de GamePanel/Player.
 */
public class KeyHandler implements KeyListener {

    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean spacePressed;    // tir
    public boolean ePressed;        // changement d'arme
    public boolean enterPressed;    // validation menu

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_UP)    upPressed    = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed  = true;
        if (code == KeyEvent.VK_Q || code == KeyEvent.VK_LEFT)  leftPressed  = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;
        if (code == KeyEvent.VK_SPACE)  spacePressed  = true;
        if (code == KeyEvent.VK_E)      ePressed      = true;
        if (code == KeyEvent.VK_ENTER)  enterPressed  = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_Z || code == KeyEvent.VK_UP)    upPressed    = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed  = false;
        if (code == KeyEvent.VK_Q || code == KeyEvent.VK_LEFT)  leftPressed  = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_SPACE)  spacePressed  = false;
        if (code == KeyEvent.VK_E)      ePressed      = false;
        // enterPressed se remet à false dans GamePanel.update() après consommation
    }

    @Override public void keyTyped(KeyEvent e) {}
}
```

---

### 3.3 `UI.java` (package main)

```java
package main;

import java.awt.*;

/**
 * Gère tout l'affichage qui se superpose au jeu :
 * menu, HUD (vie, score, arme), écran Game Over, écran Victoire.
 */
public class UI {

    private final GamePanel gp;
    private final Font      fontTitle = new Font("Arial", Font.BOLD, 48);
    private final Font      fontHUD   = new Font("Arial", Font.BOLD, 18);
    private final Font      fontSub   = new Font("Arial", Font.PLAIN, 22);

    public UI(GamePanel gp) { this.gp = gp; }

    /** Menu principal. */
    public void drawMenu(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(fontTitle);
        g2.setColor(Color.ORANGE);
        drawCentered(g2, "ESIR ESCAPE", gp.screenHeight / 2 - 60);

        g2.setFont(fontSub);
        g2.setColor(Color.WHITE);
        drawCentered(g2, "Appuie sur ENTREE pour jouer", gp.screenHeight / 2 + 20);
        drawCentered(g2, "Q pour quitter", gp.screenHeight / 2 + 55);
    }

    /** HUD affiché pendant le jeu. */
    public void drawHUD(Graphics2D g2) {
        g2.setFont(fontHUD);

        // --- Barre de vie ---
        int barX = 20, barY = 20, barW = 200, barH = 20;
        float ratio = (float) gp.player.getCurrentHp() / gp.player.getMaxHp();

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(barX, barY, barW, barH);

        Color hpColor = ratio > 0.5f ? Color.GREEN : ratio > 0.25f ? Color.ORANGE : Color.RED;
        g2.setColor(hpColor);
        g2.fillRect(barX, barY, (int)(barW * ratio), barH);

        g2.setColor(Color.WHITE);
        g2.drawRect(barX, barY, barW, barH);
        g2.drawString("Santé mentale", barX, barY - 4);

        // --- Score ---
        g2.setColor(Color.YELLOW);
        g2.drawString("Score : " + gp.score, gp.screenWidth - 180, 36);

        // --- Arme active ---
        String weaponName = gp.player.getActiveWeapon() == 0 ? "Clé USB" : "System.out.println()";
        g2.setColor(Color.CYAN);
        g2.drawString("Arme : " + weaponName, 20, gp.screenHeight - 10);

        // --- Niveau de café ---
        g2.setColor(new Color(200, 120, 0));
        g2.drawString("Café x" + gp.player.getCoffeeCount(), 20, gp.screenHeight - 30);
    }

    /** Écran Game Over. */
    public void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(fontTitle);
        g2.setColor(Color.RED);
        drawCentered(g2, "GAME OVER", gp.screenHeight / 2 - 40);

        g2.setFont(fontSub);
        g2.setColor(Color.WHITE);
        drawCentered(g2, "Score final : " + gp.score, gp.screenHeight / 2 + 20);
        drawCentered(g2, "ENTREE pour recommencer", gp.screenHeight / 2 + 60);
    }

    /** Écran de victoire. */
    public void drawVictory(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(fontTitle);
        g2.setColor(Color.GREEN);
        drawCentered(g2, "PROJET VALIDE !", gp.screenHeight / 2 - 40);

        g2.setFont(fontSub);
        g2.setColor(Color.WHITE);
        drawCentered(g2, "Score final : " + gp.score, gp.screenHeight / 2 + 20);
        drawCentered(g2, "ENTREE pour rejouer", gp.screenHeight / 2 + 60);
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (gp.screenWidth - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}
```

---

### 3.4 `Player.java` (package entity)

```java
package entity;

import main.GamePanel;
import main.KeyHandler;
import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;

/**
 * Le personnage principal. Gère déplacements, tir, armes et application des power-ups.
 */
public class Player extends Entity {

    private final KeyHandler keyHandler;

    // Stats
    private int maxHp    = 100;
    private int currentHp = 100;
    private int speed    = 4;
    private int score    = 0;

    // Armes : 0 = Clé USB, 1 = System.out.println()
    private int  activeWeapon = 0;
    private boolean tripleShot  = false;  // power-up clé USB améliorée
    private boolean doubleDmg   = false;  // power-up println amélioré
    private int  coffeeCount = 0;         // nombre de cafés bus (max 2)

    // Cooldown de tir (en frames)
    private int shootCooldown = 0;
    private static final int SHOOT_DELAY = 15; // 0.25 secondes à 60 FPS

    // Cooldown d'invincibilité après dégâts
    private int invincibleTimer = 0;

    public Player(GamePanel gp, KeyHandler kh) {
        this.gp         = gp;
        this.keyHandler = kh;
        this.x          = gp.screenWidth  / 2 - 24;
        this.y          = gp.screenHeight - 100;
        loadImage();
    }

    private void loadImage() {
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/entities/player.png"));
        } catch (Exception e) { image = null; }
    }

    @Override
    public void update() {
        // Déplacements ZQSD / flèches
        if (keyHandler.upPressed    && y > 0)                    y -= speed;
        if (keyHandler.downPressed  && y < gp.screenHeight - 48) y += speed;
        if (keyHandler.leftPressed  && x > 0)                    x -= speed;
        if (keyHandler.rightPressed && x < gp.screenWidth  - 48) x += speed;

        // Changement d'arme (touche E — front sur appui)
        if (keyHandler.ePressed) {
            activeWeapon = (activeWeapon == 0) ? 1 : 0;
            keyHandler.ePressed = false; // consommer l'événement
        }

        // Tir (ESPACE)
        if (shootCooldown > 0) shootCooldown--;
        if (keyHandler.spacePressed && shootCooldown == 0) {
            shoot();
            shootCooldown = SHOOT_DELAY;
        }

        // Décrémenter invincibilité
        if (invincibleTimer > 0) invincibleTimer--;
    }

    /** Crée le ou les projectiles et les ajoute à la liste de GamePanel. */
    private void shoot() {
        int damage = (activeWeapon == 1 && doubleDmg) ? 50 : (activeWeapon == 1 ? 25 : 10);
        int type   = activeWeapon; // 0 = CLE_USB, 1 = PRINTLN

        if (activeWeapon == 0 && tripleShot) {
            // Éventail 3 projectiles
            gp.projectiles.add(new Projectile(gp, x + 20, y, -1, -1, type, true, damage));
            gp.projectiles.add(new Projectile(gp, x + 20, y,  0, -1, type, true, damage));
            gp.projectiles.add(new Projectile(gp, x + 20, y,  1, -1, type, true, damage));
        } else {
            gp.projectiles.add(new Projectile(gp, x + 20, y, 0, -1, type, true, damage));
        }
    }

    /** Reçoit des dégâts. Invincible pendant 60 frames après un coup. */
    public void takeDamage(int dmg) {
        if (invincibleTimer > 0) return;
        currentHp -= dmg;
        if (currentHp < 0) currentHp = 0;
        invincibleTimer = 60;
    }

    /** Applique l'effet d'un power-up. */
    public void applyPowerUp(int type) {
        if (type == PowerUp.TYPE_COFFEE) {
            if (coffeeCount < 2) {
                coffeeCount++;
                speed += 2; // +2 à chaque tasse, max +4
            }
        } else if (type == PowerUp.TYPE_USB) {
            if (activeWeapon == 0) tripleShot = true;
            else                   doubleDmg  = true;
        }
    }

    /** Remet le joueur à sa position initiale (changement de niveau). */
    public void resetPosition() {
        x = gp.screenWidth / 2 - 24;
        y = gp.screenHeight - 100;
        invincibleTimer = 0;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Clignotement si invincible
        if (invincibleTimer > 0 && (invincibleTimer / 5) % 2 == 0) return;

        if (image != null) {
            g2.drawImage(image, x, y, 48, 48, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(x, y, 48, 48);
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, 48, 48); }

    // --- Getters ---
    public int     getCurrentHp()   { return currentHp; }
    public int     getMaxHp()       { return maxHp; }
    public int     getScore()       { return score; }
    public int     getActiveWeapon(){ return activeWeapon; }
    public int     getCoffeeCount() { return coffeeCount; }
    public void    addScore(int s)  { this.score += s; gp.score += s; }
}
```

---

### 3.5 `Projectile.java` (package entity)

```java
package entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Projectile lancé par le joueur ou les ennemis.
 * fromPlayer=true → tir joueur (ne blesse que les ennemis).
 * fromPlayer=false → tir ennemi (ne blesse que le joueur).
 */
public class Projectile extends Entity {

    public static final int CLE_USB     = 0;
    public static final int PRINTLN     = 1;
    public static final int ENNEMI_SHOT = 2;

    private int     dx, dy;       // direction (-1, 0, ou 1)
    private int     damage;
    private boolean alive      = true;
    private boolean fromPlayer;
    private int     range;
    private int     distanceTraveled = 0;
    private static final int SIZE = 8;

    public Projectile(GamePanel gp, int x, int y,
                      int dx, int dy, int type,
                      boolean fromPlayer, int damage) {
        this.gp         = gp;
        this.x          = x;
        this.y          = y;
        this.dx         = dx;
        this.dy         = dy;
        this.fromPlayer = fromPlayer;
        this.damage     = damage;

        switch (type) {
            case CLE_USB:     speed = 7; range = 250; break;
            case PRINTLN:     speed = 3; range = 700; break;
            case ENNEMI_SHOT: speed = 4; range = 450; break;
            default:          speed = 5; range = 400;
        }
        loadImage(type);
    }

    private void loadImage(int type) {
        String path = type == CLE_USB ? "/projectiles/cle_usb.png"
                    : type == PRINTLN  ? "/projectiles/println.png"
                    :                    "/projectiles/bug_shot.png";
        try {
            image = ImageIO.read(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            // Fallback coloré
            BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(fromPlayer ? Color.CYAN : Color.RED);
            g.fillOval(0, 0, SIZE, SIZE);
            g.dispose();
            image = img;
        }
    }

    @Override
    public void update() {
        x += dx * speed;
        y += dy * speed;
        distanceTraveled += speed;

        // Mort hors portée ou hors écran
        if (distanceTraveled > range
                || x < -SIZE || x > gp.screenWidth  + SIZE
                || y < -SIZE || y > gp.screenHeight + SIZE) {
            alive = false;
        }

        // Mort si collision avec un mur solide
        if (gp.tileManager.isSolid(x + SIZE / 2, y + SIZE / 2)) {
            alive = false;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (alive && image != null) {
            g2.drawImage(image, x, y, SIZE, SIZE, null);
        }
    }

    public Rectangle getBounds()      { return new Rectangle(x, y, SIZE, SIZE); }
    public boolean   isAlive()        { return alive; }
    public void      setAlive(boolean b) { alive = b; }
    public int       getDamage()      { return damage; }
    public boolean   isFromPlayer()   { return fromPlayer; }
}
```

---

### 3.6 `Enemy.java` (package entity — abstraite)

```java
package entity;

import main.GamePanel;
import java.awt.*;
import java.util.List;

/**
 * Classe abstraite pour tous les ennemis.
 * Sous-classes : Bug404, SurpriseExam, MoodleBoss.
 */
public abstract class Enemy extends Entity {

    protected int     maxHp, currentHp;
    protected int     damage;
    protected int     scoreValue;
    protected boolean alive        = true;
    protected int     shootCooldown;
    protected int     shootTimer   = 0;
    protected Player  player;

    protected static final int SIZE = 48;

    public Enemy(GamePanel gp, int x, int y, Player player) {
        this.gp     = gp;
        this.x      = x;
        this.y      = y;
        this.player = player;
        initStats();
    }

    /** Chaque sous-classe définit ses propres stats. */
    protected abstract void initStats();

    /** Logique de déplacement propre à chaque ennemi. */
    protected abstract void move();

    /** Logique de tir — ajoute dans le buffer, jamais dans la liste principale. */
    protected abstract void shoot(List<Projectile> buffer);

    /** update() avec buffer pour éviter ConcurrentModificationException. */
    public void update(List<Projectile> buffer) {
        if (!alive) return;
        move();
        shootTimer++;
        if (shootTimer >= shootCooldown) {
            shootTimer = 0;
            shoot(buffer);
        }
    }

    /** Reçoit des dégâts ; passe alive=false si PV <= 0. */
    public void takeDamage(int dmg) {
        currentHp -= dmg;
        if (currentHp <= 0) {
            currentHp = 0;
            alive = false;
        }
    }

    /** Dessine une barre de vie au-dessus de l'ennemi. */
    protected void drawHealthBar(Graphics2D g2) {
        float ratio = (float) currentHp / maxHp;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y - 8, SIZE, 5);
        g2.setColor(ratio > 0.5f ? Color.GREEN : ratio > 0.25f ? Color.ORANGE : Color.RED);
        g2.fillRect(x, y - 8, (int)(SIZE * ratio), 5);
    }

    /** Retourne la direction normalisée (signum) vers le joueur. */
    protected int[] dirToPlayer() {
        int ddx = player.x - x;
        int ddy = player.y - y;
        return new int[]{ (int)Math.signum(ddx), (int)Math.signum(ddy) };
    }

    public Rectangle getBounds()    { return new Rectangle(x, y, SIZE, SIZE); }
    public boolean   isAlive()      { return alive; }
    public int       getScoreValue(){ return scoreValue; }
}
```

---

### 3.7 `Bug404.java` (package entity)

```java
package entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;

/**
 * Ennemi de type 1. Toujours visible.
 * Se déplace vers le joueur et tire toutes les 2 secondes.
 */
public class Bug404 extends Enemy {

    public Bug404(GamePanel gp, int x, int y, Player player) {
        super(gp, x, y, player);
        loadSprite();
    }

    @Override
    protected void initStats() {
        maxHp        = 30;
        currentHp    = 30;
        speed        = 2;
        damage       = 10;
        scoreValue   = 100;
        shootCooldown = 120; // 2 s à 60 FPS
    }

    private void loadSprite() {
        try { image = ImageIO.read(getClass().getResourceAsStream("/entities/bug404.png")); }
        catch (Exception e) { image = null; }
    }

    @Override
    protected void move() {
        int[] dir = dirToPlayer();
        x += dir[0] * speed;
        y += dir[1] * speed;
    }

    @Override
    protected void shoot(List<Projectile> buffer) {
        int[] dir = dirToPlayer();
        buffer.add(new Projectile(gp, x + SIZE/2, y + SIZE/2,
                dir[0], dir[1], Projectile.ENNEMI_SHOT, false, 8));
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;
        if (image != null) g2.drawImage(image, x, y, SIZE, SIZE, null);
        else { g2.setColor(new Color(220, 50, 50)); g2.fillOval(x, y, SIZE, SIZE); }
        drawHealthBar(g2);
    }
}
```

---

### 3.8 `SurpriseExam.java` (package entity)

```java
package entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;

/**
 * Ennemi de type 2 — invisible par défaut.
 * Machine à états : INVISIBLE (3s) → FLASH (1s) → ATTACK (1.5s) → retour INVISIBLE.
 */
public class SurpriseExam extends Enemy {

    private enum State { INVISIBLE, FLASH, ATTACK }
    private State state      = State.INVISIBLE;
    private int   stateTimer = 0;

    private static final int INVISIBLE_DUR = 180;
    private static final int FLASH_DUR     = 60;
    private static final int ATTACK_DUR    = 90;

    public SurpriseExam(GamePanel gp, int x, int y, Player player) {
        super(gp, x, y, player);
        loadSprite();
    }

    @Override
    protected void initStats() {
        maxHp        = 20;
        currentHp    = 20;
        speed        = 3;
        damage       = 15;
        scoreValue   = 200;
        shootCooldown = 1; // géré manuellement
    }

    private void loadSprite() {
        try { image = ImageIO.read(getClass().getResourceAsStream("/entities/surprise_exam.png")); }
        catch (Exception e) { image = null; }
    }

    @Override
    protected void move() {
        if (state == State.ATTACK) {
            int[] dir = dirToPlayer();
            x += dir[0] * speed;
            y += dir[1] * speed;
        }
    }

    @Override
    public void update(List<Projectile> buffer) {
        if (!alive) return;
        stateTimer++;
        switch (state) {
            case INVISIBLE:
                if (stateTimer >= INVISIBLE_DUR) { state = State.FLASH; stateTimer = 0; }
                break;
            case FLASH:
                if (stateTimer >= FLASH_DUR) {
                    state = State.ATTACK; stateTimer = 0;
                    shoot(buffer); // tir en entrant en phase ATTACK
                }
                break;
            case ATTACK:
                move();
                if (stateTimer >= ATTACK_DUR) {
                    state = State.INVISIBLE; stateTimer = 0;
                    repositionNearPlayer();
                }
                break;
        }
    }

    private void repositionNearPlayer() {
        int offset = 150 + (int)(Math.random() * 100);
        double angle = Math.random() * 2 * Math.PI;
        x = (int)(player.x + Math.cos(angle) * offset);
        y = (int)(player.y + Math.sin(angle) * offset);
        x = Math.max(0, Math.min(x, gp.screenWidth  - SIZE));
        y = Math.max(0, Math.min(y, gp.screenHeight - SIZE));
    }

    @Override
    protected void shoot(List<Projectile> buffer) {
        int[] dir = dirToPlayer();
        buffer.add(new Projectile(gp, x + SIZE/2, y + SIZE/2,
                dir[0], dir[1], Projectile.ENNEMI_SHOT, false, 15));
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;
        switch (state) {
            case INVISIBLE: break; // rien à dessiner
            case FLASH:
                if (stateTimer % 10 < 5) drawSprite(g2, 0.5f);
                break;
            case ATTACK:
                drawSprite(g2, 1.0f);
                drawHealthBar(g2);
                break;
        }
    }

    private void drawSprite(Graphics2D g2, float alpha) {
        Composite orig = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (image != null) g2.drawImage(image, x, y, SIZE, SIZE, null);
        else { g2.setColor(new Color(150, 0, 220)); g2.fillRect(x, y, SIZE, SIZE); }
        g2.setComposite(orig); // TOUJOURS restaurer
    }
}
```

---

### 3.9 `MoodleBoss.java` (package entity)

```java
package entity;

import main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.util.List;

/**
 * Boss final. Deux phases selon les PV.
 * Phase 1 (>50% PV) : tir simple vers le joueur.
 * Phase 2 (<=50% PV) : tir en croix (4 directions).
 */
public class MoodleBoss extends Enemy {

    private static final int BOSS_SIZE = 96;

    public MoodleBoss(GamePanel gp, int x, int y, Player player) {
        super(gp, x, y, player);
        loadSprite();
    }

    @Override
    protected void initStats() {
        maxHp        = 300;
        currentHp    = 300;
        speed        = 1;
        damage       = 20;
        scoreValue   = 1000;
        shootCooldown = 90; // 1.5 s
    }

    private void loadSprite() {
        try { image = ImageIO.read(getClass().getResourceAsStream("/entities/moodle_boss.png")); }
        catch (Exception e) { image = null; }
    }

    @Override
    protected void move() {
        // Le boss se déplace latéralement
        x += speed;
        if (x >= gp.screenWidth - BOSS_SIZE || x <= 0) speed = -speed;
    }

    @Override
    protected void shoot(List<Projectile> buffer) {
        boolean phase2 = currentHp <= maxHp / 2;
        int[] dir = dirToPlayer();
        // Tir principal vers le joueur
        buffer.add(new Projectile(gp, x + BOSS_SIZE/2, y + BOSS_SIZE,
                dir[0], dir[1], Projectile.ENNEMI_SHOT, false, 20));

        if (phase2) {
            // Tir en croix supplémentaire
            int[][] extraDirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : extraDirs) {
                buffer.add(new Projectile(gp, x + BOSS_SIZE/2, y + BOSS_SIZE/2,
                        d[0], d[1], Projectile.ENNEMI_SHOT, false, 15));
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;
        if (image != null) g2.drawImage(image, x, y, BOSS_SIZE, BOSS_SIZE, null);
        else { g2.setColor(new Color(100, 0, 180)); g2.fillRect(x, y, BOSS_SIZE, BOSS_SIZE); }

        // Barre de vie du boss — grande barre en haut de l'écran
        float ratio = (float) currentHp / maxHp;
        int bx = gp.screenWidth / 4, bw = gp.screenWidth / 2;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(bx, 8, bw, 12);
        g2.setColor(ratio > 0.5f ? Color.GREEN : Color.RED);
        g2.fillRect(bx, 8, (int)(bw * ratio), 12);
        g2.setColor(Color.WHITE);
        g2.drawRect(bx, 8, bw, 12);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("Serveur Moodle", bx, 7);
    }

    @Override
    public Rectangle getBounds() { return new Rectangle(x, y, BOSS_SIZE, BOSS_SIZE); }
}
```

---

### 3.10 `PowerUp.java` (package entity)

```java
package entity;

import main.GamePanel;
import java.awt.*;

/**
 * Objet ramassable qui tombe quand un ennemi meurt.
 * TYPE_COFFEE : augmente la vitesse (cumulable 2×).
 * TYPE_USB    : améliore l'arme active du joueur.
 */
public class PowerUp extends Entity {

    public static final int TYPE_COFFEE = 0;
    public static final int TYPE_USB    = 1;

    private final int type;
    private boolean   alive = true;
    private static final int SIZE = 24;

    public PowerUp(GamePanel gp, int x, int y, int type) {
        this.gp   = gp;
        this.x    = x;
        this.y    = y;
        this.type = type;
        this.speed = 1;
    }

    @Override
    public void update() {
        // Tombe doucement vers le bas
        y += speed;
        // Disparaît s'il sort de l'écran
        if (y > gp.screenHeight) alive = false;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (!alive) return;
        if (type == TYPE_COFFEE) {
            g2.setColor(new Color(180, 100, 20));
            g2.fillOval(x, y, SIZE, SIZE);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("C", x + 7, y + 16);
        } else {
            g2.setColor(Color.CYAN);
            g2.fillRect(x, y, SIZE, SIZE);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("USB", x + 2, y + 15);
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, SIZE, SIZE); }
    public boolean   isAlive()   { return alive; }
    public void      setAlive(boolean b) { alive = b; }
    public int       getType()   { return type; }
}
```

---

### 3.11 `TileManager.java` — méthode à ajouter (package tile)

```java
// Ajouter dans la classe TileManager existante :

/**
 * Retourne true si la tuile à la position pixel (px, py) est solide (collision = true).
 * Utilisée par Projectile.update() et CollisionChecker.
 */
public boolean isSolid(int px, int py) {
    int col = px / GamePanel.TILE_SIZE;
    int row = py / GamePanel.TILE_SIZE;
    // Garder dans les bornes
    if (col < 0 || col >= GamePanel.MAX_COL || row < 0 || row >= GamePanel.MAX_ROW) {
        return true; // bords de l'écran = solides
    }
    int tileIndex = mapTileNum[col][row]; // tableau chargé par loadMap()
    return tile[tileIndex].collision;
}

/**
 * Change de niveau : charge une nouvelle carte depuis un fichier texte.
 */
public void changeLevel(String mapPath) {
    loadMap(mapPath);
}
```

---

### 3.12 `CollisionChecker.java` (package manager)

```java
package manager;

import entity.*;
import main.GamePanel;
import java.util.List;

/**
 * Centralise toute la logique de collision.
 * Appelé depuis GamePanel.update() à chaque frame.
 */
public class CollisionChecker {

    private final GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Vérifie toutes les collisions dans le bon ordre.
     * @param spawnBuffer buffer où ajouter les éventuels drop de power-ups
     */
    public void checkAll(List<Projectile> spawnBuffer) {
        checkProjectilesVsEnemies();
        checkProjectilesVsPlayer();
        checkEnemiesVsPlayer();
        checkPlayerVsPowerUps();
    }

    /** Projectiles du joueur contre les ennemis. */
    private void checkProjectilesVsEnemies() {
        for (Projectile p : gp.projectiles) {
            if (!p.isAlive() || !p.isFromPlayer()) continue;
            for (Enemy e : gp.enemies) {
                if (!e.isAlive()) continue;
                if (p.getBounds().intersects(e.getBounds())) {
                    e.takeDamage(p.getDamage());
                    p.setAlive(false);
                    if (!e.isAlive()) {
                        gp.score += e.getScoreValue();
                        gp.spawnPowerUp(e.x, e.y);
                    }
                    break; // un projectile ne touche qu'un ennemi
                }
            }
        }
    }

    /** Projectiles ennemis contre le joueur. */
    private void checkProjectilesVsPlayer() {
        for (Projectile p : gp.projectiles) {
            if (!p.isAlive() || p.isFromPlayer()) continue;
            if (p.getBounds().intersects(gp.player.getBounds())) {
                gp.player.takeDamage(p.getDamage());
                p.setAlive(false);
            }
        }
    }

    /** Contact direct ennemi → joueur. */
    private void checkEnemiesVsPlayer() {
        for (Enemy e : gp.enemies) {
            if (!e.isAlive()) continue;
            if (e.getBounds().intersects(gp.player.getBounds())) {
                gp.player.takeDamage(10); // dégâts de contact
            }
        }
    }

    /** Joueur ramasse un power-up. */
    private void checkPlayerVsPowerUps() {
        for (PowerUp pu : gp.powerUps) {
            if (!pu.isAlive()) continue;
            if (pu.getBounds().intersects(gp.player.getBounds())) {
                gp.player.applyPowerUp(pu.getType());
                pu.setAlive(false);
            }
        }
    }
}
```

---

## 4. Répartition finale des tâches (5 développeurs)

### Développeur 1 — Architecte système
**Fichiers :** `GamePanel.java`, `UI.java`

Tâches :
- Mettre en place le système `gameState` (MENU, PLAYING, GAMEOVER, VICTORY)
- Gérer les listes `enemies`, `projectiles`, `powerUps` et le `spawnBuffer`
- Coder `checkLevelTransition()`, `loadLevel()`, `spawnLevel1/2Enemies()`
- Créer `UI.java` en entier (menu, HUD, écrans de fin)
- Préparer les 2 slides + scénario de démo pour la soutenance

### Développeur 2 — Héros et tirs
**Fichiers :** `Player.java`, `Projectile.java`, `KeyHandler.java`

Tâches :
- Compléter `KeyHandler` : booléens `spacePressed`, `ePressed`, `enterPressed`
- Coder `Player` : déplacements ZQSD, tir Espace, changement d'arme E
- Implémenter les deux types de tir (clé USB rapide / println lent)
- Gérer le tir en éventail (tripleShot) et le clignotement d'invincibilité
- Coder `Projectile` en entier

### Développeur 3 — Carte et niveaux
**Fichiers :** `TileManager.java`, `niveau1.txt`, `niveau2.txt`, `README.md`

Tâches :
- Ajouter `isSolid(int px, int py)` dans `TileManager`
- Ajouter `changeLevel(String path)`
- Créer `niveau1.txt` (couloirs ouverts, peu d'obstacles)
- Créer `niveau2.txt` (salle serveurs, plus dense, murs supplémentaires)
- S'assurer que les tuiles mur/serveur ont `collision = true`
- Rédiger le `README` (dépendances Java, compilation Eclipse, lancement)

### Développeur 4 — Ennemis et boss
**Fichiers :** `Enemy.java`, `Bug404.java`, `SurpriseExam.java`, `MoodleBoss.java`

Tâches :
- Créer la classe abstraite `Enemy` avec `takeDamage`, `drawHealthBar`, `dirToPlayer`
- Coder `Bug404` : déplacement vers joueur, tir périodique
- Coder `SurpriseExam` : machine à états INVISIBLE → FLASH → ATTACK, repositionnement
- Coder `MoodleBoss` : déplacement latéral, 2 phases de tir, grande barre de vie

### Développeur 5 — Collisions et power-ups
**Fichiers :** `CollisionChecker.java`, `PowerUp.java`

Tâches :
- Coder `CollisionChecker` : 4 méthodes (projectile/ennemi, projectile/joueur, contact, power-up)
- Coder `PowerUp` : deux types, chute, dessin de fallback
- Câbler `CollisionChecker` dans `GamePanel` (instanciation + appel dans `update()`)
- Tester et valider tous les cas de collision

---

## 5. Ordre de développement recommandé (1,5 jour)

**Matin J1 (3h) — fondations :**
1. Dev 3 crée les deux cartes `.txt` et valide l'affichage avec la base fournie
2. Dev 1 pose la structure `GamePanel` (états, listes, boucle) sans ennemis
3. Dev 2 fait marcher le déplacement du joueur

**Après-midi J1 (4h) — briques principales :**
4. Dev 2 implémente `Projectile` + tir basique (1 type suffit pour commencer)
5. Dev 4 code `Enemy` + `Bug404` (ennemi simple visible)
6. Dev 5 code `CollisionChecker` (projectile/ennemi en premier)
7. Dev 1 intègre tout dans `GamePanel` → premier build jouable

**Matin J2 (3h) — finitions :**
8. Dev 4 code `SurpriseExam` + `MoodleBoss`
9. Dev 2 ajoute changement d'arme E + tir en éventail
10. Dev 5 ajoute `PowerUp`
11. Dev 1 finalise `UI` + transitions de niveau + écrans de fin
12. **Dev 3 rédige le README — dernier moment**

**1h avant rendu — intégration finale :**
- Merge sur GitLab, build complet, test rapide, zip + dépôt Moodle
