package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SwordMan extends Player {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;
    private static final float UNIT_SCALE = 1f / 16f;

    private SwordMan(float x, float y, int pv, int pm, int pa, int force, int def, int form, int defM, int vit, int dep, Sprite sprite, Texture texture, String weaponPath) {
        // Notez l'ordre des attributs : FOR puis DEF puis FORM
        super(x, y, 0, 0, pv, pm, pa, force, def, form, defM, vit, dep, sprite, texture, weaponPath);
    }

    public static SwordMan create(float x, float y) {
        String charPath = "SwordmanSpriteSheet.png";
        Texture texture = new Texture(Gdx.files.internal(charPath));
        Sprite sprite = new Sprite(texture);
        sprite.setSize(FRAME_WIDTH * UNIT_SCALE, FRAME_HEIGHT * UNIT_SCALE);

        // Nom de l'image de l'arme
        String weaponPath = "arme/sword2.png";

        // STATS : FOR = 15, FORM = 5
        return new SwordMan(x, y,
            120, // PV
            20,  // PM
            6,   // PA
            15,  // FOR (Force Physique)
            10,  // DEF
            5,   // FORM (Force Magique) - Faible ici
            5,   // DEFM
            12,  // VIT
            3,   // DEP
            sprite, texture, weaponPath
        );
    }
}
