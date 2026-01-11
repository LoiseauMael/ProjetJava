package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.save.SaveManager;

public class StartMenuState implements IGameState {
    private final Main game;

    public StartMenuState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        // On définit le processeur d'entrée sur le Stage du menu
        Gdx.input.setInputProcessor(game.startStage);
        // On reconstruit le menu à chaque entrée pour vérifier l'état de la sauvegarde
        rebuildMenu();
    }

    private void rebuildMenu() {
        game.startStage.clear();

        Table table = new Table();
        table.setFillParent(true);
        // Espacement par défaut pour les boutons
        table.defaults().pad(10).width(300).height(60);

        boolean hasSave = SaveManager.saveExists();

        if (hasSave) {
            // --- BOUTON CONTINUER ---
            TextButton btnContinue = new TextButton("Continuer", game.skin);
            btnContinue.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // 1. On charge les données du jeu depuis le fichier JSON
                    // (Cela met à jour game.player avec la position X/Y sauvegardée)
                    SaveManager.loadGame(game);

                    // 2. IMPORTANT : On sauvegarde temporairement la position chargée
                    // car l'appel suivant à game.loadMap() va remettre le joueur au point de spawn par défaut (Tiled).
                    float savedX = game.player.get_positionX();
                    float savedY = game.player.get_positionY();

                    // 3. On charge physiquement la map et les entités
                    // (Cela change aussi l'état vers ExplorationState, mais réinitialise la position du joueur)
                    game.loadMap(game.currentMapName);

                    // 4. On réapplique la position sauvegardée pour écraser le spawn par défaut
                    game.player.set_position(savedX, savedY);

                    // 5. On met à jour la caméra immédiatement pour éviter un saut d'image
                    game.camera.position.set(savedX, savedY, 0);
                    game.camera.update();
                }
            });
            table.add(btnContinue).row();

            // --- BOUTON SUPPRIMER ---
            TextButton btnDelete = new TextButton("Supprimer Sauvegarde", game.skin);
            // On met le bouton en rouge pour alerter l'utilisateur
            btnDelete.setColor(Color.FIREBRICK);
            btnDelete.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager.deleteSave();
                    rebuildMenu(); // Rafraîchir l'affichage immédiatement
                }
            });
            table.add(btnDelete).row();

        } else {
            // --- BOUTON NOUVELLE PARTIE ---
            // N'apparaît que s'il n'y a pas de sauvegarde existante
            TextButton btnNewGame = new TextButton("Nouvelle Partie", game.skin);
            btnNewGame.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.changeState(game.classSelectionState);
                }
            });
            table.add(btnNewGame).row();
        }

        // --- BOUTON QUITTER ---
        TextButton btnExit = new TextButton("Quitter le jeu", game.skin);
        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(btnExit).row();

        game.startStage.addActor(table);
    }

    @Override
    public void update(float delta) {
        // Logique de mise à jour du stage (animations, etc.)
        game.startStage.act(delta);
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        // Effacer l'écran avec un fond noir
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dessiner le menu
        game.startStage.draw();
    }

    @Override
    public void handleInput() {
        // Le Stage gère les clics
    }

    @Override
    public void exit() {
        game.startStage.clear();
    }
}
