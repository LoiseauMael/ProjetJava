package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.LoiseauMael.RPG.Entity;

public class MapExit extends Entity {

    private String targetMap;
    private float targetX, targetY;

    public MapExit(float x, float y, float width, float height, String targetMap, float tX, float tY) {
        super(x, y, null); // Pas de sprite
        this.targetMap = targetMap;
        this.targetX = tX;
        this.targetY = tY;

        // On définit la zone de collision manuellement car il n'y a pas de sprite
        this.setCollisionBounds(width, height, 0, 0);
    }

    // On surcharge draw pour ne RIEN dessiner (invisible)
    @Override
    public void draw(SpriteBatch batch) {
        // Invisible
    }

    public String getTargetMap() { return targetMap; }
    public float getTargetX() { return targetX; }
    public float getTargetY() { return targetY; }
}
