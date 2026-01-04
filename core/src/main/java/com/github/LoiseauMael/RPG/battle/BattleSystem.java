package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Enemy;
import com.github.LoiseauMael.RPG.Fighter;
import com.github.LoiseauMael.RPG.Player;
import com.badlogic.gdx.math.Rectangle; // Important !
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Map;

public class BattleSystem {

    // ... (Enums, variables, constructeur inchangés) ...
    public enum BattleState {
        WAITING, PLAYER_TURN, PLAYER_MOVING, PLAYER_SELECTING_TARGET, ENEMY_TURN, VICTORY, GAME_OVER
    }
    private Player player;
    private Enemy enemy;
    private BattleState state;
    private boolean hasMovedThisTurn = false;
    private BattleAction pendingAction;
    private Map<Fighter, Float> atbGauges;
    private static final float MAX_ATB = 100f;

    public BattleSystem(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.state = BattleState.WAITING;
        this.atbGauges = new HashMap<>();
        atbGauges.put(player, 0f);
        atbGauges.put(enemy, 0f);
    }

    public void update(float delta) {
        if (state == BattleState.WAITING) updateATB(delta);
        else if (state == BattleState.ENEMY_TURN) processEnemyTurn();
    }

    private void updateATB(float delta) {
        float playerInc = player.getVIT() * 2f * delta;
        float enemyInc = enemy.getVIT() * 2f * delta;
        atbGauges.put(player, Math.min(MAX_ATB, atbGauges.get(player) + playerInc));
        atbGauges.put(enemy, Math.min(MAX_ATB, atbGauges.get(enemy) + enemyInc));

        if (atbGauges.get(player) >= MAX_ATB) startPlayerTurn();
        else if (atbGauges.get(enemy) >= MAX_ATB) {
            state = BattleState.ENEMY_TURN;
            atbGauges.put(enemy, 0f);
        }
    }

    private void startPlayerTurn() {
        state = BattleState.PLAYER_TURN;
        atbGauges.put(player, 0f);
        hasMovedThisTurn = false;
    }

    // --- DEPLACEMENT ---
    public void startMoveSelection() {
        if (state == BattleState.PLAYER_TURN && !hasMovedThisTurn) state = BattleState.PLAYER_MOVING;
    }

    public boolean tryMovePlayerTo(float targetX, float targetY) {
        if (state != BattleState.PLAYER_MOVING) return false;

        float dist = Vector2.dst(player.get_positionX(), player.get_positionY(), targetX, targetY);

        if (dist <= player.getDEP() + 1.0f) {
            int tileX = (int) Math.floor(targetX);
            int tileY = (int) Math.floor(targetY);
            float spriteWidth = player.getSprite().getWidth();
            float spriteHeight = player.getSprite().getHeight();
            float finalX = (tileX + 0.5f) - (spriteWidth / 2f);
            float finalY = (tileY + 0.5f) - (spriteHeight / 2f);

            player.set_position(finalX, finalY);
            hasMovedThisTurn = true;
            state = BattleState.PLAYER_TURN;
            return true;
        }
        return false;
    }

    // --- ATTAQUE ---
    public void startTargetSelection(BattleAction action) {
        if (state == BattleState.PLAYER_TURN && action.canExecute(player)) {
            this.pendingAction = action;
            this.state = BattleState.PLAYER_SELECTING_TARGET;
        }
    }

    /**
     * NOUVELLE LOGIQUE : Intersection de Rectangles
     */
    public boolean tryAttackTarget(float clickX, float clickY) {
        if (state != BattleState.PLAYER_SELECTING_TARGET || pendingAction == null) return false;

        // 1. Définir la case cliquée (Rectangle de 1x1)
        int clickedTileX = (int) Math.floor(clickX);
        int clickedTileY = (int) Math.floor(clickY);
        Rectangle clickedTileRect = new Rectangle(clickedTileX, clickedTileY, 1f, 1f);

        // 2. Vérifier si on a cliqué sur une case ROUGE (Zone valide)
        Array<Vector2> validTiles = pendingAction.getTargetableTiles(player);
        boolean isClickOnValidTile = false;

        if (validTiles != null) {
            for (Vector2 tile : validTiles) {
                if ((int)tile.x == clickedTileX && (int)tile.y == clickedTileY) {
                    isClickOnValidTile = true;
                    break;
                }
            }
        } else {
            // Fallback (cercle)
            float dist = Vector2.dst(player.get_positionX(), player.get_positionY(), clickX, clickY);
            if (dist <= pendingAction.getRange()) isClickOnValidTile = true;
        }

        if (!isClickOnValidTile) return false;

        // 3. VÉRIFICATION COLLISION (Overlap)
        // Est-ce que le rectangle de la case cliquée touche la hitbox de l'ennemi ?
        if (clickedTileRect.overlaps(enemy.getBounds())) {
            pendingAction.execute(player, enemy);
            endPlayerTurn();
            return true;
        }

        return false;
    }

    public void cancelSelection() {
        if (state == BattleState.PLAYER_MOVING || state == BattleState.PLAYER_SELECTING_TARGET) {
            state = BattleState.PLAYER_TURN;
            pendingAction = null;
        }
    }

    public void passTurn() {
        if (state == BattleState.PLAYER_TURN) {
            state = BattleState.WAITING;
            atbGauges.put(player, 0f);
            hasMovedThisTurn = false;
            pendingAction = null;
        }
    }

    private void endPlayerTurn() {
        checkWinCondition();
        if (state != BattleState.VICTORY && state != BattleState.GAME_OVER) {
            state = BattleState.WAITING;
        }
        pendingAction = null;
    }

    private void processEnemyTurn() {
        new AttackAction().execute(enemy, player);
        endPlayerTurn();
    }

    private void checkWinCondition() {
        if (player.getPV() <= 0) state = BattleState.GAME_OVER;
        else if (enemy.getPV() <= 0) {
            state = BattleState.VICTORY;
            applyRewards();
        }
    }

    private void applyRewards() {
        int moneyGain = enemy.getMoneyReward();
        player.addMoney(moneyGain);

        int baseExp = enemy.getExpReward();
        int levelDiff = enemy.getLevel() - player.getLevel();
        float multiplier = 1.0f;

        if (levelDiff > 0) multiplier += (levelDiff * 0.2f);
        else if (levelDiff < 0) multiplier += (levelDiff * 0.1f);

        if (multiplier < 0.1f) multiplier = 0.1f;

        int finalExp = Math.round(baseExp * multiplier);
        player.gainExp(finalExp);

        com.badlogic.gdx.Gdx.app.log("Battle", "Victoire ! Gain: " + moneyGain + " Or, " + finalExp + " EXP.");
    }

    // Getters
    public float getPlayerATB() { return atbGauges.get(player); }
    public float getEnemyATB() { return atbGauges.get(enemy); }
    public BattleState getState() { return state; }
    public Enemy getEnemy() { return enemy; }
    public boolean hasMoved() { return hasMovedThisTurn; }
    public BattleAction getPendingAction() { return pendingAction; }
}
