package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.Enemy;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.BattleSystem;
import com.github.LoiseauMael.RPG.skills.Skill;

// AJOUT : Implements InputProcessor pour gérer proprement les clics sur la grille
public class CombatState implements IGameState, InputProcessor {

    private Main game;
    private Enemy enemy;
    private Label statsLabel;
    private ShapeRenderer shapeRenderer;

    // UI Elements
    private Table skillsTable;

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

        enemy.setInCombat(true);
        game.player.setInputEnabled(false);

        if (game.player.getGridDistance(enemy) < 3) {
            int playerX = game.player.getTileX();
            int enemyX = enemy.getTileX();
            int newX = (enemyX >= playerX) ? playerX + 3 : playerX - 3;
            enemy.setGridPosition(newX, enemy.getTileY());
            enemy.snapToGrid();
        }

        game.battleSystem = new BattleSystem(game, game.player, this.enemy);
        setupCombatUI();

        // CORRECTION IMPORTANTE : InputMultiplexer
        // L'UI (combatStage) reçoit les clics en premier. S'il ne les traite pas (clic à côté des boutons),
        // alors 'this' (CombatState) les reçoit pour gérer la grille.
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(game.combatStage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void setupCombatUI() {
        game.combatStage.clear();
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        statsLabel = new Label("", game.skin);
        statsLabel.setFontScale(1.2f);
        mainTable.add(statsLabel).colspan(2).pad(20).top().row();

        mainTable.add().expand().colspan(2).row();

        if (game.battleSystem.getLogScroll() != null) {
            mainTable.add(game.battleSystem.getLogScroll()).width(500).height(150).colspan(2).pad(10).left().row();
        }

        Table buttonTable = new Table();

        // 1. Bouton BOUGER
        TextButton moveBtn = new TextButton("Bouger", game.skin);
        moveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) {
                    game.battleSystem.enableMovePhase();
                    hideSkillsTable();
                }
            }
        });
        buttonTable.add(moveBtn).width(100).height(50).pad(5);

        // 2. Bouton ATTAQUER
        TextButton attackBtn = new TextButton("Attaquer", game.skin);
        attackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) {
                    game.battleSystem.startTargetSelection(new AttackAction());
                    hideSkillsTable();
                }
            }
        });
        buttonTable.add(attackBtn).width(100).height(50).pad(5);

        // 3. Bouton ARTS
        TextButton artsBtn = new TextButton("Arts", game.skin);
        artsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) game.battleSystem.cancelSelection();
                showSkillList(Skill.SkillType.ART);
            }
        });
        buttonTable.add(artsBtn).width(100).height(50).pad(5);

        // 4. Bouton MAGIE
        TextButton magicBtn = new TextButton("Magie", game.skin);
        magicBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) game.battleSystem.cancelSelection();
                showSkillList(Skill.SkillType.MAGIC);
            }
        });
        buttonTable.add(magicBtn).width(100).height(50).pad(5);

        // 5. Bouton PASSER
        TextButton passBtn = new TextButton("Passer", game.skin);
        passBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.battleSystem != null) {
                    game.battleSystem.playerPassTurn();
                    hideSkillsTable();
                }
            }
        });
        buttonTable.add(passBtn).width(100).height(50).pad(5);

        TextButton fleeBtn = new TextButton("Fuir", game.skin);
        fleeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                BattleSystem.addLog("Fuite !");
                game.changeState(game.explorationState);
            }
        });
        buttonTable.add(fleeBtn).width(100).height(50).pad(5);

        mainTable.add(buttonTable).bottom().padBottom(20);
        game.combatStage.addActor(mainTable);

        skillsTable = new Table();
        // On rajoute un fond pour être sûr que le tableau bloque les clics s'il y a des trous entre les boutons
        // skillsTable.setBackground(game.skin.newDrawable("white", 0, 0, 0, 0.8f)); // Optionnel
        skillsTable.setVisible(false);
        game.combatStage.addActor(skillsTable);
    }

    private void hideSkillsTable() {
        if (skillsTable != null) skillsTable.setVisible(false);
    }

    private void showSkillList(Skill.SkillType type) {
        if (game.battleSystem == null || game.player == null) return;

        skillsTable.clear();
        skillsTable.setVisible(true);
        skillsTable.setSize(300, 300);
        skillsTable.setPosition(Gdx.graphics.getWidth()/2f - 150, Gdx.graphics.getHeight()/2f - 50);

        String titleText = (type == Skill.SkillType.ART) ? "--- ARTS (PA) ---" : "--- MAGIE (PM) ---";
        skillsTable.add(new Label(titleText, game.skin)).pad(10).row();

        Array<Skill> skills = game.player.getSkillsByType(type);

        if (skills.size == 0) {
            skillsTable.add(new Label("Aucune compétence.", game.skin)).pad(10).row();
        } else {
            for (final Skill s : skills) {
                String costText = (type == Skill.SkillType.ART) ? s.cost + " PA" : s.cost + " PM";
                TextButton btn = new TextButton(s.getName() + " (" + costText + ")", game.skin);

                if (!s.canExecute(game.player)) {
                    btn.setColor(Color.GRAY);
                    btn.setDisabled(true);
                }

                btn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (btn.isDisabled()) return;

                        // IMPORTANT : On annule d'abord toute sélection en cours pour éviter les conflits
                        game.battleSystem.cancelSelection();

                        if (s.targetType == Skill.TargetType.SELF) {
                            s.execute(game.player, game.player);
                            game.battleSystem.passTurn();
                        } else {
                            game.battleSystem.startTargetSelection(s);
                        }
                        hideSkillsTable();
                    }
                });
                skillsTable.add(btn).fillX().pad(2).row();
            }
        }

        TextButton closeBtn = new TextButton("Fermer", game.skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideSkillsTable();
                // Si on ferme, on revient à l'état normal (au cas où)
                if(game.battleSystem.getState() == BattleSystem.BattleState.PLAYER_SELECTING_TARGET) {
                    game.battleSystem.cancelSelection();
                }
            }
        });
        skillsTable.add(closeBtn).padTop(10).width(80);
    }

    private void updateStatsLabel() {
        if (game.player != null && enemy != null && game.battleSystem != null) {
            String stats = String.format("JOUEUR: %d/%d PV | %d PA | %d PM (ATB: %.0f%%)\nENNEMI: %d PV (ATB: %.0f%%)",
                game.player.getPV(), game.player.getMaxPV(),
                game.player.getPA(), game.player.getPM(),
                game.battleSystem.getPlayerATB(),
                enemy.getPV(), game.battleSystem.getEnemyATB());
            statsLabel.setText(stats);
        }
    }

    @Override
    public void exit() {
        game.combatStage.clear();
        if (enemy != null) enemy.setInCombat(false);
        if (game.player != null) game.player.setInputEnabled(true);
    }

    @Override
    public void update(float delta) {
        if (game.battleSystem != null) {
            game.battleSystem.update(delta);
            if (game.battleSystem.getState() == BattleSystem.BattleState.GAME_OVER) {
                game.changeState(game.startMenuState);
            }
        }

        if (enemy != null) enemy.update(delta);
        if (game.player != null) game.player.update(delta);

        updateStatsLabel();
        game.combatStage.act(delta);

        // Note : Plus besoin d'appeler handleInput() ici car on utilise InputProcessor
    }

    @Override
    public void handleInput() {
        // Vide ou supprimé, car géré par touchDown ci-dessous
    }

    // --- IMPLEMENTATION INPUT PROCESSOR (Gestion Clics Grille) ---

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Cette méthode n'est appelée QUE si le Stage (UI) n'a pas traité le clic.
        // Donc on est sûr que le joueur a cliqué sur la carte, pas sur un bouton.

        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        game.camera.unproject(touchPos);

        if (game.battleSystem != null) {
            game.battleSystem.handleGridClick(touchPos.x, touchPos.y);
        }

        return true; // On a traité l'input
    }

    // Méthodes InputProcessor non utilisées (obligatoires à implémenter)
    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

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

        if (game.battleSystem.getState() == BattleSystem.BattleState.PLAYER_MOVING) {
            shapeRenderer.setColor(new Color(0, 0, 1, 0.3f));
            for (Vector2 tile : game.battleSystem.getValidMoveTiles()) {
                shapeRenderer.rect(tile.x, tile.y, 1, 1);
            }
        }
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
