package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.github.LoiseauMael.RPG.Enemy;
import com.github.LoiseauMael.RPG.Entity;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.SwordMan;
import com.github.LoiseauMael.RPG.npcs.MapExit;
import com.github.LoiseauMael.RPG.npcs.NPC;

public class ExplorationState implements IGameState {

    private Main game;

    public ExplorationState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        if (game.player == null) {
            game.player = new SwordMan(10, 10);
        }

        if (game.player != null) {
            game.camera.position.set(game.player.get_positionX(), game.player.get_positionY(), 0);
            game.camera.update();
        }
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

        // 2. Mise à jour des entités
        if (game.entities != null) {
            for (int i = 0; i < game.entities.size; i++) {
                Entity entity = game.entities.get(i);
                entity.update(delta);

                // --- DÉTECTION COMBAT ---
                if (entity instanceof Enemy) {
                    Enemy enemy = (Enemy) entity;

                    // --- MODIFICATION ICI ---
                    // On lance le combat seulement si les boites se touchent ET que le joueur N'EST PAS invincible
                    if (game.player.getBoundingBox().overlaps(enemy.getBoundingBox())) {

                        // Si le joueur est en cooldown de fuite, on ignore la collision
                        if (game.player.isInvincible()) {
                            continue;
                        }

                        // Sinon, on lance le combat
                        game.combatState.setEnemy(enemy);
                        game.changeState(game.combatState);
                        return;
                    }
                    // ------------------------
                }

                // --- DÉTECTION SORTIE DE CARTE (TRANSITION) ---
                if (entity instanceof MapExit) {
                    MapExit exit = (MapExit) entity;
                    // On vérifie si le joueur marche sur la sortie
                    if (game.player.getBoundingBox().overlaps(exit.getBoundingBox())) {

                        System.out.println("Sortie détectée vers : " + exit.getTargetMap());
                        game.deadEnemyIds.clear();
                        game.loadMap(exit.getTargetMap(), exit.getTargetX(), exit.getTargetY());

                        return;
                    }
                }
            }
        }

        // 3. Caméra
        if (game.player != null) {
            game.camera.position.x = game.player.get_positionX();
            game.camera.position.y = game.player.get_positionY();
            game.camera.update();
        }

        // Menu Pause
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

        // Tri pour l'affichage (les entités plus bas sont devant)
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            interactWithNearbyNPC();
        }
    }

    private void interactWithNearbyNPC() {
        float interactionRange = 1.8f;
        for (NPC npc : game.npcs) {
            float dist = Vector2.dst(game.player.get_positionX(), game.player.get_positionY(),
                npc.get_positionX(), npc.get_positionY());
            if (dist <= interactionRange) {
                npc.lookAt(game.player);
                game.dialogueState.setNPC(npc);
                game.changeState(game.dialogueState);
                break;
            }
        }
    }
}
