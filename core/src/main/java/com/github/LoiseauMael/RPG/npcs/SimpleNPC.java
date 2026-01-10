package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SimpleNPC extends NPC {

    public SimpleNPC(float x, float y, String name, String spritePath, String[] dialogues) {
        // Découpage dynamique de la frame de base pour les NPCs chargés via Tiled
        super(x, y, name, dialogues, new Sprite(new TextureRegion(new Texture(spritePath), 0, 0, 16, 16)));

        if (this.sprite != null) {
            this.sprite.setSize(1f, 1f);
        }
    }

    @Override
    public void update(float delta) {
        if (sprite != null) {
            sprite.setPosition(get_positionX(), get_positionY());
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (sprite != null) {
            sprite.draw(batch);
        }
    }
}
