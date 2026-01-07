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

    // --- ANIMATION ---
    private Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    private float stateTime;
    private static final int FRAME_COLS = 3;
    private static final int FRAME_ROWS = 4;

    // --- IA VAGABONDAGE ---
    private Vector2 spawnPosition;
    private Vector2 targetPosition;
    private float waitTimer;
    private boolean isMoving;
    private float wanderRadius = 3.0f;

    // --- COMBAT ---
    protected static class EnemyMove {
        public BattleAction action;
        public int weight;
        public EnemyMove(BattleAction action, int weight) { this.action = action; this.weight = weight; }
    }
    protected Array<EnemyMove> availableMoves;

    /**
     * Méthode STATIQUE pour créer le sprite AVANT d'appeler le constructeur parent.
     * Cela évite le NullPointerException dans Entity.java.
     */
    private static Sprite createBaseSprite(String texturePath) {
        Texture texture = new Texture(Gdx.files.internal(texturePath));
        Sprite s = new Sprite(texture);
        // Ajustement de la taille (32px -> 2 unités)
        s.setSize(s.getWidth() / 3f / 16f, s.getHeight() / 4f / 16f);
        return s;
    }

    // Constructeur
    protected Enemy(float x, float y, int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, String texturePath) {
        // On passe le sprite créé par la méthode statique directement au parent
        super(x, y, 0, 0, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, createBaseSprite(texturePath));

        this.availableMoves = new Array<>();
        this.spawnPosition = new Vector2(x, y);
        this.targetPosition = new Vector2(x, y);

        // Maintenant que le parent est initialisé, 'this.sprite' existe.
        // On peut initialiser les animations.
        initAnimations();

        // Initialisation de l'IA
        setupMoves();
    }

    private void initAnimations() {
        // On récupère la texture depuis le sprite (qui a été créé dans createBaseSprite)
        Texture texture = this.sprite.getTexture();

        // Découpe
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / FRAME_COLS, texture.getHeight() / FRAME_ROWS);
        walkDown = new Animation<>(0.2f, tmp[0]);
        walkLeft = new Animation<>(0.2f, tmp[1]);
        walkRight = new Animation<>(0.2f, tmp[2]);
        walkUp = new Animation<>(0.2f, tmp[3]);
    }

    protected void initStats(int targetLevel, int bPV, int bPM, int bPA, int bFOR, int bDEF, int bFORM, int bDEFM, int bVIT, int bDEP) {
        // 1. Stats de base
        this.level = 1;
        this.maxPV = bPV; this.PV = bPV;
        this.maxPM = bPM; this.PM = bPM;
        this.maxPA = bPA; this.PA = bPA;
        this.FOR = bFOR;
        this.DEF = bDEF;
        this.FORM = bFORM;
        this.DEFM = bDEFM;
        this.VIT = bVIT;
        this.DEP = bDEP;

        // 2. Montée de niveau simulée
        for (int i = 1; i < targetLevel; i++) {
            this.level++;
            this.maxPV += Math.max(1, (int)(this.maxPV * 0.1f)); this.PV = this.maxPV;
            this.maxPM += Math.max(1, (int)(this.maxPM * 0.1f)); this.PM = this.maxPM;
            this.FOR  += Math.max(1, (int)(this.FOR * 0.1f));
            this.DEF  += Math.max(1, (int)(this.DEF * 0.1f));
            this.FORM += Math.max(1, (int)(this.FORM * 0.1f));
            this.DEFM += Math.max(1, (int)(this.DEFM * 0.1f));
            this.VIT  += Math.max(1, (int)(this.VIT * 0.1f));
        }
    }

    protected abstract void setupMoves();

    @Override
    public void update(float delta) {
        stateTime += delta;

        if (isMoving) {
            float speed = 2.0f * delta;
            Vector2 position = new Vector2(get_positionX(), get_positionY());
            Vector2 direction = new Vector2(targetPosition).sub(position).nor();
            float distance = position.dst(targetPosition);

            if (distance <= speed) {
                set_position(targetPosition.x, targetPosition.y);
                isMoving = false;
                waitTimer = MathUtils.random(1.0f, 3.0f);
            } else {
                set_position(position.x + direction.x * speed, position.y + direction.y * speed);

                if (Math.abs(direction.x) > Math.abs(direction.y)) {
                    this.getSprite().setRegion(direction.x > 0 ? walkRight.getKeyFrame(stateTime, true) : walkLeft.getKeyFrame(stateTime, true));
                } else {
                    this.getSprite().setRegion(direction.y > 0 ? walkUp.getKeyFrame(stateTime, true) : walkDown.getKeyFrame(stateTime, true));
                }
            }
        } else {
            waitTimer -= delta;
            if (waitTimer <= 0) {
                float randomAngle = MathUtils.random(0f, 360f);
                float randomDist = MathUtils.random(0f, wanderRadius);
                targetPosition.set(
                    spawnPosition.x + MathUtils.cosDeg(randomAngle) * randomDist,
                    spawnPosition.y + MathUtils.sinDeg(randomAngle) * randomDist
                );
                isMoving = true;
            } else {
                TextureRegion[] frames = walkDown.getKeyFrames();
                if (frames.length > 1) this.getSprite().setRegion(frames[1]);
            }
        }
    }

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
        int randomValue = MathUtils.random(0, totalWeight - 1);
        int currentSum = 0;
        for (EnemyMove move : validMoves) {
            currentSum += move.weight;
            if (randomValue < currentSum) return move.action;
        }
        return validMoves.first().action;
    }

    // Compatibilité
    public static Enemy create(float x, float y, int type, String texturePath) {
        switch (type) {
            case 2: return new BossEnemy(x, y, texturePath);
            case 1: return new EliteEnemy(x, y, texturePath);
            case 0: default: return new NormalEnemy(x, y, texturePath);
        }
    }

    @Override
    public void dispose() {
        if (this.getSprite() != null && this.getSprite().getTexture() != null) {
            this.getSprite().getTexture().dispose();
        }
    }
}
