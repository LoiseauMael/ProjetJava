package com.github.LoiseauMael.RPG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.model.entities.Enemy;
import com.github.LoiseauMael.RPG.model.entities.Entity;
import com.github.LoiseauMael.RPG.model.entities.GenericEnemy;
import com.github.LoiseauMael.RPG.npcs.HealerNPC;
import com.github.LoiseauMael.RPG.npcs.MapExit;
import com.github.LoiseauMael.RPG.npcs.MerchantNPC;
import com.github.LoiseauMael.RPG.npcs.SimpleNPC;

/**
 * Classe utilitaire pour charger les éléments dynamiques depuis une carte Tiled.
 * <p>
 * Elle lit la couche d'objets nommée "Entities" dans le fichier .tmx et instancie
 * les objets Java correspondants (Ennemis, PNJs, Sorties de carte).
 */
public class MapLoader {

    /**
     * Parcourt la couche "Entities" de la carte Tiled et crée les entités du jeu.
     *
     * @param map La TiledMap chargée en mémoire.
     * @param game L'instance principale du jeu (utilisée pour vérifier les ennemis morts via leur ID).
     * @return Une liste (Array) contenant toutes les entités instanciées prêtes à être ajoutées au jeu.
     */
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
                // Conversion pixels -> tuiles (1 tuile = 16 pixels)
                float x = rect.x / 16f;
                float y = rect.y / 16f;

                // Lecture du Type (propriété personnalisée "type" ou "class" dans Tiled)
                String type = object.getProperties().get("type", String.class);
                if (type == null) type = object.getProperties().get("class", String.class);
                if (type == null) continue;

                // On ignore les points de spawn (gérés séparément par getPlayerSpawn)
                if ("PlayerStart".equals(type) || "Start".equals(object.getName())) continue;

                // Gestion de l'ID unique (lecture depuis Tiled ou génération procédurale)
                Integer propId = null;
                try {
                    propId = object.getProperties().get("id", Integer.class);
                } catch (Exception e) {
                    try {
                        String strId = object.getProperties().get("id", String.class);
                        if (strId != null) propId = Integer.parseInt(strId);
                    } catch (NumberFormatException ignored) {
                    }
                }
                // Si pas d'ID, on en génère un basé sur la position (x*1000 + y)
                int id = (propId != null) ? propId : (int) (x * 1000 + y);

                // Propriétés communes (texture, dialogues)
                String texturePath = object.getProperties().get("texture", String.class);
                String dialsRaw = object.getProperties().get("dialogues", String.class);
                String[] dialogues = (dialsRaw != null) ? dialsRaw.split(";") : null;

                Entity entity = null;
                try {
                    // --- INSTANCIATION SELON LE TYPE ---

                    if ("Enemy".equals(type) || "Monster".equals(type)) {
                        // Ennemi générique (Stats définies dans Tiled)
                        entity = new GenericEnemy(x, y, id, object.getProperties());
                    } else if ("NPC".equals(type)) {
                        // PNJ Villageois simple
                        String name = object.getProperties().get("name", "Villageois", String.class);
                        if (texturePath == null) texturePath = "assets/HealerSpriteSheet.png";
                        if (dialogues == null) dialogues = new String[]{"..."};
                        entity = new SimpleNPC(x, y, name, texturePath, dialogues);
                    } else if ("Healer".equals(type)) {
                        // PNJ Guérisseur
                        if (dialogues == null) dialogues = new String[]{"Je peux soigner vos blessures."};
                        entity = new HealerNPC(x, y, texturePath, dialogues);
                    } else if ("Merchant".equals(type)) {
                        // PNJ Marchand
                        if (dialogues == null) dialogues = new String[]{"Jetez un œil à ma boutique."};
                        entity = new MerchantNPC(x, y, texturePath, dialogues);
                    } else if ("Exit".equals(type)) {
                        // Zone de sortie vers une autre carte
                        String targetMap = object.getProperties().get("targetMap", String.class);
                        float targetX = object.getProperties().get("targetX", 0f, Float.class);
                        float targetY = object.getProperties().get("targetY", 0f, Float.class);

                        // Dimensions de la zone de déclenchement
                        float w = rect.width / 16f;
                        float h = rect.height / 16f;

                        if (targetMap != null) {
                            entity = new MapExit(x, y, w, h, targetMap, targetX, targetY);
                        }
                    }

                } catch (Exception e) {
                    Gdx.app.error("MapLoader", "Erreur entité " + type + " (ID: " + id + "): " + e.getMessage());
                }

                // Ajout à la liste si l'entité est valide
                if (entity != null) {
                    // Vérification de la persistance : ne pas respawn les ennemis tués
                    if (entity instanceof Enemy && game.deadEnemyIds.contains(entity.getId(), false)) {
                        continue;
                    }
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    /**
     * Recherche le point de départ du joueur ("PlayerStart") dans la carte.
     *
     * @param map La TiledMap chargée.
     * @return Un Vector2 contenant les coordonnées (en tuiles) du spawn, ou (10, 10) par défaut.
     */
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
