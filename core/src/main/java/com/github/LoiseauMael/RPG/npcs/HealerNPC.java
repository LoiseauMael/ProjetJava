package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HealerNPC extends NPC {

    public HealerNPC(float x, float y, String[] dialogues) {
        // Chargement de la texture (assurez-vous que le fichier est bien dans assets/)
        super(x, y, "Guérisseur", dialogues,
            new Sprite(new TextureRegion(new Texture("HealerSpriteSheet.png"), 0, 0, 16, 16)));
    }

    // PLUS BESOIN de surcharger update() ni draw() !
    // Entity.java s'occupe de mettre à jour la position et de dessiner le sprite centré.
}
