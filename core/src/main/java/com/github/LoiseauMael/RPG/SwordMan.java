package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SwordMan extends Player {

    private SwordMan(float x, float y, Sprite sprite, Texture texture) {
        // PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP
        super(x, y, 0, 0, 100, 20, 6, 10, 5, 2, 3, 5, 4, sprite, texture);
    }

    public static SwordMan create(float x, float y) {
        Texture texture = new Texture(Gdx.files.internal("SwordmanSpriteSheet.png"));
        Sprite sprite = new Sprite(texture);

        // --- CORRECTION TAILLE ---
        // Le sprite fait 32 pixels. L'échelle du monde est 1 unité = 16 pixels.
        // Donc la taille doit être 32 / 16 = 2.0f unités.
        sprite.setSize(2.0f, 2.0f);

        return new SwordMan(x, y, sprite, texture);
    }
}
