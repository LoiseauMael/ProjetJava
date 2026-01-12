package com.github.LoiseauMael.RPG.physics;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.npcs.NPC;

/**
 * Système centralisé de gestion des collisions.
 * <p>
 * Il vérifie si une entité peut se déplacer à une position donnée en testant :
 * <ol>
 * <li>Les tuiles bloquantes de la carte (Layer "Collisions").</li>
 * <li>Les boîtes de collision des autres PNJ (pour éviter de marcher sur quelqu'un).</li>
 * </ol>
 */
public class CollisionSystem {
    private TiledMap map;
    private Array<NPC> npcs;

    /**
     * Initialise le système avec la carte actuelle.
     * @param map La carte Tiled (doit contenir un calque nommé "Collisions").
     */
    public CollisionSystem(TiledMap map) {
        this.map = map;
        this.npcs = new Array<>();
    }

    /**
     * Met à jour la liste des PNJs à prendre en compte pour les collisions dynamiques.
     * Appelée par le MapLoader ou l'ExplorationState lors du changement de carte.
     */
    public void setNpcs(Array<NPC> npcs) { this.npcs = npcs; }

    /**
     * Vérifie si une boîte de collision (Hitbox) entre en intersection avec un obstacle.
     *
     * @param box Le rectangle de collision de l'entité qui tente de bouger.
     * @return {@code true} si un obstacle est touché (mouvement impossible), {@code false} sinon.
     */
    public boolean isColliding(Rectangle box) {
        TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("Collisions");

        if (collisionLayer != null) {
            // Optimisation : On ne teste que 5 points clés de la hitbox au lieu de toute la surface.
            // Cela suffit pour une grille 2D standard.
            float[][] points = {
                {box.x, box.y},                               // Coin Bas-Gauche
                {box.x + box.width, box.y},                   // Coin Bas-Droite
                {box.x, box.y + box.height},                  // Coin Haut-Gauche
                {box.x + box.width, box.y + box.height},      // Coin Haut-Droite
                {box.x + box.width / 2f, box.y}               // Point CENTRAL BAS (entre les pieds, crucial pour le feeling)
            };

            for (float[] pt : points) {
                if (checkTileCollision(pt[0], pt[1], collisionLayer)) return true;
            }
        }

        // Vérification des collisions dynamiques avec les PNJs
        if (npcs != null) {
            for (NPC npc : npcs) {
                Rectangle npcBox = npc.getBoundingBox();
                // box != npcBox est essentiel : empêche l'entité de se bloquer elle-même
                if (box.overlaps(npcBox) && box != npcBox) return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si la cellule (tuile) aux coordonnées (x,y) contient un élément bloquant.
     */
    private boolean checkTileCollision(float x, float y, TiledMapTileLayer layer) {
        TiledMapTileLayer.Cell cell = layer.getCell((int)x, (int)y);
        // Si la cellule existe et contient une tuile graphique, c'est un obstacle.
        return cell != null && cell.getTile() != null;
    }
}
