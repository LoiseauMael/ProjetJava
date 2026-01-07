package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.github.LoiseauMael.RPG.Entity;
import com.github.LoiseauMael.RPG.Player;

public class NPC extends Entity implements Disposable {
    private Rectangle bounds;
    private String name;
    private String[] dialogues;

    // --- ANIMATION ---
    private Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    private float stateTime;

    private static final int FRAME_COLS = 3;
    private static final int FRAME_ROWS = 4;

    public NPC(float x, float y, String texturePath, String name, String[] dialogues) {
        super(x, y, createScaledSprite(texturePath));
        this.bounds = new Rectangle(x, y, 32, 32);
        this.name = name;
        this.dialogues = dialogues;
        initAnimations();
    }

    private static Sprite createScaledSprite(String texturePath) {
        Texture texture = new Texture(Gdx.files.internal(texturePath));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Sprite sprite = new Sprite(texture);
        sprite.setSize(2.0f, 2.0f); // Taille adaptée au joueur
        sprite.setOriginCenter();
        return sprite;
    }

    private void initAnimations() {
        Texture texture = this.getSprite().getTexture();

        TextureRegion[][] tmp = TextureRegion.split(texture,
            texture.getWidth() / FRAME_COLS,
            texture.getHeight() / FRAME_ROWS);

        walkDown  = new Animation<>(0.2f, tmp[0]);
        walkLeft  = new Animation<>(0.2f, tmp[1]);
        walkRight = new Animation<>(0.2f, tmp[2]);
        walkUp    = new Animation<>(0.2f, tmp[3]);

        walkDown.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        walkLeft.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        walkRight.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        walkUp.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        stateTime = 0f;
        this.getSprite().setRegion(walkDown.getKeyFrames()[1]);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;

        TextureRegion currentFrame = null;
        boolean isMoving = Math.abs(get_velocityX()) > 0.01f || Math.abs(get_velocityY()) > 0.01f;

        switch (getDirection()) {
            case 2: // Droite
                currentFrame = isMoving ? walkRight.getKeyFrame(stateTime, true) : walkRight.getKeyFrames()[1];
                break;
            case 1: // Gauche
                currentFrame = isMoving ? walkLeft.getKeyFrame(stateTime, true) : walkLeft.getKeyFrames()[1];
                break;
            case 3: // Haut
                currentFrame = isMoving ? walkUp.getKeyFrame(stateTime, true) : walkUp.getKeyFrames()[1];
                break;
            case 0: // Bas
            default:
                currentFrame = isMoving ? walkDown.getKeyFrame(stateTime, true) : walkDown.getKeyFrames()[1];
                break;
        }

        if (currentFrame != null) {
            this.getSprite().setRegion(currentFrame);
        }
    }

    public void onInteract(Player player) {
        float diffX = player.get_positionX() - this.get_positionX();
        float diffY = player.get_positionY() - this.get_positionY();

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (diffX > 0) currentDirection = 2;
            else currentDirection = 1;
        } else {
            if (diffY > 0) currentDirection = 3;
            else currentDirection = 0;
        }

    }

    public Rectangle getBounds() {
        return bounds;
    }

    public String getName() { return name; }
    public String[] getDialogues() { return dialogues; }

    @Override
    public void dispose() {
        if (this.getSprite() != null && this.getSprite().getTexture() != null) {
            this.getSprite().getTexture().dispose();
        }
    }
}
