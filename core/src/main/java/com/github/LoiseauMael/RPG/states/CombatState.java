package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.LoiseauMael.RPG.Enemy;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.BattleSystem;

public class CombatState implements IGameState {

    private Main game;
    private Enemy enemy;
    private Label statsLabel;
    private ShapeRenderer shapeRenderer;

    public CombatState(Main game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
    }

    @Override
    public void enter() {
        if (this.enemy == null) {
            game.changeState(game.explorationState);
            return;
        }

        // 1. Désactiver l'IA de déplacement de l'ennemi (pour qu'il ne se balade pas)
        enemy.setInCombat(true);

        // 2. Désactiver le déplacement libre du joueur
        game.player.setInputEnabled(false);

        // 3. Gestion Distanciation : Si trop proche (< 3 cases), on décale l'ennemi
        if (game.player.getGridDistance(enemy) < 3) {
            int playerX = game.player.getTileX();
            int enemyX = enemy.getTileX();
            // On repousse l'ennemi à l'opposé du joueur
            int newX = (enemyX >= playerX) ? playerX + 3 : playerX - 3;

            enemy.setGridPosition(newX, enemy.getTileY());
            enemy.snapToGrid();
        }

        // Initialisation du système de combat
        // MODIFICATION : On passe 'game' pour la gestion des sauvegardes/morts
        game.battleSystem = new BattleSystem(game, game.player, this.enemy);

        setupCombatUI();
        Gdx.input.setInputProcessor(game.combatStage);
    }

    private void setupCombatUI() {
        game.combatStage.clear();
        Table table = new Table();
        table.setFillParent(true);

        // Label des statistiques (PV, ATB)
        statsLabel = new Label("", game.skin);
        statsLabel.setFontScale(1.2f);
        table.add(statsLabel).colspan(2).pad(20).top().row();

        // Espace vide pour centrer le reste
        table.add().expand().colspan(2).row();

        // Ajout de la zone de logs si elle existe
        if (game.battleSystem.getLogScroll() != null) {
            table.add(game.battleSystem.getLogScroll()).width(500).height(150).colspan(2).pad(10).left().row();
        }

        // --- BARRE DE BOUTONS ---
        Table buttonTable = new Table();

        // 1. Bouton BOUGER
        TextButton moveBtn = new TextButton("Bouger", game.skin);
        moveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) game.battleSystem.enableMovePhase();
            }
        });
        buttonTable.add(moveBtn).width(100).height(50).pad(5);

        // 2. Bouton ATTAQUER
        TextButton attackBtn = new TextButton("Attaquer", game.skin);
        attackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) game.battleSystem.startTargetSelection(new AttackAction());
            }
        });
        buttonTable.add(attackBtn).width(100).height(50).pad(5);

        // 3. Bouton PASSER
        TextButton passBtn = new TextButton("Passer", game.skin);
        passBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) game.battleSystem.playerPassTurn();
            }
        });
        buttonTable.add(passBtn).width(100).height(50).pad(5);

        // 4. Bouton FUIR
        TextButton fleeBtn = new TextButton("Fuir", game.skin);
        fleeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BattleSystem.addLog("Fuite !");
                game.changeState(game.explorationState);
            }
        });
        buttonTable.add(fleeBtn).width(100).height(50).pad(5);

        table.add(buttonTable).bottom().padBottom(20);
        game.combatStage.addActor(table);
    }

    private void updateStatsLabel() {
        if (game.player != null && enemy != null && game.battleSystem != null) {
            String stats = String.format("JOUEUR: %d/%d PV (ATB: %.0f%%)\nENNEMI: %d PV (ATB: %.0f%%)",
                game.player.getPV(), game.player.getMaxPV(), game.battleSystem.getPlayerATB(),
                enemy.getPV(), game.battleSystem.getEnemyATB());
            statsLabel.setText(stats);
        }
    }

    @Override
    public void exit() {
        game.combatStage.clear();

        // IMPORTANT : On remet l'état normal en sortant du combat
        if (enemy != null) enemy.setInCombat(false);
        // On réactive les contrôles de déplacement libre du joueur pour l'exploration
        if (game.player != null) game.player.setInputEnabled(true);
    }

    @Override
    public void update(float delta) {
        if (game.battleSystem != null) {
            game.battleSystem.update(delta);

            // MODIFICATION : On retire la gestion de la victoire ici car elle est
            // gérée par le BattleSystem (clic pour quitter après les logs)

            if (game.battleSystem.getState() == BattleSystem.BattleState.GAME_OVER) {
                game.changeState(game.startMenuState);
            }
        }

        // --- MISE À JOUR DES ENTITÉS ---
        if (enemy != null) enemy.update(delta);
        if (game.player != null) game.player.update(delta);

        updateStatsLabel();
        game.combatStage.act(delta);

        handleInput();
    }

    @Override
    public void handleInput() {
        // Gestion du clic sur la grille
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            game.camera.unproject(touchPos);
            if (game.battleSystem != null) {
                game.battleSystem.handleGridClick(touchPos.x, touchPos.y);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.tiledMapRenderer != null) {
            game.tiledMapRenderer.setView(game.camera);
            game.tiledMapRenderer.render();
        }

        drawGridHighlights();

        batch.setProjectionMatrix(game.camera.combined);
        batch.begin();
        if (game.player != null) game.player.draw(batch);
        if (enemy != null) enemy.draw(batch);
        batch.end();

        game.combatStage.draw();
    }

    private void drawGridHighlights() {
        if (game.battleSystem == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(game.camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Zone de déplacement (BLEU)
        if (game.battleSystem.getState() == BattleSystem.BattleState.PLAYER_MOVING) {
            shapeRenderer.setColor(new Color(0, 0, 1, 0.3f));
            for (Vector2 tile : game.battleSystem.getValidMoveTiles()) {
                shapeRenderer.rect(tile.x, tile.y, 1, 1);
            }
        }
        // Zone d'attaque (ROUGE)
        else if (game.battleSystem.getState() == BattleSystem.BattleState.PLAYER_SELECTING_TARGET) {
            shapeRenderer.setColor(new Color(1, 0, 0, 0.3f));
            for (Vector2 tile : game.battleSystem.getValidAttackTiles()) {
                shapeRenderer.rect(tile.x, tile.y, 1, 1);
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
