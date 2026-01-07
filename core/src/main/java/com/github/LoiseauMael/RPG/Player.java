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

// Imports Items & Equipment
import com.github.LoiseauMael.RPG.items.Item;
import com.github.LoiseauMael.RPG.items.Equipment;
import com.github.LoiseauMael.RPG.items.Weapon;
import com.github.LoiseauMael.RPG.items.Armor;
import com.github.LoiseauMael.RPG.items.Relic;

public abstract class Player extends Fighter implements Disposable {

    private final Texture texture;

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

    private static final float SPEED = 6f;
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;

    // --- INVENTAIRE ---
    private Array<Item> inventory;

    // --- EQUIPEMENT (Slots) ---
    private Weapon equippedWeapon;
    private Armor equippedArmor;
    private Relic equippedRelic;

    protected Player(float positionX, float positionY, float velocityX, float velocityY,
                     int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP,
                     Sprite sprite, Texture texture) {

        super(positionX, positionY, velocityX, velocityY, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, sprite);

        this.texture = texture;
        this.inventory = new Array<>();

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

        // Gestion Animation Déplacement
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
    }

    // ==========================================
    // GESTION EQUIPEMENT & STATS
    // ==========================================

    public String equip(Equipment item) {
        if (!item.canEquip(this)) {
            return "Impossible : Classe invalide !";
        }

        Equipment oldItem = null;

        if (item instanceof Weapon) {
            oldItem = equippedWeapon;
            equippedWeapon = (Weapon) item;
        }
        else if (item instanceof Armor) {
            oldItem = equippedArmor;
            equippedArmor = (Armor) item;
        }
        else if (item instanceof Relic) {
            oldItem = equippedRelic;
            equippedRelic = (Relic) item;
        }

        if (oldItem != null) {
            addItem(oldItem);
        }

        if (item.getCount() > 1) {
            item.addCount(-1);
        } else {
            inventory.removeValue(item, true);
        }

        return "Equipé : " + item.getName();
    }

    public void unequip(Equipment item) {
        if (item == equippedWeapon) equippedWeapon = null;
        else if (item == equippedArmor) equippedArmor = null;
        else if (item == equippedRelic) equippedRelic = null;

        addItem(item);
    }

    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor getEquippedArmor() { return equippedArmor; }
    public Relic getEquippedRelic() { return equippedRelic; }

    @Override
    public int getFOR() {
        int total = super.FOR;
        if (equippedWeapon != null) total += equippedWeapon.bonusFOR;
        return total;
    }

    @Override
    public int getFORM() {
        int total = super.FORM;
        if (equippedWeapon != null) total += equippedWeapon.bonusFORM;
        return total;
    }

    @Override
    public int getDEF() {
        int total = super.DEF;
        if (equippedArmor != null) total += equippedArmor.bonusDEF;
        return total;
    }

    @Override
    public int getDEFM() {
        int total = super.DEFM;
        if (equippedArmor != null) total += equippedArmor.bonusDEFM;
        return total;
    }

    public float getDamageMultiplier() {
        return (equippedRelic != null) ? equippedRelic.damageMultiplier : 1.0f;
    }

    public float getDefenseMultiplier() {
        return (equippedRelic != null) ? equippedRelic.defenseMultiplier : 1.0f;
    }

    // ==========================================
    // GESTION XP / NIVEAUX / SETTERS POUR SAUVEGARDE
    // ==========================================

    public int getMaxExp() { return level * 100; }

    public void gainExp(int amount) {
        this.exp += amount;
        while (this.exp >= getMaxExp()) {
            this.exp -= getMaxExp();
            levelUp();
        }
    }

    private int getStatIncrease(int currentStatValue) {
        int increase = (int) (currentStatValue * 0.10f);
        return Math.max(1, increase);
    }

    private void levelUp() {
        this.level++;
        this.maxPV += getStatIncrease(this.maxPV); this.PV = this.maxPV;
        this.maxPM += getStatIncrease(this.maxPM); this.PM = this.maxPM;
        this.FOR += getStatIncrease(this.FOR);
        this.DEF += getStatIncrease(this.DEF);
        this.FORM += getStatIncrease(this.FORM);
        this.DEFM += getStatIncrease(this.DEFM);
        this.VIT += getStatIncrease(this.VIT);
        if (this.maxPA > 0) { this.maxPA += getStatIncrease(this.maxPA); this.PA = this.maxPA; }
        Gdx.app.log("Player", "NIVEAU UP ! Niveau " + level);
    }

    // --- SETTERS POUR LE CHARGEMENT (LOAD) ---
    public void setLevel(int level) { this.level = level; }
    public void setExp(int exp) { this.exp = exp; }
    public void setMoney(int money) { this.money = money; } // Pour écraser l'or
    // Les PV/PM sont déjà gérés par setPV/setPM dans Fighter

    // ==========================================
    // INVENTAIRE
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
        if (item.getCount() <= 0) inventory.removeValue(item, true);
    }

    public Array<Item> getInventory() { return inventory; }

    // ==========================================
    // RENDU & HELPERS
    // ==========================================

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

    @Override
    public void dispose() {
        if (this.texture != null) this.texture.dispose();
    }

    public int getDirection() { return currentDirection; }

    public void setDirection(int direction) {
        this.currentDirection = direction;
        switch (direction) {
            case 1: this.currentIdleFrame = idleLeft; break;
            case 2: this.currentIdleFrame = idleRight; break;
            case 3: this.currentIdleFrame = idleBack; break;
            case 0: default: this.currentIdleFrame = idleFront; break;
        }
        this.getSprite().setRegion(currentIdleFrame);
    }

    public int getMaxPV() { return maxPV; }
    public int getMaxPM() { return maxPM; }
    public int getMaxPA() { return maxPA; }
}
