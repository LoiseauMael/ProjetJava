package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Disposable;

public class Player extends Fighter implements Disposable {

    //private Texture texture;
    private static final float SPEED = 250.0f;

    public Player(float positionX, float positionY, float velocityX, float velocityY, int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, Sprite sprite) {

        super(positionX, positionY, velocityX, velocityY, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, sprite);
        this.getSprite().setSize(64f, 64f);
    }

    public void update(float delta) {

        move(get_velocityX() * delta, get_velocityY() * delta);

        // this.sprite est le champ hérité de Entity, déjà positionné par move()
        if (Math.abs(get_velocityX()) > 0.01f) {
            if (get_velocityX() < 0 && this.getSprite() != null && !this.getSprite().isFlipX()) {
                this.getSprite().flip(true, false);
            } else if (get_velocityX() > 0 && this.getSprite() != null && this.getSprite().isFlipX()) {
                this.getSprite().flip(true, false);
            }
        }
    }

    public void handleInput() {
        set_velocityX(0);
        set_velocityY(0);

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            set_velocityX(-SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            set_velocityX(SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            set_velocityY(SPEED);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            set_velocityY(-SPEED);
        }
    }

    @Override
    public void dispose() {
        // Seule la Texture est une ressource LibGDX à libérer
        //if (this.texture != null) {
            //this.texture.dispose();
        }
        // Le Sprite (this.sprite) n'a pas besoin d'être libéré
}
