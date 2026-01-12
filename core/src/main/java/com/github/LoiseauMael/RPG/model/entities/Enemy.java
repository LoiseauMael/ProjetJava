package com.github.LoiseauMael.RPG.model.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.LoiseauMael.RPG.battle.BattleAction;

/**
 * Classe abstraite définissant le comportement commun à tous les ennemis.
 * <p>
 * Responsabilités :
 * <ul>
 * <li><b>IA de Monde (Overworld) :</b> Gère les états de patrouille (ROAMING) et d'attente.</li>
 * <li><b>IA de Combat :</b> Gère la sélection pondérée des actions (attaques, sorts) via {@link EnemyMove}.</li>
 * <li><b>Animation :</b> Gère les sprites de déplacement automatique.</li>
 * </ul>
 */
public abstract class Enemy extends Fighter implements Disposable {

    /**
     * États possibles de l'IA de l'ennemi.
     */
    public enum EnemyState {
        /** L'ennemi se déplace librement sur la carte (patrouille aléatoire). */
        ROAMING,
        /** L'ennemi est engagé dans un combat (figé sur la carte, attend le BattleSystem). */
        COMBAT_IDLE
    }

    protected EnemyState currentState = EnemyState.ROAMING;

    // --- ANIMATIONS ---
    protected Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    protected float stateTime = 0;
    protected static final int FRAME_COLS = 3;
    protected static final int FRAME_ROWS = 4;

    // --- IA DE DÉPLACEMENT (ROAMING) ---
    /** Point d'apparition initial. L'ennemi essaiera de rester autour de ce point. */
    protected Vector2 spawnPoint;
    /** Rayon maximal d'éloignement par rapport au point d'apparition (en cases). */
    protected float wanderRange = 3.0f;
    protected float wanderTimer = 0;
    protected boolean isWaiting = true;
    protected float moveSpeed = 1.5f;

    // --- IA DE COMBAT ---
    /**
     * Structure interne reliant une action de combat à sa probabilité d'utilisation (poids).
     */
    protected static class EnemyMove {
        public BattleAction action;
        public int weight;

        /**
         * @param action L'action à exécuter (Attaque, Sort...).
         * @param weight Le poids (probabilité relative). Plus il est haut, plus l'action est fréquente.
         */
        public EnemyMove(BattleAction action, int weight) { this.action = action; this.weight = weight; }
    }

    /** Liste des coups disponibles pour cet ennemi. */
    protected Array<EnemyMove> availableMoves;

    /**
     * Constructeur de base pour un ennemi.
     * Charge la texture spécifiée et initialise l'IA de patrouille.
     *
     * @param x Position X initiale.
     * @param y Position Y initiale.
     * @param level Niveau.
     * @param exp XP donnée à la mort.
     * @param PV Points de vie.
     * @param PM Points de mana.
     * @param PA Points d'action.
     * @param FOR Force.
     * @param DEF Défense.
     * @param FORM Force Magique.
     * @param DEFM Défense Magique.
     * @param VIT Vitesse.
     * @param DEP Déplacement.
     * @param spriteName Chemin du fichier texture (ex: "goblin.png").
     */
    public Enemy(float x, float y, int level, int exp, int PV, int PM, int PA,
                 int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, String spriteName) {
        super(x, y, level, exp, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, null);

        Texture tex = new Texture(Gdx.files.internal(spriteName));
        this.sprite = new Sprite(tex);
        this.sprite.setSize(1f, 1f); // Taille standard d'une case

        // Le point de spawn est centré sur la case pour les calculs de distance
        this.spawnPoint = new Vector2(x + 0.5f, y);
        this.nom = spriteName.replace(".png", ""); // Nom par défaut basé sur le fichier

        setCollisionBounds(0.6f, 0.4f, 0f, 0.1f);
        initAnimations(tex);

        this.availableMoves = new Array<>();
        setupMoves();
    }

    private void initAnimations(Texture texture) {
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / FRAME_COLS, texture.getHeight() / FRAME_ROWS);
        walkDown  = new Animation<>(0.2f, tmp[0]);
        walkLeft  = new Animation<>(0.2f, tmp[1]);
        walkRight = new Animation<>(0.2f, tmp[2]);
        walkUp    = new Animation<>(0.2f, tmp[3]);
    }

    /**
     * Méthode abstraite à implémenter par les sous-classes (ex: GenericEnemy).
     * Doit remplir la liste {@link #availableMoves} avec les attaques possibles.
     */
    protected abstract void setupMoves();

    /**
     * Sélectionne une action à effectuer pendant le tour de combat.
     * Utilise un algorithme de sélection aléatoire pondérée (Weighted Random).
     *
     * @return L'action choisie (AttackAction, SpellAction, etc.) ou null si aucune n'est valide.
     */
    public BattleAction chooseAction() {
        if (availableMoves.size == 0) return null;

        // 1. Filtrer les coups possibles (ex: assez de Mana ?)
        Array<EnemyMove> validMoves = new Array<>();
        int totalWeight = 0;
        for (EnemyMove move : availableMoves) {
            if (move.action.canExecute(this)) {
                validMoves.add(move);
                totalWeight += move.weight;
            }
        }

        // Sécurité : si aucun coup spécial n'est possible (ex: 0 PM), retourne le premier (souvent Attaque de base)
        if (validMoves.size == 0) return availableMoves.first().action;

        // 2. Tirage aléatoire
        int r = MathUtils.random(0, totalWeight - 1);
        int c = 0;
        for (EnemyMove move : validMoves) {
            c += move.weight;
            if (r < c) return move.action;
        }
        return validMoves.first().action;
    }

    // --- GESTION DES ÉTATS ---

    /**
     * Change l'état de l'ennemi.
     * Si passe en combat (true), l'ennemi s'arrête immédiatement.
     * @param inCombat true pour entrer en combat, false pour retourner en patrouille.
     */
    public void setInCombat(boolean inCombat) {
        if (inCombat) {
            this.currentState = EnemyState.COMBAT_IDLE;
            // Arrêt immédiat et forcé pour éviter le glissement
            this.velocityX = 0;
            this.velocityY = 0;
        } else {
            this.currentState = EnemyState.ROAMING;
        }
    }

    @Override
    public void update(float delta) {
        // --- LOGIQUE D'ETAT STRICTE ---
        switch (currentState) {
            case ROAMING:
                updateRoamingAI(delta);
                break;
            case COMBAT_IDLE:
                // En combat, on s'assure que la vélocité reste à 0.
                // Le déplacement tactique est géré par le BattleSystem (MoveAction).
                this.velocityX = 0;
                this.velocityY = 0;
                break;
        }

        super.update(delta); // Applique la physique (Entity)
        updateAnimation(delta);
    }

    /**
     * Logique de déplacement aléatoire sur la carte (Patrouille).
     * L'ennemi alterne entre "Attendre" et "Marcher dans une direction aléatoire".
     * Il reste dans un rayon {@code wanderRange} autour de son point d'apparition.
     */
    private void updateRoamingAI(float delta) {
        wanderTimer -= delta;

        if (isWaiting) {
            // État : ATTENTE
            if (wanderTimer <= 0) {
                // Fin de l'attente -> On bouge
                isWaiting = false;
                wanderTimer = MathUtils.random(1f, 3f); // Durée de marche

                // Choix d'un angle aléatoire
                float angle = MathUtils.random(0, 360) * MathUtils.degreesToRadians;
                velocityX = MathUtils.cos(angle) * moveSpeed;
                velocityY = MathUtils.sin(angle) * moveSpeed;
            } else {
                velocityX = 0; velocityY = 0;
            }
        } else {
            // État : MARCHE
            // S'arrête si le timer est fini OU s'il est trop loin du spawn
            if (wanderTimer <= 0 || Vector2.dst(positionX, positionY, spawnPoint.x, spawnPoint.y) > wanderRange) {
                isWaiting = true;
                wanderTimer = MathUtils.random(2f, 5f); // Durée d'attente
                velocityX = 0; velocityY = 0;
            }
        }
    }

    private void updateAnimation(float delta) {
        stateTime += delta;
        boolean isMoving = (velocityX != 0 || velocityY != 0);

        // Mise à jour de la direction seulement si on bouge (pour garder le regard fixe à l'arrêt)
        if (isMoving) {
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                currentDirection = (velocityX > 0) ? 2 : 1;
            } else {
                currentDirection = (velocityY > 0) ? 3 : 0;
            }
        }

        TextureRegion currentFrame;
        switch (currentDirection) {
            case 1: currentFrame = isMoving ? walkLeft.getKeyFrame(stateTime, true) : walkLeft.getKeyFrames()[1]; break;
            case 2: currentFrame = isMoving ? walkRight.getKeyFrame(stateTime, true) : walkRight.getKeyFrames()[1]; break;
            case 3: currentFrame = isMoving ? walkUp.getKeyFrame(stateTime, true) : walkUp.getKeyFrames()[1]; break;
            default: currentFrame = isMoving ? walkDown.getKeyFrame(stateTime, true) : walkDown.getKeyFrames()[1]; break;
        }
        if (currentFrame != null && sprite != null) sprite.setRegion(currentFrame);
    }

    @Override
    protected void updateSpriteRegion() {
        // Met à jour l'image immédiatement (utile pour l'orientation instantanée via lookAt)
        updateAnimation(0);
    }

    @Override
    public void dispose() {
        if (sprite != null && sprite.getTexture() != null) sprite.getTexture().dispose();
    }
}
