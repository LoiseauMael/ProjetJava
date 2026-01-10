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
            Gdx.app.error("MapLoader", "Calque 'Entities' introuvable !");
            return entities;
        }

        MapObjects objects = map.getLayers().get("Entities").getObjects();

        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                float x = rect.x / 16f;
                float y = rect.y / 16f;

                String type = object.getProperties().get("type", String.class);
                if (type == null) type = object.getProperties().get("class", String.class);
                if (type == null) continue;

                // Ignorer l'objet de départ du joueur s'il est lu comme une entité par erreur
                if ("PlayerStart".equals(type) || "Start".equals(object.getName())) continue;

                Integer propId = null;
                try {
                    propId = object.getProperties().get("id", Integer.class);
                } catch (Exception e) {
                    String strId = object.getProperties().get("id", String.class);
                    if (strId != null) propId = Integer.parseInt(strId);
                }
                int id = (propId != null) ? propId : (int)(x * 1000 + y);

                Entity entity = null;
                try {
                    switch (type) {
                        case "KingGoblin":
                            entity = new KingGoblin(x, y, getIntProperty(object, "level", 1), id);
                            break;
                        case "Goblin":
                            entity = new Goblin(x, y, getIntProperty(object, "level", 1), id);
                            break;
                        case "Healer":
                            String hDial = object.getProperties().get("dialogues", "Bonjour !;Besoin de soins ?", String.class);
                            entity = new HealerNPC(x, y, hDial.split(";"));
                            break;
                        case "Merchant":
                            String mDial = object.getProperties().get("dialogues", "Bienvenue !;Voulez-vous acheter ?", String.class);
                            entity = new MerchantNPC(x, y, mDial.split(";"));
                            break;
                        case "NPC":
                            String name = object.getProperties().get("name", "Villageois", String.class);
                            String sprite = object.getProperties().get("sprite", "default.png", String.class).replace("npcs/", "");
                            String dials = object.getProperties().get("dialogues", "...", String.class);
                            entity = new SimpleNPC(x, y, name, sprite, dials.split(";"));
                            break;
                    }
                } catch (Exception e) {
                    Gdx.app.error("MapLoader", "Erreur entité " + type + ": " + e.getMessage());
                }

                if (entity != null) {
                    if (!(entity instanceof Enemy && game.deadEnemyIds.contains(entity.getId(), false))) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }

    private static int getIntProperty(MapObject object, String key, int defaultValue) {
        Object val = object.getProperties().get(key);
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch(NumberFormatException e) { return defaultValue; }
        }
        return defaultValue;
    }

    public static Vector2 getPlayerSpawn(TiledMap map) {
        // CORRECTION : On cherche dans "Entities" au lieu de "Collisions"
        if (map.getLayers().get("Entities") != null) {
            for (MapObject object : map.getLayers().get("Entities").getObjects()) {
                String type = object.getProperties().get("type", String.class);
                // On accepte soit le Nom "Start", soit le Type "PlayerStart"
                if ("Start".equals(object.getName()) || "PlayerStart".equals(type)) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        // Retourne la position exacte (16px = 1 unité)
                        return new Vector2(rect.x / 16f, rect.y / 16f);
                    }
                }
            }
        }
        // Valeur par défaut si non trouvé
        return new Vector2(10, 10);
    }
}
