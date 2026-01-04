package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Entity {
    private float positionX;
    private float positionY;
    private float velocityX;
    private float velocityY;
    private Sprite sprite;
    protected final Rectangle bounds;
    private float hitboxOffsetX;
    private float hitboxOffsetY;
    private static CollisionSystem collisionSystem;

    public static void setCollisionSystem(CollisionSystem system) {
        if (system == null) throw new IllegalArgumentException("CollisionSystem nul");
        collisionSystem = system;
    }

    public Entity (float positionX, float positionY, float velocityX, float velocityY, Sprite sprite) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.sprite = sprite;

        // Hitbox standard
        float hitboxWidth = 0.6f;
        float hitboxHeight = 0.4f;

        if (this.sprite != null) {
            float spriteWidth = this.sprite.getWidth();
            this.hitboxOffsetX = (spriteWidth - hitboxWidth) / 2f;
            this.hitboxOffsetY = 0f;
        } else {
            this.hitboxOffsetX = 0;
            this.hitboxOffsetY = 0;
        }

        this.bounds = new Rectangle(positionX + hitboxOffsetX, positionY + hitboxOffsetY, hitboxWidth, hitboxHeight);

        if (this.sprite != null) {
            this.sprite.setPosition(positionX, positionY);
        }
    }

    public void move(float deltaX, float deltaY) {
        float oldX = this.positionX;
        float oldY = this.positionY;

        // --- AXE X ---
        this.positionX += deltaX;
        this.bounds.x = this.positionX + hitboxOffsetX;

        if (collisionSystem != null && collisionSystem.checkCollision(this.bounds)) {
            this.positionX = oldX;
            this.bounds.x = oldX + hitboxOffsetX;
        }

        // --- AXE Y ---
        this.positionY += deltaY;
        this.bounds.y = this.positionY + hitboxOffsetY;

        if (collisionSystem != null && collisionSystem.checkCollision(this.bounds)) {
            this.positionY = oldY;
            this.bounds.y = oldY + hitboxOffsetY;
        }

        if (this.sprite != null) {
            this.sprite.setPosition(this.positionX, this.positionY);
        }
    }

    // --- Getters & Setters indispensables pour Player ---
    public Sprite getSprite() { return sprite; }
    public Rectangle getBounds() { return bounds; }
    public float get_positionX() { return positionX; }
    public float get_positionY() { return positionY; }
    public float get_velocityX() { return velocityX; }
    public float get_velocityY() { return velocityY; }
    public void set_velocityX(float v) { this.velocityX = v; }
    public void set_velocityY(float v) { this.velocityY = v; }

    public void set_position(float x, float y) {
        this.positionX = x; this.positionY = y;
        this.bounds.setPosition(x + hitboxOffsetX, y + hitboxOffsetY);
        if (sprite != null) sprite.setPosition(x, y);
    }

    public void draw(SpriteBatch batch) {
        if (this.sprite != null) this.sprite.draw(batch);
    }
}
