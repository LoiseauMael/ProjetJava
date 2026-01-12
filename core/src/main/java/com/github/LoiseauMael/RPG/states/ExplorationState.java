package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.github.LoiseauMael.RPG.model.entities.Enemy;
import com.github.LoiseauMael.RPG.model.entities.Entity;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.model.entities.SwordMan;
import com.github.LoiseauMael.RPG.npcs.MapExit;
import com.github.LoiseauMael.RPG.npcs.NPC;

/**
 * État principal du jeu où le joueur se déplace librement sur la carte.
 * <p>
 * Responsabilités :
 * <ul>
 * <li>Gestion de la caméra (suivi du joueur).</li>
 * <li>Mise à jour de la physique et des déplacements.</li>
 * <li>Détection des collisions avec les Ennemis (déclenche {@link CombatState}).</li>
 * <li>Détection des sorties de carte (charge une nouvelle map).</li>
 * <li>Gestion des interactions avec les PNJ (touche F).</li>
 * </ul>
 */
public class ExplorationState implements IGameState {

    private Main game;

    public ExplorationState(Main game) {
        this.game = game;
    }

    /**
     * Initialise la caméra sur le joueur et réinitialise l'InputProcessor.
     * Si le joueur n'existe pas (premier lancement), en crée un par défaut.
     */
    @Override
    public void enter() {
        if (game.player == null) {
            game.player = new SwordMan(10, 10);
        }

        if (game.player != null) {
            game.camera.position.set(game.player.get_positionX(), game.player.get_positionY(), 0);
            game.camera.update();
        }
        Gdx.input.setInputProcessor(null); // On repasse en mode Polling (clavier)
    }

    @Override
    public void exit() {}

    /**
     * Met à jour toutes les entités et vérifie les triggers (Combat / Transition).
     */
    @Override
    public void update(float delta) {
        // 1. Mise à jour du joueur (mouvement + physique)
        if (game.player != null) {
            game.player.update(delta);
        }

        // 2. Mise à jour des entités (Ennemis, PNJs, Sorties)
        if (game.entities != null) {
            for (int i = 0; i < game.entities.size; i++) {
                Entity entity = game.entities.get(i);
                entity.update(delta);

                // --- DÉTECTION COMBAT ---
                if (entity instanceof Enemy) {
                    Enemy enemy = (Enemy) entity;

                    // Si collision avec un ennemi
                    if (game.player.getBoundingBox().overlaps(enemy.getBoundingBox())) {
                        // On ignore si le joueur est invulnérable (après une fuite)
                        if (game.player.isInvincible()) {
                            continue;
                        }
                        // Sinon : Transition vers le combat
                        game.combatState.setEnemy(enemy);
                        game.changeState(game.combatState);
                        return;
                    }
                }

                // --- DÉTECTION SORTIE DE CARTE ---
                if (entity instanceof MapExit) {
                    MapExit exit = (MapExit) entity;
                    if (game.player.getBoundingBox().overlaps(exit.getBoundingBox())) {
                        System.out.println("Sortie détectée vers : " + exit.getTargetMap());
                        // On nettoie la liste des ennemis morts pour la nouvelle zone (optionnel)
                        game.deadEnemyIds.clear();
                        game.loadMap(exit.getTargetMap(), exit.getTargetX(), exit.getTargetY());
                        return;
                    }
                }
            }
        }

        // 3. Suivi Caméra
        if (game.player != null) {
            game.camera.position.x = game.player.get_positionX();
            game.camera.position.y = game.player.get_positionY();
            game.camera.update();
        }

        // Menu Pause (Echap)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.changeState(game.menuState);
        }
    }

    /**
     * Dessine la carte (TiledMap), puis les entités triées par profondeur (Y-sort), puis le joueur.
     */
    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Rendu de la map (Sol, Murs)
        if (game.tiledMapRenderer != null) {
            game.tiledMapRenderer.setView(game.camera);
            game.tiledMapRenderer.render();
        }

        batch.setProjectionMatrix(game.camera.combined);
        batch.begin();

        // Tri des entités pour gérer la profondeur (ce qui est plus "bas" est dessiné "devant")
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

    /** Vérifie la distance avec les PNJs et lance le dialogue si proche. */
    private void interactWithNearbyNPC() {
        float interactionRange = 1.8f;
        for (NPC npc : game.npcs) {
            float dist = Vector2.dst(game.player.get_positionX(), game.player.get_positionY(),
                npc.get_positionX(), npc.get_positionY());
            if (dist <= interactionRange) {
                npc.lookAt(game.player); // Le PNJ se tourne vers le joueur
                game.dialogueState.setNPC(npc);
                game.changeState(game.dialogueState);
                break;
            }
        }
    }
}
