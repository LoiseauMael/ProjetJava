package com.github.LoiseauMael.RPG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.*;
import com.github.LoiseauMael.RPG.npcs.*;

public class MapLoader {

    public static Array<Entity> loadEntities(TiledMap map, Main game) {
        Array<Entity> entities = new Array<>();

        if (map.getLayers().get("Entities") == null) {
            Gdx.app.error("MapLoader", "Calque 'Entities' introuvable dans la map !");
            return entities;
        }

        MapObjects objects = map.getLayers().get("Entities").getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                // Conversion pixels -> tuiles
                float x = rect.x / 16f;
                float y = rect.y / 16f;

                // Lecture du Type
                String type = object.getProperties().get("type", String.class);
                if (type == null) type = object.getProperties().get("class", String.class);
                if (type == null) continue;

                // On ignore les points de spawn (gérés par getPlayerSpawn)
                if ("PlayerStart".equals(type) || "Start".equals(object.getName())) continue;

                // Gestion de l'ID unique (lecture ou génération)
                Integer propId = null;
                try {
                    propId = object.getProperties().get("id", Integer.class);
                } catch (Exception e) {
                    try {
                        String strId = object.getProperties().get("id", String.class);
                        if (strId != null) propId = Integer.parseInt(strId);
                    } catch (NumberFormatException ignored) {}
                }
                int id = (propId != null) ? propId : (int)(x * 1000 + y);

                // Propriétés communes
                String texturePath = object.getProperties().get("texture", String.class);
                String dialsRaw = object.getProperties().get("dialogues", String.class);
                String[] dialogues = (dialsRaw != null) ? dialsRaw.split(";") : null;

                Entity entity = null;
                try {
                    // --- TYPES D'ENTITÉS ---

                    if ("Enemy".equals(type) || "Monster".equals(type)) {
                        // Ennemi piloté par les propriétés Tiled
                        entity = new GenericEnemy(x, y, id, object.getProperties());
                    }
                    else if ("NPC".equals(type)) {
                        String name = object.getProperties().get("name", "Villageois", String.class);
                        if (texturePath == null) texturePath = "assets/HealerSpriteSheet.png";
                        if (dialogues == null) dialogues = new String[]{"..."};
                        entity = new SimpleNPC(x, y, name, texturePath, dialogues);
                    }
                    else if ("Healer".equals(type)) {
                        if (dialogues == null) dialogues = new String[]{"Je peux soigner vos blessures."};
                        entity = new HealerNPC(x, y, texturePath, dialogues);
                    }
                    else if ("Merchant".equals(type)) {
                        if (dialogues == null) dialogues = new String[]{"Jetez un œil à ma boutique."};
                        entity = new MerchantNPC(x, y, texturePath, dialogues);
                    }
                    else if ("Exit".equals(type)) {
                        // SORTIE DE CARTE (Transition)
                        String targetMap = object.getProperties().get("targetMap", String.class);
                        float targetX = object.getProperties().get("targetX", 0f, Float.class);
                        float targetY = object.getProperties().get("targetY", 0f, Float.class);

                        // Largeur/Hauteur de la zone en tuiles
                        float w = rect.width / 16f;
                        float h = rect.height / 16f;

                        if (targetMap != null) {
                            entity = new MapExit(x, y, w, h, targetMap, targetX, targetY);
                        }
                    }

                } catch (Exception e) {
                    Gdx.app.error("MapLoader", "Erreur entité " + type + " (ID: " + id + "): " + e.getMessage());
                }

                // Ajout à la liste
                if (entity != null) {
                    // Ne pas ajouter les ennemis déjà morts
                    if (entity instanceof Enemy && game.deadEnemyIds.contains(entity.getId(), false)) {
                        continue;
                    }
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    public static Vector2 getPlayerSpawn(TiledMap map) {
        if (map.getLayers().get("Entities") != null) {
            for (MapObject object : map.getLayers().get("Entities").getObjects()) {
                String type = object.getProperties().get("type", String.class);
                if ("Start".equals(object.getName()) || "PlayerStart".equals(type)) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        return new Vector2(rect.x / 16f, rect.y / 16f);
                    }
                }
            }
        }
        return new Vector2(10, 10);
    }
}
