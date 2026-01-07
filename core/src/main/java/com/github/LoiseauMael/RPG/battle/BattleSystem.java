package com.github.LoiseauMael.RPG.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.Enemy;
import com.github.LoiseauMael.RPG.Player;

public class BattleSystem {

    public enum BattleState {
        WAITING,                // Remplissage des barres ATB
        PLAYER_TURN,            // Le joueur doit choisir une action
        PLAYER_MOVING,          // Le joueur choisit où se déplacer
        PLAYER_SELECTING_TARGET,// Le joueur choisit la cible de son attaque
        ENEMY_TURN,             // L'ennemi réfléchit et agit
        VICTORY,
        GAME_OVER
    }

    private Player player;
    private Enemy enemy;
    private BattleState state;

    // ATB (Active Time Battle) : 0 à 100
    private float playerATB;
    private float enemyATB;

    // Logique du tour
    private boolean hasMoved;
    private BattleAction pendingAction; // L'action que le joueur (ou l'ennemi) veut faire
    private float turnTimer; // Pour donner un délai visuel à l'IA

    // --- SYSTEME DE LOGS (Static pour être accessible depuis les Actions) ---
    private static Array<String> combatLogs = new Array<>();

    public BattleSystem(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.state = BattleState.WAITING;
        this.playerATB = 0;
        this.enemyATB = 0;
        this.hasMoved = false;

        // On vide les logs au début d'un nouveau combat
        combatLogs.clear();
        addLog("Le combat commence !");
    }

    /**
     * Ajoute un message au journal de combat (Visible Console + UI).
     */
    public static void addLog(String message) {
        Gdx.app.log("COMBAT", message); // Console
        combatLogs.add("> " + message); // UI

        // On garde seulement les 6 derniers messages
        if (combatLogs.size > 6) {
            combatLogs.removeIndex(0);
        }
    }

    public static Array<String> getLogs() {
        return combatLogs;
    }

    public void update(float delta) {
        // 1. Vérification des conditions de fin
        if (player.getPV() <= 0) {
            state = BattleState.GAME_OVER;
            return;
        }
        if (enemy.getPV() <= 0) {
            state = BattleState.VICTORY;
            return;
        }

        // 2. Gestion de l'ATB (Si personne ne joue)
        if (state == BattleState.WAITING) {
            playerATB += player.getVIT() * delta * 5.0f;
            enemyATB += enemy.getVIT() * delta * 5.0f;

            if (playerATB >= 100) {
                playerATB = 100;
                startPlayerTurn();
            } else if (enemyATB >= 100) {
                enemyATB = 100;
                startEnemyTurn();
            }
        }

        // 3. Logique IA (Tour Ennemi)
        if (state == BattleState.ENEMY_TURN) {
            updateEnemyTurn(delta);
        }
    }

    // ==========================================
    // LOGIQUE JOUEUR
    // ==========================================

    private void startPlayerTurn() {
        state = BattleState.PLAYER_TURN;
        hasMoved = false;
        pendingAction = null;
        player.restorePA(2);
        addLog("C'est a votre tour !");
    }

    public void startMoveSelection() {
        if (state == BattleState.PLAYER_TURN && !hasMoved) {
            state = BattleState.PLAYER_MOVING;
            addLog("Cliquez sur une case pour bouger.");
        }
    }

    public void tryMovePlayerTo(float targetX, float targetY) {
        float dist = Vector2.dst(player.get_positionX(), player.get_positionY(), targetX, targetY);

        if (dist <= player.getDEP()) {
            player.set_position(targetX, targetY);
            hasMoved = true;
            state = BattleState.PLAYER_TURN;
        } else {
            addLog("Trop loin pour se deplacer !");
        }
    }

    public void startTargetSelection(BattleAction action) {
        if (state == BattleState.PLAYER_TURN) {
            if (action.canExecute(player)) {
                this.pendingAction = action;
                state = BattleState.PLAYER_SELECTING_TARGET;
                addLog("Selectionnez votre cible (" + action.getName() + ")");
            } else {
                addLog("Pas assez de ressources (PA/PM) !");
            }
        }
    }

    public void tryAttackTarget(float x, float y) {
        float distToEnemy = Vector2.dst(x, y, enemy.get_positionX(), enemy.get_positionY());

        if (distToEnemy < 1.5f) {
            boolean isInRange = false;
            Array<Vector2> specificTiles = pendingAction.getTargetableTiles(player);

            if (specificTiles != null) {
                // Vérification zone spécifique (ex: Croix)
                int enemyX = Math.round(enemy.get_positionX());
                int enemyY = Math.round(enemy.get_positionY());
                for (Vector2 tile : specificTiles) {
                    if ((int)tile.x == enemyX && (int)tile.y == enemyY) {
                        isInRange = true; break;
                    }
                }
                if (!isInRange) addLog("Ennemi hors de la zone d'effet !");
            }
            else {
                // Vérification distance simple
                float distPlayerEnemy = Vector2.dst(player.get_positionX(), player.get_positionY(),
                    enemy.get_positionX(), enemy.get_positionY());
                if (distPlayerEnemy <= pendingAction.getRange()) isInRange = true;
                else addLog("Cible hors de portee !");
            }

            if (isInRange) {
                pendingAction.execute(player, enemy);
                passTurn();
            }
        } else {
            addLog("Cliquez sur l'ennemi pour valider !");
        }
    }

    public void cancelSelection() {
        if (state == BattleState.PLAYER_MOVING || state == BattleState.PLAYER_SELECTING_TARGET) {
            state = BattleState.PLAYER_TURN;
            pendingAction = null;
            addLog("Action annulee.");
        }
    }

    public void passTurn() {
        playerATB = 0;
        state = BattleState.WAITING;
    }

    // ==========================================
    // LOGIQUE ENNEMI (IA)
    // ==========================================

    private void startEnemyTurn() {
        state = BattleState.ENEMY_TURN;
        hasMoved = false;
        turnTimer = 0;
        pendingAction = null;
        enemy.restorePA(2);
    }

    private void updateEnemyTurn(float delta) {
        turnTimer += delta;
        if (turnTimer < 0.8f) return;

        if (!hasMoved) {
            if (pendingAction == null) {
                pendingAction = enemy.chooseAction();
            }

            if (pendingAction == null) {
                addLog(enemy.getClass().getSimpleName() + " passe son tour.");
                finishEnemyTurn();
                return;
            }

            float requiredRange = pendingAction.getRange();
            float currentDist = Vector2.dst(player.get_positionX(), player.get_positionY(), enemy.get_positionX(), enemy.get_positionY());

            if (currentDist <= requiredRange) {
                hasMoved = true;
                turnTimer = 0;
            } else {
                Vector2 direction = new Vector2(player.get_positionX() - enemy.get_positionX(), player.get_positionY() - enemy.get_positionY()).nor();
                float moveAmount = Math.min(currentDist - (requiredRange - 0.5f), enemy.getDEP());
                enemy.set_position(enemy.get_positionX() + direction.x * moveAmount, enemy.get_positionY() + direction.y * moveAmount);

                addLog(enemy.getClass().getSimpleName() + " s'approche...");
                hasMoved = true;
                turnTimer = 0;
            }
        }
        else {
            float dist = Vector2.dst(player.get_positionX(), player.get_positionY(), enemy.get_positionX(), enemy.get_positionY());
            if (dist <= pendingAction.getRange()) {
                pendingAction.execute(enemy, player);
            } else {
                addLog(enemy.getClass().getSimpleName() + " rate son attaque !");
            }
            finishEnemyTurn();
        }
    }

    private void finishEnemyTurn() {
        enemyATB = 0;
        state = BattleState.WAITING;
    }

    // GETTERS
    public BattleState getState() { return state; }
    public Enemy getEnemy() { return enemy; }
    public float getPlayerATB() { return playerATB; }
    public float getEnemyATB() { return enemyATB; }
    public boolean hasMoved() { return hasMoved; }
    public BattleAction getPendingAction() { return pendingAction; }
}
