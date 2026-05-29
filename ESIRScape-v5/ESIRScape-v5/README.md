# ESIR Escape

Jeu de type **shoot them up (shmup)** en 2D vue du dessus, réalisé en Java dans le cadre du projet de programmation ESIR1 2025-2026.

Le joueur incarne un étudiant (ou un ninja) de l'ESIR qui doit rendre son projet de programmation avant la deadline. Il affronte des bugs informatiques, des examens surprises, et finit par combattre le boss final : le Prof Duroc qui bloque l'accès au serveur Moodle.

---

## Membres du groupe

| Nom | Prénom | Rôle |
|-----|--------|------|
| Mboumba | Mack   | Chef de projet / Menu et États |
| Diaby | .Mamoudou   | Système de tir et Projectiles |
| Ba | Ibrahima    | Ennemis de base |
| Kerim |Mohamed    | Boss et Map design|
| GUEYE | Ndiaga   | Power-ups, Score  |

---

## Fonctionnalités implémentées

### Objectifs obligatoires
- [x] Jeu complètement fonctionnel avec boucle à 60 FPS
- [x] Carte chargée depuis un fichier texte (tilemap)
- [x] 3 niveaux avec transitions cinématiques (Salle de classe → Labo 404 → Salle des Serveurs)
- [x] Personnage principal contrôlable (ZQSD / flèches)
- [x] 2 types d'ennemis de base (Bug Informatique + Examen Surprise)
- [x] Tir de projectiles et gestion des combats
- [x] Barre de vie du joueur
- [x] Évolution du personnage via power-ups
- [x] Système de score
- [x] Gestion des collisions (murs, ennemis, projectiles)
- [x] Écran de fin défini ("Projet Validé !")

### Objectifs secondaires
- [x] Écran de sélection de personnage (Étudiant / Ninja)
- [x] Changement de type de tir (Clé USB / System.out.println())
- [x] Système de power-ups persistants entre les niveaux
- [x] Boss final avec 2 phases et système de rage
- [x] Moteur audio synthétique (sons et musique générés par code, sans fichier audio)
- [x] Effets de particules visuels (impacts, morts, explosions)
- [x] Tir intelligent (ciblage automatique de l'ennemi le plus proche)

---

## Prérequis

- **Java** : version 11 ou supérieure
- **IntelliJ IDEA** (recommandé) ou Eclipse
- Pas de bibliothèque externe : le jeu utilise uniquement **Java Swing** et **Java Sound** (inclus dans le JDK)

Pour vérifier ta version Java :
```bash
java -version
```

---

## Installation et lancement



### Lancer avec IntelliJ IDEA

1. **File → Open** → sélectionner le dossier `ESIREscape`
2. Clic droit sur `res` → **Mark Directory as → Resources Root**
3. Clic droit sur `src` → **Mark Directory as → Sources Root**
4. Ouvrir `src/main/Main.java`
5. Cliquer sur le triangle vert à côté de `public static void main`

### Lancer avec Eclipse

1. **File → Import → Existing Projects into Workspace**
2. Sélectionner le dossier `ESIREscape`
3. Clic droit sur le projet → **Build Path → Configure Build Path**
4. Onglet **Source** → **Add Folder** → sélectionner `res`
5. Clic droit sur `Main.java` → **Run As → Java Application**

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
│   │   ├── Main.java              Point d'entrée : création de la JFrame et démarrage du thread
│   │   ├── GamePanel.java         Boucle de jeu 60 FPS, machine à états, rendu global
│   │   ├── KeyHandler.java        Gestion des touches clavier (ZQSD + flèches)
│   │   └── SoundManager.java      Moteur audio : sons et musique générés par synthèse PCM
│   ├── entity/
│   │   ├── Entity.java            Classe abstraite de base (position, HP, speed, update, draw)
│   │   ├── Player.java            Le joueur (déplacement, tir, 2 personnages, animation)
│   │   ├── Enemy.java             Classe abstraite ennemi (IA de déplacement et tir)
│   │   ├── BugEnemy.java          Bug Informatique (insecte vert, fonce vers le joueur)
│   │   ├── ExamenEnemy.java       Examen Surprise (apparition en fondu, tire vite)
│   │   ├── SpawnedEnemy.java      Sbire pondu par le boss (très rapide)
│   │   └── Boss.java              Prof Duroc (boss final, 2 phases, système de rage)
│   ├── projectile/
│   │   ├── Projectile.java              Classe abstraite (mouvement, collision murs)
│   │   ├── UsbProjectile.java           Clé USB : rapide, faibles dégâts
│   │   ├── PrintProjectile.java         System.out.println() : lent, gros dégâts
│   │   ├── LaserSwordProjectile.java    Épée laser du ninja : très rapide, dégâts élevés
│   │   └── BossProjectile.java          Projectile du boss (tir en éventail en phase 2)
│   ├── powerup/
│   │   ├── PowerUp.java           Classe abstraite power-up (détection de collecte)
│   │   ├── CoffeeUp.java          Tasse de café : augmente la vitesse (2 niveaux)
│   │   └── UsbUp.java             Clé USB améliorée : améliore les deux armes
│   └── tile/
│       ├── Tile.java              Une tuile (flag collision)
│       └── TileManager.java       Chargement des cartes .txt et rendu procédural des tuiles
└── res/
    └── maps/
        ├── map1.txt               Niveau 1 : salle de classe
        └── map2.txt               Niveau 2 : Labo 404 / Salle des Serveurs
```

> **Note** : tous les graphismes sont dessinés procéduralement avec `Graphics2D`. Il n'y a aucune image externe ni fichier audio — tout est généré par le code.

---

## Contrôles

| Touche | Action |
|--------|--------|
| `Z` ou flèche haut    | Se déplacer vers le haut |
| `S` ou flèche bas     | Se déplacer vers le bas |
| `Q` ou flèche gauche  | Se déplacer vers la gauche |
| `D` ou flèche droite  | Se déplacer vers la droite |
| `Espace`              | Tirer |
| `E`                   | Changer d'arme (Étudiant) / Passer la porte de sortie |
| `Entrée`              | Valider (menus, transitions, rejouer) |
| `Échap`               | Quitter |

---

## Mécaniques de jeu

### Sélection de personnage

| Personnage | Arme(s) | Particularité |
|------------|---------|---------------|
| Étudiant 🎒 | Clé USB + System.out.println() | Polyvalent, deux armes commutables avec `E` |
| Ninja ⚔️   | Épée Laser uniquement | Cooldown de tir réduit, attaque de mêlée simultanée dans un rayon de 1,5 tiles |

### Armes

| Arme | Vitesse | Dégâts | Particularité |
|------|---------|--------|---------------|
| Clé USB | Rapide (7) | 10 | 3 projectiles en éventail si améliorée |
| System.out.println() | Lent (9) | 20 | Dégâts doublés (40) si améliorée |
| Épée Laser (Ninja) | Très rapide (10) | 35 | Traînée lumineuse violette/cyan + zone de mêlée (25 dégâts) |

> Le tir est **intelligent** : il cible automatiquement l'ennemi le plus proche. En l'absence d'ennemi, il tire dans la dernière direction de déplacement.

### Niveaux

| Niveau | Lieu | Ennemis | Condition de passage |
|--------|------|---------|----------------------|
| 1 | Salle de classe | 3 BugEnemy (lents) + 3 ExamenEnemy | Éliminer tous les ennemis, puis appuyer sur `E` près de la porte |
| 2 | Labo 404 | 4 BugEnemy + 3 ExamenEnemy (plus rapides) | Éliminer tous les ennemis, puis appuyer sur `E` près de la porte |
| 3 | Salle des Serveurs | Boss Prof Duroc + sbires invoqués | Vaincre le Prof Duroc, puis atteindre le serveur Moodle |

### Ennemis

| Ennemi | PV | Comportement | Points |
|--------|----|-------------|--------|
| Bug Informatique | 30 | Fonce vers le joueur et tire périodiquement (~1,2 s) | 100 |
| Examen Surprise | 25 | Apparition progressive en fondu, tire plus vite (~0,75 s) | 200 |
| Sbire (pondu par le boss) | 15 | Très rapide, fonce immédiatement, tire aussi | 50 |
| Prof Duroc (boss) | 200 | 2 phases, système de rage, invocation de sbires | 500 (victoire) |

### Boss — Prof Duroc

- **Phase 1** (HP > 100) : se déplace lentement (vitesse 1), tire 1 projectile vers le joueur toutes les ~1,3 s, invoque des sbires périodiquement
- **Phase 2** (HP ≤ 100) : vitesse doublée (2), tir en éventail de 3 projectiles toutes les ~0,67 s, invocations de sbires plus fréquentes
- **Système de rage** : après 3 hits encaissés, le boss entre en rage pendant 3 secondes — il charge le joueur à vitesse 5, inflige des dégâts au contact, et une aura rouge clignotante l'entoure avec une barre de progression visible. Toutes les 2 rages déclenchées, il invoque une vague de sbires en cercle (nombre croissant)
- **Timer de 20 secondes** : si le temps expire, le serveur explose — animation de particules et Game Over

### Power-ups

Les power-ups sont droppés aléatoirement à la mort d'un ennemi (30 % café, 20 % USB+, 50 % rien) et **persistent entre les niveaux**.

| Power-up | Effet |
|----------|-------|
| Tasse de café ☕ | 1re tasse : +2 vitesse / 2e tasse : +2 vitesse encore (max 7) |
| Clé USB améliorée 🔵 | Clé USB → tir en éventail (×3) / println() → dégâts doublés |

### Score

| Événement | Points |
|-----------|--------|
| Tuer un Bug Informatique | +100 |
| Tuer un Examen Surprise | +200 |
| Tuer un sbire | +50 |
| Hit sur le boss | +10 |
| Vaincre le boss et déposer le projet | +500 |

---

## Dépendances

Aucune dépendance externe. Le projet utilise uniquement :

- **Java SE 11+**
- **javax.swing** : fenêtre et rendu graphique (inclus dans le JDK)
- **java.awt** : graphismes 2D procéduraux (inclus dans le JDK)
- **javax.sound.sampled** : génération audio PCM synthétique (inclus dans le JDK)

---

## Contexte académique

Projet réalisé dans le cadre du module **PROJ-PROG 2025-2026** à l'**ESIR1 / ISTIC — Université de Rennes**.  
Date de rendu : **29 mai 2026 à 10h00**.  
Soutenance : **29 mai 2026 de 10h15 à 12h15**.