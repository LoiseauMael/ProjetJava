package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

    protected float positionX;
    protected float positionY;
    protected float velocityX;
    protected float velocityY;
    protected Sprite sprite;
    protected Rectangle boundingBox;

    protected int currentDirection = 0;
    protected static CollisionSystem collisionSystem;

    public Entity(float positionX, float positionY, Sprite sprite) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.sprite = sprite;
        this.sprite.setPosition(positionX, positionY);

        // --- CORRECTION : HITBOX PLUS PETITE (PIEDS) ---
        // Le sprite fait 2.0f de large/haut.
        // On veut une hitbox de 1.0f de large et 0.6f de haut (juste les pieds).
        float boxW = sprite.getWidth() * 0.5f;
        float boxH = sprite.getHeight() * 0.3f;

        // On crée la boite. On la mettra à jour dans updateBoxPosition()
        this.boundingBox = new Rectangle(positionX, positionY, boxW, boxH);

        // Mise à jour initiale
        updateBoxPosition();
    }

    public static void setCollisionSystem(CollisionSystem system) {
        Entity.collisionSystem = system;
    }

    /**
     * Met à jour la position de la hitbox par rapport au sprite.
     * Elle est centrée horizontalement et collée en bas.
     */
    protected void updateBoxPosition() {
        // Centrer la box en X : PositionX + (LargeurSprite - LargeurBox) / 2
        float offsetX = (sprite.getWidth() - boundingBox.width) / 2;

        // En Y, on laisse à 0 (les pieds)
        float offsetY = 0;

        boundingBox.setPosition(positionX + offsetX, positionY + offsetY);
    }

    public void move(float amountX, float amountY) {
        // 1. Test X
        float oldX = positionX;
        positionX += amountX;
        updateBoxPosition(); // On bouge la boite virtuellement

        if (collisionSystem != null && collisionSystem.isColliding(boundingBox)) {
            positionX = oldX; // Collision ! On annule
            updateBoxPosition();
        }

        // 2. Test Y
        float oldY = positionY;
        positionY += amountY;
        updateBoxPosition(); // On bouge la boite virtuellement

        if (collisionSystem != null && collisionSystem.isColliding(boundingBox)) {
            positionY = oldY; // Collision ! On annule
            updateBoxPosition();
        }

        // 3. Mise à jour finale du sprite
        sprite.setPosition(positionX, positionY);
    }

    public void update(float delta) {
        // La position physique est gérée par move().
        // Ici on gère juste l'animation et la mise à jour visuelle.

        // Si on a bougé manuellement (sans move), on synchronise
        sprite.setPosition(positionX, positionY);
        updateBoxPosition();

        if (Math.abs(velocityX) > 0.01f || Math.abs(velocityY) > 0.01f) {
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                currentDirection = (velocityX > 0) ? 2 : 1;
            } else {
                currentDirection = (velocityY > 0) ? 3 : 0;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    // Getters / Setters inchangés...
    public float get_positionX() { return positionX; }
    public float get_positionY() { return positionY; }
    public void set_position(float x, float y) {
        this.positionX = x;
        this.positionY = y;
        this.sprite.setPosition(x, y);
        updateBoxPosition();
    }
    public float get_velocityX() { return velocityX; }
    public float get_velocityY() { return velocityY; }
    public void set_velocityX(float v) { this.velocityX = v; }
    public void set_velocityY(float v) { this.velocityY = v; }
    public Rectangle getBoundingBox() { return boundingBox; }
    public Sprite getSprite() { return sprite; }
    public int getDirection() { return currentDirection; }
}
