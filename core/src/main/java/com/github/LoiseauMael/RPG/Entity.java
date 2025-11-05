package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Entity {
    private float positionX;
    private float positionY;
    private float velocityX;
    private float velocityY;

    private Sprite sprite;


    public Entity (float positionX, float positionY, float velocityX, float velocityY, Sprite sprite) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.sprite = sprite;
    }

    public void move(float deltaX, float deltaY) {
        this.positionX += deltaX;
        this.positionY += deltaY;
        if (this.sprite != null) {
            this.sprite.setPosition(this.positionX, this.positionY);
        }
    }

    public void set_position(float positionX, float positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        if (this.sprite != null) {
            this.sprite.setPosition(positionX, positionY);
        }
    }

    public void draw(SpriteBatch batch) {
        if (this.sprite != null) {
            this.sprite.draw(batch);
        }
    }

    public float get_positionX() { return this.positionX; }
    public float get_positionY() { return this.positionY; }
    public float get_velocityX() { return this.velocityX; }
    public float get_velocityY() { return this.velocityY; }
    public void set_velocityX(float velocityX) { this.velocityX = velocityX; }
    public void set_velocityY(float velocityY) { this.velocityY = velocityY; }

    public Sprite getSprite() { return sprite; }
}
