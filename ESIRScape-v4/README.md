# ESIRScape v4

## Nouveautés v4

### Choix de personnage
Au démarrage, choisissez entre :
- **Étudiant** — Clé USB & System.out.println(), polyvalent
- **Ninja** — Teint noir clair, longue épée laser violette/cyan, attaque rapide avec dégâts de zone en mêlée

### Niveaux restructurés
- **Niveau 1 — Salle de classe** : Ennemis lents (speed=1), ambiance école
- **Niveau 2 — Labo 404** : Nouveau décor laboratoire vert, ennemis plus rapides (speed=2), power-ups du niveau 1 **conservés**
- **Niveau final — Salle des Serveurs** : Musique de boss en boucle, **20 secondes** pour vaincre le Prof Duroc. Le serveur **explose** si le temps est écoulé !

### Tir intelligent
Le joueur tire toujours vers **l'ennemi le plus proche** automatiquement, sans besoin de viser manuellement.

### Power-ups conservés
Les power-ups (Café et Clé USB+) obtenus au niveau 1 sont **conservés** au niveau 2.

## Compilation

```bash
mkdir out
find src -name "*.java" | xargs javac -d out
cp -r res out/
```

## Exécution

```bash
java -cp out main.Main
```

## Contrôles
- **ZQSD / Flèches** : Déplacer
- **ESPACE** : Tirer (toujours vers l'ennemi le plus proche)
- **E** : Changer d'arme / Utiliser une porte
- **ENTRÉE** : Confirmer / Avancer
- **ÉCHAP** : Quitter
