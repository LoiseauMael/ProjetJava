package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Wizard extends Player {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final float UNIT_SCALE = 1f / 16f;

    private Wizard(float x, float y, int pv, int pm, int pa, int force, int def, int form, int defM, int vit, int dep, Sprite sprite, Texture texture, String weaponPath) {
        super(x, y, 0, 0, pv, pm, pa, force, def, form, defM, vit, dep, sprite, texture, weaponPath);
    }

    public static Wizard create(float x, float y) {
        // On réutilise le sprite sheet du joueur (ou un autre si vous avez)
        String charPath = "SwordmanSpriteSheet.png";
        Texture texture = new Texture(Gdx.files.internal(charPath));
        Sprite sprite = new Sprite(texture);
        sprite.setSize(FRAME_WIDTH * UNIT_SCALE, FRAME_HEIGHT * UNIT_SCALE);

        // Image du baton
        String weaponPath = "Staff.png";

        // STATS : FOR = 5, FORM = 20
        return new Wizard(x, y,
            80,  // PV (Faible)
            100, // PM (Elevé)
            6,   // PA
            5,   // FOR (Faible)
            5,   // DEF
            20,  // FORM (Force Magique) - Elevée !
            15,  // DEFM
            11,  // VIT
            3,   // DEP
            sprite, texture, weaponPath
        );
    }
}
