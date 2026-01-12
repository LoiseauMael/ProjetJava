# Moteur de Jeu JRPG 2D - LibGDX

Ce projet implémente un moteur de jeu de rôle (RPG) en 2D extensible, développé en Java avec le framework LibGDX. Il permet la création et l'édition de niveaux, d'ennemis et de PNJ via l'éditeur de cartes Tiled, sans modification du code source.

## 📋 Fonctionnalités Principales

* **Moteur de Jeu Extensible** : Architecture basée sur des états (Exploration, Combat, Menu).
* **Intégration Tiled** : Chargement des cartes, des collisions, des positions de départ, et des interactions (PNJ) directement depuis les fichiers `.tmx`.
* **Système de Combat** : Combat au tour par tour (inspiré des JRPG classiques) avec gestion des compétences et objets.
* **Gestion des Données** : Chargement des objets, compétences et boutiques via fichiers JSON.
* **Sauvegarde** : Système de sérialisation pour sauvegarder la progression du joueur.

## 🛠 Prérequis

* **Java** : JDK 17 (ou version supérieure).
* **Git** : Pour cloner le projet.

## 🚀 Installation et Exécution

1.  **Cloner le dépôt :**
    ```bash
    git clone https://github.com/LoiseauMael/ProjetJava
    cd JRPG
    ```

2.  **Lancer le jeu (Windows) :**
    Double-cliquez sur `gradlew.bat` ou exécutez dans l'invite de commande :
    ```bash
    gradlew.bat lwjgl3:run
    ```

3.  **Lancer le jeu (Mac/Linux) :**
    Ouvrez un terminal et exécutez :
    ```bash
    ./gradlew lwjgl3:run
    ```

## 🗺 Ajouter du contenu (Tiled)

Pour ajouter une nouvelle carte :
1.  Créez une carte `.tmx` dans `assets/tiled/map/`.
2.  Utilisez les couches d'objets pour définir les collisions et les zones d'interaction.
3.  Configurez les propriétés des objets (ex: `type` = `HealerNPC`) pour que le `MapLoader` les instancie automatiquement.

## 👤 Auteur

* **Loiseau Maël** - Développement complet (Moteur, Gameplay, Intégration).
