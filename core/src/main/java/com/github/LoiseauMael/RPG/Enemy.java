package com.github.LoiseauMael.RPG;

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

public abstract class Enemy extends Fighter implements Disposable {

    // --- NOUVEAU : ETATS DE L'ENNEMI ---
    public enum EnemyState {
        ROAMING,       // Se balade sur la carte
        COMBAT_IDLE    // En combat (attend les ordres du BattleSystem)
    }

    protected EnemyState currentState = EnemyState.ROAMING;
    // ------------------------------------

    protected Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    protected float stateTime = 0;
    protected static final int FRAME_COLS = 3;
    protected static final int FRAME_ROWS = 4;

    protected Vector2 spawnPoint;
    protected float wanderRange = 3.0f;
    protected float wanderTimer = 0;
    protected boolean isWaiting = true;
    protected float moveSpeed = 1.5f;

    protected static class EnemyMove {
        public BattleAction action;
        public int weight;
        public EnemyMove(BattleAction action, int weight) { this.action = action; this.weight = weight; }
    }
    protected Array<EnemyMove> availableMoves;

    public Enemy(float x, float y, int level, int exp, int PV, int PM, int PA,
                 int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, String spriteName) {
        super(x, y, level, exp, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, null);

        Texture tex = new Texture(Gdx.files.internal(spriteName));
        this.sprite = new Sprite(tex);
        this.sprite.setSize(1f, 1f);

        this.spawnPoint = new Vector2(x + 0.5f, y);
        this.nom = spriteName.replace(".png", "");

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

    protected abstract void setupMoves();

    public BattleAction chooseAction() {
        if (availableMoves.size == 0) return null;

        Array<EnemyMove> validMoves = new Array<>();
        int totalWeight = 0;
        for (EnemyMove move : availableMoves) {
            if (move.action.canExecute(this)) {
                validMoves.add(move);
                totalWeight += move.weight;
            }
        }
        if (validMoves.size == 0) return availableMoves.first().action;

        int r = MathUtils.random(0, totalWeight - 1);
        int c = 0;
        for (EnemyMove move : validMoves) {
            c += move.weight;
            if (r < c) return move.action;
        }
        return validMoves.first().action;
    }

    // --- CHANGEMENT D'ETAT ---
    public void setInCombat(boolean inCombat) {
        if (inCombat) {
            this.currentState = EnemyState.COMBAT_IDLE;
            // Arrêt immédiat et forcé
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
                // En combat, on s'assure que la vélocité reste à 0
                // Le déplacement est géré par téléportation (BattleSystem) ou cinématique dédiée
                this.velocityX = 0;
                this.velocityY = 0;
                break;
        }

        super.update(delta); // Applique la physique (Entity)
        updateAnimation(delta);
    }

    private void updateRoamingAI(float delta) {
        wanderTimer -= delta;
        if (isWaiting) {
            if (wanderTimer <= 0) {
                isWaiting = false;
                wanderTimer = MathUtils.random(1f, 3f);
                float angle = MathUtils.random(0, 360) * MathUtils.degreesToRadians;
                velocityX = MathUtils.cos(angle) * moveSpeed;
                velocityY = MathUtils.sin(angle) * moveSpeed;
            } else {
                velocityX = 0; velocityY = 0;
            }
        } else {
            if (wanderTimer <= 0 || Vector2.dst(positionX, positionY, spawnPoint.x, spawnPoint.y) > wanderRange) {
                isWaiting = true;
                wanderTimer = MathUtils.random(2f, 5f);
                velocityX = 0; velocityY = 0;
            }
        }
    }

    private void updateAnimation(float delta) {
        stateTime += delta;
        boolean isMoving = (velocityX != 0 || velocityY != 0);

        // Si immobile, on garde la direction actuelle (gérée par lookAt en combat)
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
        // Met à jour l'image immédiatement (utile pour l'orientation instantanée)
        updateAnimation(0);
    }

    @Override
    public void dispose() {
        if (sprite != null && sprite.getTexture() != null) sprite.getTexture().dispose();
    }
}
