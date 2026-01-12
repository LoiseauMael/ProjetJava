package com.github.LoiseauMael.RPG.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.model.entities.Enemy;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Gère la logique et le déroulement des combats (Tour par tour dynamique / ATB).
 * <p>
 * Responsabilités :
 * <ul>
 * <li>Gestion des états du combat (Tour du joueur, Tour ennemi, Victoire, Défaite).</li>
 * <li>Gestion de la barre d'initiative (ATB) basée sur la vitesse (VIT).</li>
 * <li>Gestion de l'interface utilisateur spécifique au combat (Logs).</li>
 * <li>Attribution des récompenses (XP, Or) en fin de combat.</li>
 * </ul>
 */
public class BattleSystem {

    /**
     * Enumération des états possibles du combat.
     */
    public enum BattleState {
        /** En attente de remplissage des barres ATB. */
        WAITING,
        /** Le joueur doit choisir une action (Menu). */
        PLAYER_TURN,
        /** Le joueur a choisi de bouger et doit sélectionner une case. */
        PLAYER_MOVING,
        /** Le joueur a choisi une compétence et doit sélectionner une cible. */
        PLAYER_SELECTING_TARGET,
        /** L'IA de l'ennemi est en train de jouer. */
        ENEMY_TURN,
        /** L'ennemi est vaincu. */
        VICTORY,
        /** Le joueur est vaincu. */
        GAME_OVER
    }

    private Main game;
    private Player player;
    private Enemy enemy;
    private BattleState state;

    private float playerATB;
    private float enemyATB;

    private boolean hasMoved;
    private BattleAction pendingAction;
    private float turnTimer;
    private boolean rewardsGiven = false;

    // UI Elements
    private static Array<String> combatLogs = new Array<>();
    private ScrollPane logScroll;
    private Table logTable;
    private Skin skin;

    /**
     * Initialise un nouveau combat.
     *
     * @param game Instance principale du jeu (pour l'accès aux états globaux).
     * @param player Le personnage du joueur.
     * @param enemy L'adversaire rencontré.
     */
    public BattleSystem(Main game, Player player, Enemy enemy) {
        this.game = game;
        this.player = player;
        this.enemy = enemy;
        this.state = BattleState.WAITING;
        this.rewardsGiven = false;

        initUI();

        combatLogs.clear();
        addLog("Combat commencé contre " + enemy.getName() + " !");

        // Alignement initial sur la grille
        player.snapToGrid();
        enemy.snapToGrid();
    }

    private void initUI() {
        skin = new Skin();

        // Ajout police et styles
        skin.add("default", new BitmapFont());

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Style nécessaire pour le ScrollPane
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollStyle);

        logTable = new Table();
        logTable.top().left();

        logScroll = new ScrollPane(logTable, skin);
        logScroll.setFadeScrollBars(false);
    }

    /**
     * Ajoute un message dans le journal de combat visible à l'écran.
     * @param message Le texte à afficher.
     */
    public static void addLog(String message) {
        Gdx.app.log("COMBAT", message);
        combatLogs.add("> " + message);
        if (combatLogs.size > 20) combatLogs.removeIndex(0);
    }

    private void updateLogUI() {
        if (logTable == null) return;
        logTable.clear();
        for (String log : combatLogs) {
            Label l = new Label(log, skin);
            logTable.add(l).left().row();
        }
        if (logScroll != null) {
            logScroll.layout();
            logScroll.setScrollPercentY(100);
        }
    }

    public ScrollPane getLogScroll() {
        return logScroll;
    }

    /**
     * Met à jour la logique du combat à chaque frame.
     * Gère la progression de l'ATB et les transitions d'état.
     *
     * @param delta Temps écoulé depuis la dernière frame.
     */
    public void update(float delta) {
        updateLogUI();

        if (player.getPV() <= 0) {
            state = BattleState.GAME_OVER;
            return;
        }

        // Détection de la victoire
        if (enemy.getPV() <= 0) {
            state = BattleState.VICTORY;
            if (!rewardsGiven) {
                giveRewards();
            }
            return;
        }

        // Orientation automatique
        if (state != BattleState.PLAYER_MOVING && state != BattleState.GAME_OVER) {
            player.lookAt(enemy);
        }
        if (state != BattleState.ENEMY_TURN && state != BattleState.VICTORY) {
            enemy.lookAt(player);
        }

        // Système ATB
        if (state == BattleState.WAITING) {
            playerATB += player.getVIT() * delta * 5.0f;
            enemyATB += enemy.getVIT() * delta * 5.0f;

            if (playerATB >= 100) { playerATB = 100; startPlayerTurn(); }
            else if (enemyATB >= 100) { enemyATB = 100; startEnemyTurn(); }
        } else if (state == BattleState.ENEMY_TURN) {
            updateEnemyTurn(delta);
        }
    }

    // --- GESTION DES RÉCOMPENSES ---
    private void giveRewards() {
        rewardsGiven = true;
        addLog("Victoire !");

        // 1. Calcul EXP
        int monsterLevel = enemy.getLevel();
        int playerLevel = player.getLevel();
        int baseExp = 20 * monsterLevel;

        // Malus si le joueur est trop haut niveau
        float multiplier = 1.0f;
        if (playerLevel > monsterLevel) {
            multiplier = Math.max(0.1f, 1.0f - (playerLevel - monsterLevel) * 0.2f);
        }

        int finalExp = (int) (baseExp * multiplier);
        if (finalExp < 1) finalExp = 1;

        // Gain d'XP via la méthode qui gère le Level UP
        int oldLevel = player.getLevel();
        player.gainExp(finalExp);

        addLog("Gagné " + finalExp + " EXP.");
        if (player.getLevel() > oldLevel) {
            addLog("NIVEAU SUPÉRIEUR ! (" + player.getLevel() + ")");
        }

        // 2. Calcul Or
        int goldReward = 10 * monsterLevel;
        player.addMoney(goldReward);
        addLog("Gagné " + goldReward + " Or.");
        addLog("(Cliquez pour quitter)");

        // 3. Suppression définitive de l'ennemi
        if (game != null) {
            game.deadEnemyIds.add(enemy.getId());
            game.entities.removeValue(enemy, true);
        }
    }

    private void startPlayerTurn() {
        state = BattleState.PLAYER_TURN;
        hasMoved = false;
        pendingAction = null;
        player.regenPA(2);
        addLog("A vous !");
    }

    // --- INTERACTION JOUEUR ---

    /**
     * Annule l'action ou le déplacement en cours de sélection.
     * Permet au joueur de revenir au menu principal du combat.
     */
    public void cancelSelection() {
        if (state == BattleState.PLAYER_MOVING || state == BattleState.PLAYER_SELECTING_TARGET) {
            state = BattleState.PLAYER_TURN;
            pendingAction = null;
        }
    }

    /**
     * Active le mode déplacement pour le joueur si celui-ci n'a pas encore bougé ce tour-ci.
     */
    public void enableMovePhase() {
        // Si on visait une attaque, on annule la visée pour passer en mode mouvement
        if (state == BattleState.PLAYER_SELECTING_TARGET) {
            cancelSelection();
        }

        if (state == BattleState.PLAYER_TURN && !hasMoved) {
            state = BattleState.PLAYER_MOVING;
            addLog("Déplacez-vous.");
        } else if (hasMoved) {
            addLog("Déjà bougé !");
        }
    }

    /**
     * Passe le tour du joueur volontairement.
     */
    public void playerPassTurn() {
        if (state == BattleState.PLAYER_TURN || state == BattleState.PLAYER_SELECTING_TARGET || state == BattleState.PLAYER_MOVING) {
            addLog("Vous passez votre tour.");
            passTurn();
        }
    }

    /**
     * Gère les clics sur la grille de combat.
     *
     * @param x Coordonnée X de la case cliquée (monde).
     * @param y Coordonnée Y de la case cliquée (monde).
     */
    public void handleGridClick(float x, float y) {
        // Clic pour quitter l'écran de victoire
        if (state == BattleState.VICTORY) {
            game.changeState(game.explorationState);
            return;
        }

        int tileX = (int)x;
        int tileY = (int)y;

        if (state == BattleState.PLAYER_MOVING) tryMovePlayerTo(tileX, tileY);
        else if (state == BattleState.PLAYER_SELECTING_TARGET) tryAttackTarget(tileX, tileY);
    }

    /**
     * Tente de déplacer le joueur vers la case cible.
     */
    public void tryMovePlayerTo(int targetX, int targetY) {
        int dist = Math.abs(targetX - player.getTileX()) + Math.abs(targetY - player.getTileY());
        if (dist <= player.getDEP()) {
            player.setGridPosition(targetX, targetY);
            player.snapToGrid();
            hasMoved = true;
            state = BattleState.PLAYER_TURN;
            addLog("Déplacement.");
        } else addLog("Trop loin !");
    }

    public Array<Vector2> getValidMoveTiles() {
        Array<Vector2> t = new Array<>();
        if (state != BattleState.PLAYER_MOVING) return t;
        int r = player.getDEP();
        for(int x=player.getTileX()-r; x<=player.getTileX()+r; x++)
            for(int y=player.getTileY()-r; y<=player.getTileY()+r; y++)
                if(Math.abs(x-player.getTileX())+Math.abs(y-player.getTileY())<=r) t.add(new Vector2(x,y));
        return t;
    }

    /**
     * Prépare une action (sort/attaque) et passe en mode sélection de cible.
     * @param action L'action à effectuer.
     */
    public void startTargetSelection(BattleAction action) {
        // Si on était en train de bouger ou de viser un autre sort, on reset pour prendre la nouvelle action
        if (state == BattleState.PLAYER_MOVING || state == BattleState.PLAYER_SELECTING_TARGET) {
            cancelSelection();
        }

        if (state == BattleState.PLAYER_TURN) {
            if (action.canExecute(player)) {
                this.pendingAction = action;
                state = BattleState.PLAYER_SELECTING_TARGET;
                addLog("Cible ?");
            } else addLog("Pas assez de ressources !");
        }
    }

    public Array<Vector2> getValidAttackTiles() {
        Array<Vector2> t = new Array<>();
        if (state != BattleState.PLAYER_SELECTING_TARGET || pendingAction == null) return t;
        int r = (int) pendingAction.getRange();
        for(int x=player.getTileX()-r; x<=player.getTileX()+r; x++)
            for(int y=player.getTileY()-r; y<=player.getTileY()+r; y++)
                if(Math.abs(x-player.getTileX())+Math.abs(y-player.getTileY())<=r) t.add(new Vector2(x,y));
        return t;
    }

    /**
     * Tente d'exécuter l'action en attente sur la case ciblée.
     */
    public void tryAttackTarget(int tx, int ty) {
        if (enemy.getTileX() == tx && enemy.getTileY() == ty) {
            int dist = Math.abs(player.getTileX() - enemy.getTileX()) + Math.abs(player.getTileY() - enemy.getTileY());
            if (dist <= pendingAction.getRange()) {
                pendingAction.execute(player, enemy);
                passTurn();
            } else addLog("Hors de portée !");
        } else addLog("Cible invalide !");
    }

    public void passTurn() {
        playerATB = 0;
        state = BattleState.WAITING;
    }

    // --- IA ENNEMIE ---

    private void startEnemyTurn() {
        state = BattleState.ENEMY_TURN;
        hasMoved = false;
        turnTimer = 0;
        pendingAction = null;
        enemy.regenPA(2);
    }

    private void updateEnemyTurn(float delta) {
        turnTimer += delta;
        if (turnTimer < 0.8f) return;

        if (pendingAction == null) {
            pendingAction = enemy.chooseAction();
            if (pendingAction == null) { finishEnemyTurn(); return; }
        }

        int dist = Math.abs(player.getTileX() - enemy.getTileX()) + Math.abs(player.getTileY() - enemy.getTileY());
        boolean inRange = dist <= pendingAction.getRange();

        if (!inRange && !hasMoved) {
            // IA de déplacement simple vers le joueur
            int moves = enemy.getDEP();
            int cx = enemy.getTileX(), cy = enemy.getTileY();
            int tx = player.getTileX(), ty = player.getTileY();

            while (moves > 0) {
                int nx = cx, ny = cy;
                if (Math.abs(tx - cx) > Math.abs(ty - cy)) { if (cx < tx) nx++; else nx--; }
                else { if (cy < ty) ny++; else ny--; }

                cx = nx; cy = ny;
                moves--;
                if (Math.abs(tx - cx) + Math.abs(ty - cy) <= pendingAction.getRange()) break;
            }

            enemy.setGridPosition(cx, cy);
            enemy.snapToGrid();
            hasMoved = true;
            addLog(enemy.getName() + " se rapproche.");
            turnTimer = 0;
            return;
        }

        dist = Math.abs(player.getTileX() - enemy.getTileX()) + Math.abs(player.getTileY() - enemy.getTileY());
        if (dist <= pendingAction.getRange() && pendingAction.canExecute(enemy)) {
            addLog(enemy.getName() + " lance " + pendingAction.getName());
            pendingAction.execute(enemy, player);
        }

        finishEnemyTurn();
    }

    private void finishEnemyTurn() {
        enemyATB = 0;
        state = BattleState.WAITING;
    }

    public BattleState getState() { return state; }
    public float getPlayerATB() { return playerATB; }
    public float getEnemyATB() { return enemyATB; }
}
