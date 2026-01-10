package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.LoiseauMael.RPG.items.*;

public abstract class Player extends Fighter implements Disposable {
    private Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    private float stateTime;
    private static final int FRAME_COLS = 3;
    private static final int FRAME_ROWS = 4;

    protected Array<Item> inventory;
    protected Weapon equippedWeapon;
    protected Armor equippedArmor;
    protected Relic equippedRelic;

    // --- Contrôle des entrées ---
    private boolean inputEnabled = true;

    public Player(float x, float y, int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, String texturePath) {
        // MODIFICATION ICI : Le 3ème argument est passé à 1 (Niveau de départ)
        super(x, y, 1, 0, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, new Sprite(new Texture(Gdx.files.internal(texturePath))));

        Texture t = this.getSprite().getTexture();
        float widthInUnits = (t.getWidth() / (float)FRAME_COLS) / 16f;
        float heightInUnits = (t.getHeight() / (float)FRAME_ROWS) / 16f;
        this.getSprite().setSize(widthInUnits, heightInUnits);

        setCollisionBounds(0.5f, 0.3f, 0f, 0.1f);

        initAnimations();
        this.inventory = new Array<>();
    }

    // --- GESTION INPUT ---
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
        if (!enabled) {
            // Stop net le mouvement si on désactive les inputs
            this.velocityX = 0;
            this.velocityY = 0;
        }
    }

    public void handleInput() {
        // Si les inputs sont désactivés (Combat), on ne fait rien
        if (!inputEnabled) {
            this.velocityX = 0;
            this.velocityY = 0;
            return;
        }

        // Sinon (Exploration), comportement normal
        this.velocityX = 0;
        this.velocityY = 0;
        float speed = 4.0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            this.velocityX = -speed;
            setDirection(1);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            this.velocityX = speed;
            setDirection(2);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            this.velocityY = speed;
            setDirection(3);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            this.velocityY = -speed;
            setDirection(0);
        }
    }

    // --- GESTION OBJETS ---

    public void addItem(Item item) {
        for (Item i : inventory) {
            if (i.getName().equals(item.getName())) {
                i.addCount(1);
                return;
            }
        }
        inventory.add(item);
    }

    public void consumeItem(Item item) {
        if (item.use(this)) {
            item.removeCount(1);
            if (item.getCount() <= 0) {
                inventory.removeValue(item, true);
            }
        }
    }

    public void equip(Equipment item) {
        if (item instanceof Weapon) {
            if (equippedWeapon != null) unequip(equippedWeapon);
            equippedWeapon = (Weapon) item;
        } else if (item instanceof Armor) {
            if (equippedArmor != null) unequip(equippedArmor);
            equippedArmor = (Armor) item;
        } else if (item instanceof Relic) {
            if (equippedRelic != null) unequip(equippedRelic);
            equippedRelic = (Relic) item;
        }
        applyEquipmentStats(item, true);
        System.out.println("Équipé : " + item.getName());
    }

    public void unequip(Equipment item) {
        applyEquipmentStats(item, false);
        if (item == equippedWeapon) equippedWeapon = null;
        else if (item == equippedArmor) equippedArmor = null;
        else if (item == equippedRelic) equippedRelic = null;
    }

    private void applyEquipmentStats(Equipment item, boolean isEquipping) {
        int factor = isEquipping ? 1 : -1;
        if (item instanceof Weapon) {
            Weapon w = (Weapon) item;
            this.FOR += w.bonusFOR * factor;
            this.FORM += w.bonusFORM * factor;
        } else if (item instanceof Armor) {
            Armor a = (Armor) item;
            this.DEF += a.bonusDEF * factor;
            this.DEFM += a.bonusDEFM * factor;
        }
    }

    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor getEquippedArmor() { return equippedArmor; }
    public Relic getEquippedRelic() { return equippedRelic; }
    public Array<Item> getInventory() { return inventory; }

    @Override
    public void update(float delta) {
        handleInput();
        super.update(delta);
        updateAnimation(delta);
    }

    protected void updateAnimation(float delta) {
        stateTime += delta;
        boolean isMoving = (velocityX != 0 || velocityY != 0);
        TextureRegion currentFrame;
        switch (getDirection()) {
            case 1: currentFrame = isMoving ? walkLeft.getKeyFrame(stateTime, true) : walkLeft.getKeyFrames()[1]; break;
            case 2: currentFrame = isMoving ? walkRight.getKeyFrame(stateTime, true) : walkRight.getKeyFrames()[1]; break;
            case 3: currentFrame = isMoving ? walkUp.getKeyFrame(stateTime, true) : walkUp.getKeyFrames()[1]; break;
            default: currentFrame = isMoving ? walkDown.getKeyFrame(stateTime, true) : walkDown.getKeyFrames()[1]; break;
        }
        this.getSprite().setRegion(currentFrame);
    }

    @Override
    protected void updateSpriteRegion() {
        updateAnimation(0);
    }

    private void initAnimations() {
        Texture texture = this.getSprite().getTexture();
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / FRAME_COLS, texture.getHeight() / FRAME_ROWS);
        walkDown = new Animation<>(0.2f, tmp[0]);
        walkLeft = new Animation<>(0.2f, tmp[1]);
        walkRight = new Animation<>(0.2f, tmp[2]);
        walkUp = new Animation<>(0.2f, tmp[3]);
    }

    @Override
    public void dispose() {
        if (getSprite() != null) getSprite().getTexture().dispose();
    }
}
