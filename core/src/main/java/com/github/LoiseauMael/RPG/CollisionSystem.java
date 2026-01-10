package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.npcs.NPC;

public class CollisionSystem {
    private TiledMap map;
    private Array<NPC> npcs;

    public CollisionSystem(TiledMap map) {
        this.map = map;
        this.npcs = new Array<>();
    }

    public void setNpcs(Array<NPC> npcs) { this.npcs = npcs; }

    public boolean isColliding(Rectangle box) {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("Collisions");

        if (collisionLayer != null) {
            // On teste les points clés de la boîte centrée
            float[][] points = {
                {box.x, box.y},                               // Bas-Gauche
                {box.x + box.width, box.y},                  // Bas-Droite
                {box.x, box.y + box.height},                 // Haut-Gauche
                {box.x + box.width, box.y + box.height},    // Haut-Droite
                {box.x + box.width / 2f, box.y}              // CENTRE BAS (entre les pieds)
            };

            for (float[] pt : points) {
                if (checkTileCollision(pt[0], pt[1], collisionLayer)) return true;
            }
        }

        if (npcs != null) {
            for (NPC npc : npcs) {
                Rectangle npcBox = npc.getBoundingBox();
                // box != npcBox évite que l'entité ne se bloque elle-même
                if (box.overlaps(npcBox) && box != npcBox) return true;
            }
        }
        return false;
    }

    private boolean checkTileCollision(float x, float y, TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell((int)x, (int)y);
        return cell != null && cell.getTile() != null;
    }
}
