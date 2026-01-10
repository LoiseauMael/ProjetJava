package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.github.LoiseauMael.RPG.Enemy;
import com.github.LoiseauMael.RPG.Entity;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.SwordMan;
import com.github.LoiseauMael.RPG.npcs.NPC;

public class ExplorationState implements IGameState {

    private Main game;

    public ExplorationState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        // Sécurité : recréer un joueur par défaut s'il n'existe pas
        if (game.player == null) {
            game.player = new SwordMan(10, 10);
        }

        // Repositionner la caméra sur le joueur
        if (game.player != null) {
            game.camera.position.set(game.player.get_positionX(), game.player.get_positionY(), 0);
            game.camera.update();
        }
        // On désactive les processeurs d'input spécifiques (comme les stages) pour laisser handleInput gérer le clavier
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void exit() {}

    @Override
    public void update(float delta) {
        // 1. Mise à jour du joueur
        if (game.player != null) {
            game.player.update(delta);
        }

        // 2. Mise à jour des entités (Ennemis, NPC)
        if (game.entities != null) {
            for (Entity entity : game.entities) {
                entity.update(delta);

                // Détection automatique du combat avec les ennemis
                if (entity instanceof Enemy) {
                    Enemy enemy = (Enemy) entity;
                    if (game.player.getBoundingBox().overlaps(enemy.getBoundingBox())) {
                        game.combatState.setEnemy(enemy);
                        game.changeState(game.combatState);
                        return;
                    }
                }
            }
        }

        // 3. La caméra suit le joueur
        if (game.player != null) {
            game.camera.position.x = game.player.get_positionX();
            game.camera.position.y = game.player.get_positionY();
            game.camera.update();
        }

        // Touche ECHAP pour le menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.changeState(game.menuState);
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.tiledMapRenderer != null) {
            game.tiledMapRenderer.setView(game.camera);
            game.tiledMapRenderer.render();
        }

        batch.setProjectionMatrix(game.camera.combined);
        batch.begin();

        // Tri des entités par Y pour la profondeur visuelle
        if (game.entities != null) {
            game.entities.sort((e1, e2) -> Float.compare(e2.get_positionY(), e1.get_positionY()));
            for (Entity entity : game.entities) {
                entity.draw(batch);
            }
        }

        if (game.player != null) {
            game.player.draw(batch);
        }
        batch.end();
    }

    @Override
    public void handleInput() {
        // Détection de l'interaction avec la touche F
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            interactWithNearbyNPC();
        }
    }

    /**
     * Cherche un NPC à proximité et lance le dialogue s'il y en a un.
     */
    private void interactWithNearbyNPC() {
        float interactionRange = 1.8f; // Distance de détection

        for (NPC npc : game.npcs) {
            // Calcul de la distance entre le joueur et le PNJ
            float dist = Vector2.dst(game.player.get_positionX(), game.player.get_positionY(),
                npc.get_positionX(), npc.get_positionY());

            if (dist <= interactionRange) {
                // Le NPC se tourne vers le joueur
                npc.lookAt(game.player);

                // Préparation et lancement de l'état de dialogue
                game.dialogueState.setNPC(npc);
                game.changeState(game.dialogueState);
                break; // On ne parle qu'à un seul PNJ à la fois
            }
        }
    }
}
