# ESIR Escape

Jeu de type **shoot them up (shmup)** en 2D vue du dessus, realise en Java dans le cadre du projet de programmation ESIR1 2025-2026.

Le joueur incarne un etudiant de l ESIR qui doit traverser les couloirs de l ISTIC pour rendre son projet de programmation avant 10h00. Il affronte des bugs informatiques, des examens surprises invisibles, et finit par combattre le boss final : le serveur Moodle.

---

## Membres du groupe

| Nom | Prenom | Role |
|-----|--------|------|
| ... | ...    | Chef de projet / Menu et Etats |
| ... | ...    | Systeme de tir et Projectiles |
| ... | ...    | Ennemis de base |
| ... | ...    | Boss et Collisions |
| ... | ...    | Power-ups, Score et Map design |

---

## Fonctionnalites implementees

### Objectifs obligatoires
- [x] Jeu completement fonctionnel avec boucle a 60 FPS
- [x] Carte chargee depuis un fichier texte (tilemap)
- [x] 2 niveaux avec transition
- [x] Personnage principal controlable (ZQSD / fleches)
- [x] 2 types d ennemis (Bug Informatique + Examen Surprise invisible)
- [x] Tir de projectiles et gestion des combats
- [x] Barre de vie du joueur
- [x] Evolution du personnage via power-ups
- [x] Systeme de score
- [x] Gestion des collisions (murs, ennemis, projectiles)
- [x] Ecran de fin defini ("Projet Valide !")

### Objectifs secondaires
- [x] Menu principal (Play / Quit)
- [x] Changement de type de tir (Cle USB / System.out.println())
- [x] Systeme de power-ups (inventaire leger)

---

## Prerequis

- **Java** : version 11 ou superieure
- **IntelliJ IDEA** (recommande) ou Eclipse
- Pas de bibliotheque externe : le jeu utilise uniquement **Java Swing** (inclus dans le JDK)

Pour verifier ta version Java :
```bash
java -version
```

---

## Installation et lancement

### Cloner le repo Git

```bash
git clone https://github.com/tonpseudo/ESIREscape.git
cd ESIREscape
```

### Lancer avec IntelliJ IDEA

1. **File -> Open** -> selectionner le dossier `ESIREscape`
2. Clic droit sur `res` -> **Mark Directory as -> Resources Root**
3. Clic droit sur `src` -> **Mark Directory as -> Sources Root**
4. Ouvrir `src/main/Main.java`
5. Cliquer sur le triangle vert a cote de `public static void main`

### Lancer avec Eclipse

1. **File -> Import -> Existing Projects into Workspace**
2. Selectionner le dossier `ESIREscape`
3. Clic droit sur le projet -> **Build Path -> Configure Build Path**
4. Onglet **Source** -> **Add Folder** -> selectionner `res`
5. Clic droit sur `Main.java` -> **Run As -> Java Application**

### Lancer en ligne de commande

```bash
# Linux / Mac
javac -d out -sourcepath src src/main/Main.java
java -cp "out:res" main.Main

# Windows
javac -d out -sourcepath src src/main/Main.java
java -cp "out;res" main.Main
```

---

## Structure du projet

```
ESIREscape/
├── src/
│   ├── main/
│   │   ├── Main.java              Point d entree du programme
│   │   ├── GamePanel.java         Boucle de jeu 60 FPS, gestion des etats
│   │   └── KeyHandler.java        Gestion des touches clavier
│   ├── entity/
│   │   ├── Entity.java            Classe abstraite de base
│   │   ├── Player.java            Le joueur (deplacement, tir, armes)
│   │   ├── Enemy.java             Classe abstraite ennemi
│   │   ├── BugEnemy.java          Bug Informatique (visible, fonce vers le joueur)
│   │   ├── ExamenEnemy.java       Examen Surprise (invisible par intermittence)
│   │   └── Boss.java              Serveur Moodle (boss final, 2 phases)
│   ├── projectile/
│   │   ├── Projectile.java        Classe abstraite projectile
│   │   ├── UsbProjectile.java     Cle USB : rapide, faible degat
│   │   └── PrintProjectile.java   System.out.println() : lent, gros degat
│   ├── powerup/
│   │   ├── PowerUp.java           Classe abstraite power-up
│   │   ├── CoffeeUp.java          Tasse de cafe : augmente la vitesse
│   │   └── UsbUp.java             Cle USB amelioree : ameliore l arme active
│   └── tile/
│       ├── Tile.java              Une tuile (image + collision)
│       └── TileManager.java       Chargement et affichage de la carte
├── res/
│   ├── maps/
│   │   ├── map1.txt               Niveau 1 : couloirs ouverts
│   │   └── map2.txt               Niveau 2 : couloirs denses + zone boss
│   ├── tiles/
│   │   ├── GRASS.png              Sol du couloir
│   │   ├── BRICK.png              Mur (collision)
│   │   ├── BRICK2.png             Serveur (collision)
│   │   ├── SAND.png               Bureau (decoration)
│   │   └── WATER.png              Eau (decoration)
│   └── player/
│       └── superhero.png          Sprite du joueur
├── .gitignore
└── README.md
```

---

## Controles

| Touche | Action |
|--------|--------|
| `Z` ou fleche haut    | Se deplacer vers le haut |
| `S` ou fleche bas     | Se deplacer vers le bas |
| `Q` ou fleche gauche  | Se deplacer vers la gauche |
| `D` ou fleche droite  | Se deplacer vers la droite |
| `Espace`              | Tirer |
| `E`                   | Changer d arme |
| `Entree`              | Valider (menu, rejouer) |
| `Echap`               | Quitter |

---

## Mecaniques de jeu

### Armes

| Arme | Vitesse | Degats | Particularite |
|------|---------|--------|---------------|
| Cle USB | Rapide | 10 | 3 projectiles en eventail si amelioree |
| System.out.println() | Lent | 20 | Degats doubles si amelioree |

### Ennemis

| Ennemi | Comportement | Visibilite |
|--------|-------------|-----------|
| Bug Informatique | Fonce vers le joueur, tire toutes les 2 sec | Toujours visible |
| Examen Surprise | Attaque surprise quand visible | Invisible 2,5 sec / visible 1 sec |
| Serveur Moodle (boss) | Phase 1 : tir vers le joueur / Phase 2 : tir en croix | Toujours visible |

### Power-ups

| Power-up | Effet |
|----------|-------|
| Tasse de cafe | 1ere tasse : +2 vitesse / 2eme tasse : +2 vitesse (sprint) |
| Cle USB amelioree | Ameliore l arme actuellement active |

### Score

| Evenement | Points |
|-----------|--------|
| Tuer un Bug Informatique | +100 |
| Tuer un Examen Surprise | +200 |
| Vaincre le boss Moodle | +500 |

---

## Dependances

Aucune dependance externe. Le projet utilise uniquement :

- **Java SE 11+**
- **javax.swing** : fenetre et rendu graphique (inclus dans le JDK)
- **java.awt** : graphismes 2D (inclus dans le JDK)
- **javax.imageio** : chargement des images PNG (inclus dans le JDK)

---

## Contexte academique

Projet realise dans le cadre du module **PROJ-PROG 2025-2026** a l **ESIR1 / ISTIC - Universite de Rennes**.
Date de rendu : **29 mai 2026 a 10h00**.
Soutenance : **29 mai 2026 de 10h15 a 12h15**.
