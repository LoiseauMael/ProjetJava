package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.LoiseauMael.RPG.items.Item; // Import nécessaire

public abstract class Player extends Fighter implements Disposable {

    private final Texture texture;

    // --- ARME & ATTAQUE ---
    protected Sprite weaponSprite;
    protected Texture weaponTexture;
    public boolean isAttacking = false;
    protected float attackTimer = 0f;

    private static final float ATTACK_DURATION = 0.25f;

    // 0=Bas, 1=Gauche, 2=Droite, 3=Haut
    private int currentDirection = 0;

    // Animations
    private final Animation<TextureRegion> walkUp;
    private final Animation<TextureRegion> walkDown;
    private final Animation<TextureRegion> walkLeft;
    private final Animation<TextureRegion> walkRight;
    private final TextureRegion idleFront;
    private final TextureRegion idleBack;
    private final TextureRegion idleLeft;
    private final TextureRegion idleRight;

    private Animation<TextureRegion> currentAnimation;
    private TextureRegion currentIdleFrame;
    private float stateTime;

    private static final float SPEED = 4f;
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;

    // --- INVENTAIRE ---
    private Array<Item> inventory;

    protected Player(float positionX, float positionY, float velocityX, float velocityY,
                     int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP,
                     Sprite sprite, Texture texture, String weaponPath) {

        super(positionX, positionY, velocityX, velocityY, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, sprite);

        this.texture = texture;

        // Initialisation de l'inventaire
        this.inventory = new Array<>();

        // --- CHARGEMENT ARME ---
        if (weaponPath != null) {
            this.weaponTexture = new Texture(Gdx.files.internal(weaponPath));
            this.weaponSprite = new Sprite(this.weaponTexture);

            float desiredHeight = 1.1f;
            float aspect = (float) weaponSprite.getWidth() / weaponSprite.getHeight();
            float desiredWidth = desiredHeight * aspect;
            this.weaponSprite.setSize(desiredWidth, desiredHeight);
            this.weaponSprite.setOrigin(desiredWidth / 2f, 0f);
        }

        // --- ANIMATIONS ---
        TextureRegion[][] tmp = TextureRegion.split(this.texture, FRAME_WIDTH, FRAME_HEIGHT);

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

        this.idleFront = tmp.length > 0 && tmp[0].length > 1 ? tmp[0][1] : tmp[0][0];
        this.idleLeft  = tmp.length > 1 && tmp[1].length > 1 ? tmp[1][1] : tmp[0][0];
        this.idleRight = tmp.length > 2 && tmp[2].length > 1 ? tmp[2][1] : tmp[0][0];
        this.idleBack  = tmp.length > 3 && tmp[3].length > 1 ? tmp[3][1] : tmp[0][0];

        currentAnimation = walkDown;
        currentIdleFrame = idleFront;
        this.getSprite().setRegion(currentIdleFrame);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        move(get_velocityX() * delta, get_velocityY() * delta);

        if (isAttacking) {
            attackTimer += delta;
            if (attackTimer >= ATTACK_DURATION) {
                isAttacking = false;
                attackTimer = 0;
            }
        }

        float vx = get_velocityX();
        float vy = get_velocityY();
        boolean isMoving = Math.abs(vx) > 0.01f || Math.abs(vy) > 0.01f;

        if (isMoving) {
            Animation<TextureRegion> nextAnimation;
            if (Math.abs(vx) > Math.abs(vy)) {
                if (vx > 0) {
                    nextAnimation = walkRight; currentIdleFrame = idleRight; currentDirection = 2;
                } else {
                    nextAnimation = walkLeft; currentIdleFrame = idleLeft; currentDirection = 1;
                }
            } else {
                if (vy > 0) {
                    nextAnimation = walkUp; currentIdleFrame = idleBack; currentDirection = 3;
                } else {
                    nextAnimation = walkDown; currentIdleFrame = idleFront; currentDirection = 0;
                }
            }

            if (currentAnimation != nextAnimation) {
                currentAnimation = nextAnimation;
                stateTime = 0f;
            }
            this.getSprite().setRegion(currentAnimation.getKeyFrame(stateTime, true));
        } else {
            currentAnimation = null;
            this.getSprite().setRegion(currentIdleFrame);
        }
    }

    public void handleInput() {
        set_velocityX(0);
        set_velocityY(0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) set_velocityY(SPEED);
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) set_velocityY(-SPEED);

        if (get_velocityY() == 0) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) set_velocityX(-SPEED);
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) set_velocityX(SPEED);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isAttacking) {
            performAttack();
        }
    }

    protected void performAttack() {
        isAttacking = true;
        attackTimer = 0f;
    }

    // ==========================================
    // GESTION XP / NIVEAUX / STATS
    // ==========================================

    public int getMaxExp() {
        return level * 100;
    }

    public void gainExp(int amount) {
        this.exp += amount;
        while (this.exp >= getMaxExp()) {
            this.exp -= getMaxExp();
            levelUp();
        }
    }

    private int getStatIncrease(int currentStatValue) {
        int increase = (int) (currentStatValue * 0.10f); // 10%
        return Math.max(1, increase);
    }

    private void levelUp() {
        this.level++;

        this.maxPV += getStatIncrease(this.maxPV);
        this.PV = this.maxPV;

        this.maxPM += getStatIncrease(this.maxPM);
        this.PM = this.maxPM;

        this.FOR += getStatIncrease(this.FOR);
        this.DEF += getStatIncrease(this.DEF);
        this.FORM += getStatIncrease(this.FORM);
        this.DEFM += getStatIncrease(this.DEFM);
        this.VIT += getStatIncrease(this.VIT);

        if (this.maxPA > 0) {
            this.maxPA += getStatIncrease(this.maxPA);
            this.PA = this.maxPA;
        }

        Gdx.app.log("Player", "NIVEAU UP ! Niveau " + level + " atteint ! Stats +10%.");
    }

    // ==========================================
    // GESTION INVENTAIRE
    // ==========================================

    public void addItem(Item newItem) {
        for (Item item : inventory) {
            if (item.getName().equals(newItem.getName())) {
                item.addCount(1);
                return;
            }
        }
        inventory.add(newItem);
    }

    public void consumeItem(Item item) {
        item.use(this);
        if (item.getCount() <= 0) {
            inventory.removeValue(item, true);
        }
    }

    public Array<Item> getInventory() {
        return inventory;
    }

    // ==========================================
    // RENDU & HELPERS
    // ==========================================

    @Override
    public void draw(SpriteBatch batch) {
        boolean weaponBehind = (currentDirection == 3);

        if (weaponBehind) drawWeapon(batch);
        super.draw(batch);
        if (!weaponBehind) drawWeapon(batch);
    }

    private void drawWeapon(SpriteBatch batch) {
        if (isAttacking && weaponSprite != null) {
            float cx = get_positionX() + 0.5f;
            float cy = get_positionY() + 0.4f;
            float rotation = 0;
            float handOffsetX = 0;
            float handOffsetY = 0;
            boolean flip = false;

            float progress = attackTimer / ATTACK_DURATION;

            if (currentDirection == 0) { // BAS
                handOffsetX = -0.2f; handOffsetY = -0.3f;
                rotation = 225 + (-90 * progress);
            } else if (currentDirection == 3) { // HAUT
                handOffsetX = 0.2f; handOffsetY = 0.1f;
                rotation = 45 + (90 * progress);
            } else {
                float rightBaseAngle = -45;
                float rightSweep = -90;
                float rightHandX = 0.3f;
                float rightHandY = -0.2f;

                if (currentDirection == 2) { // DROITE
                    rotation = rightBaseAngle + (rightSweep * progress);
                    handOffsetX = rightHandX;
                    handOffsetY = rightHandY;
                } else { // GAUCHE
                    rotation = -(rightBaseAngle + (rightSweep * progress));
                    handOffsetX = -rightHandX;
                    handOffsetY = rightHandY;
                    flip = true;
                }
            }

            weaponSprite.setFlip(flip, false);
            weaponSprite.setPosition(
                (cx + handOffsetX) - weaponSprite.getOriginX(),
                (cy + handOffsetY) - weaponSprite.getOriginY()
            );
            weaponSprite.setRotation(rotation);
            weaponSprite.draw(batch);
        }
    }

    @Override
    public void dispose() {
        if (this.texture != null) this.texture.dispose();
        if (this.weaponTexture != null) this.weaponTexture.dispose();
    }

    public int getDirection() {
        return currentDirection;
    }

    public void setDirection(int direction) {
        this.currentDirection = direction;
        switch (direction) {
            case 1: this.currentIdleFrame = idleLeft; break;
            case 2: this.currentIdleFrame = idleRight; break;
            case 3: this.currentIdleFrame = idleBack; break;
            case 0:
            default: this.currentIdleFrame = idleFront; break;
        }
        this.getSprite().setRegion(currentIdleFrame);
    }

    // Accesseurs pour le Menu (Assurez-vous que Fighter a les getters correspondants si private)
    public int getMaxPV() { return maxPV; }
    public int getMaxPM() { return maxPM; }
    public int getMaxPA() { return maxPA; }
}
