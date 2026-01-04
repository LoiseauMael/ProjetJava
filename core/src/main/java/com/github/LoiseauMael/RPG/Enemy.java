package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Enemy extends Fighter {

    private final float spawnX;
    private final float spawnY;
    private final float wanderRadius;

    private float aiTimer;
    private float moveDuration;
    private boolean isMoving;
    private final float moveSpeed = 2f;

    private final Animation<TextureRegion> walkUp;
    private final Animation<TextureRegion> walkDown;
    private final Animation<TextureRegion> walkLeft;
    private final Animation<TextureRegion> walkRight;

    private TextureRegion idleFront;
    private float stateTime;

    private final Texture texture;

    // --- NOUVEAU : Récompenses ---
    private int expReward;
    private int moneyReward;

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final float UNIT_SCALE_CONVERSION = 1f / 16f;

    private Enemy(float positionX, float positionY,
                  int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP,
                  int level, int expReward, int moneyReward, // Nouveaux paramètres
                  Sprite sprite, Texture texture, float wanderRadius) {

        super(positionX, positionY, 0, 0, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, sprite);

        this.spawnX = positionX;
        this.spawnY = positionY;
        this.wanderRadius = wanderRadius;
        this.texture = texture;

        // Initialisation RPG
        this.level = level; // Assurez-vous que 'level' est bien dans Fighter, sinon ajoutez-le ici
        this.expReward = expReward;
        this.moneyReward = moneyReward;

        TextureRegion[][] tmp = TextureRegion.split(this.texture, FRAME_WIDTH, FRAME_HEIGHT);

        // Animations
        Array<TextureRegion> downFrames = new Array<>();
        if (tmp.length > 0) for (int i = 0; i < tmp[0].length; i++) downFrames.add(tmp[0][i]);
        this.walkDown = new Animation<>(0.1f, downFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> leftFrames = new Array<>();
        if (tmp.length > 1) for (int i = 0; i < tmp[1].length; i++) leftFrames.add(tmp[1][i]);
        this.walkLeft = new Animation<>(0.1f, leftFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> rightFrames = new Array<>();
        if (tmp.length > 2) for (int i = 0; i < tmp[2].length; i++) rightFrames.add(tmp[2][i]);
        this.walkRight = new Animation<>(0.1f, rightFrames, Animation.PlayMode.LOOP);

        Array<TextureRegion> upFrames = new Array<>();
        if (tmp.length > 3) for (int i = 0; i < tmp[3].length; i++) upFrames.add(tmp[3][i]);
        this.walkUp = new Animation<>(0.1f, upFrames, Animation.PlayMode.LOOP);

        this.idleFront = (tmp.length > 0) ? tmp[0][0] : null;
        if (this.idleFront != null) {
            this.getSprite().setRegion(this.idleFront);
        }

        pickNewState();
    }

    public static Enemy create(float x, float y, float radius, int level, String texturePath) {
        Texture texture = new Texture(Gdx.files.internal(texturePath));
        Sprite sprite = new Sprite(texture);
        sprite.setSize(FRAME_WIDTH * UNIT_SCALE_CONVERSION, FRAME_HEIGHT * UNIT_SCALE_CONVERSION);

        // Calcul des stats basées sur le niveau
        int pv = 20 + (level * 5);
        int atk = 3 + level;
        int def = 1 + (level / 2);

        // Calcul des récompenses basées sur le niveau
        int xp = 20 + (level * 10);
        int money = 5 + (level * 2);

        return new Enemy(x, y,
            pv, 0, 10, atk, def, 0, 0, 4, 3, // Stats de combat
            level, xp, money,               // RPG Stats
            sprite, texture, radius);
    }

    // --- LES GETTERS MANQUANTS ---
    public int getExpReward() { return expReward; }
    public int getMoneyReward() { return moneyReward; }

    public void update(float delta) {
        stateTime += delta;
        aiTimer += delta;

        if (aiTimer >= moveDuration) {
            pickNewState();
            aiTimer = 0;
        }

        if (isMoving) {
            move(get_velocityX() * delta, get_velocityY() * delta);
        }

        updateAnimation(delta);
    }

    private void updateAnimation(float delta) {
        float vx = get_velocityX();
        float vy = get_velocityY();

        if (!isMoving || (vx == 0 && vy == 0)) return;

        Animation<TextureRegion> currentAnim = null;
        if (Math.abs(vx) > Math.abs(vy)) {
            currentAnim = (vx > 0) ? walkRight : walkLeft;
        } else {
            currentAnim = (vy > 0) ? walkUp : walkDown;
        }

        if (currentAnim != null) {
            this.getSprite().setRegion(currentAnim.getKeyFrame(stateTime, true));
        }
    }

    private void pickNewState() {
        if (MathUtils.randomBoolean()) {
            isMoving = true;
            moveDuration = MathUtils.random(0.5f, 2.0f);

            float distX = get_positionX() - spawnX;
            float distY = get_positionY() - spawnY;

            boolean needFixX = distX > wanderRadius || distX < -wanderRadius;
            boolean needFixY = distY > wanderRadius || distY < -wanderRadius;

            if (needFixX && needFixY) {
                if (MathUtils.randomBoolean()) needFixY = false;
                else needFixX = false;
            }

            if (needFixX) {
                float dir = (distX > 0) ? -1 : 1;
                set_velocityX(dir * moveSpeed);
                set_velocityY(0);
            } else if (needFixY) {
                float dir = (distY > 0) ? -1 : 1;
                set_velocityY(dir * moveSpeed);
                set_velocityX(0);
            } else {
                if (MathUtils.randomBoolean()) {
                    set_velocityX(MathUtils.randomSign() * moveSpeed);
                    set_velocityY(0);
                } else {
                    set_velocityX(0);
                    set_velocityY(MathUtils.randomSign() * moveSpeed);
                }
            }

        } else {
            isMoving = false;
            moveDuration = MathUtils.random(1.0f, 3.0f);
            set_velocityX(0);
            set_velocityY(0);
        }
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
