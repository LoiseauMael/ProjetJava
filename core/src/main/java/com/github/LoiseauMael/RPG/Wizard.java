package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Wizard extends Player {

    private Wizard(float x, float y, Sprite sprite, Texture texture) {
        // PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP
        // Stats Mage : Moins de PV (70), Plus de PM (80), Faible FOR (4), Forte FORM (15)
        super(x, y, 0, 0, 70, 80, 6, 4, 3, 15, 10, 8, 4, sprite, texture);
    }

    public static Wizard create(float x, float y) {
        // Chargement de la texture spécifique au mage
        Texture texture = new Texture(Gdx.files.internal("WizardSpriteSheet.png"));
        Sprite sprite = new Sprite(texture);

        // --- CORRECTION TAILLE ---
        // Le sprite fait 32 pixels. L'échelle du monde est 1 unité = 16 pixels.
        // Donc la taille doit être 32 / 16 = 2.0f unités.
        sprite.setSize(2.0f, 2.0f);

        return new Wizard(x, y, sprite, texture);
    }
}
