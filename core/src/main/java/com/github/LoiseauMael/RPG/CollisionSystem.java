package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;

public class CollisionSystem {

    private TiledMapTileLayer collisionLayer;

    public CollisionSystem(TiledMap map) {
        // On récupère le calque nommé "Collision" dans votre fichier .tmx
        // ATTENTION : Le nom doit être exact (majuscule/minuscule)
        this.collisionLayer = (TiledMapTileLayer) map.getLayers().get("Collision");
    }

    /**
     * Vérifie si le rectangle donné touche une tuile du calque de collision.
     * @param box La boîte de collision de l'entité (en unités du monde)
     * @return true si collision (mur), false sinon (passage libre)
     */
    public boolean isColliding(Rectangle box) {
        // Si le calque n'existe pas (ex: map sans murs), on ne bloque rien
        if (collisionLayer == null) return false;

        // On convertit les coordonnées du monde (float) en coordonnées de grille (int)
        // Comme 1 unité = 1 tuile, un simple cast (int) suffit.

        int minX = (int) box.x;
        int maxX = (int) (box.x + box.width - 0.01f); // -0.01 pour ne pas bloquer si on touche juste le bord de la tuile suivante
        int minY = (int) box.y;
        int maxY = (int) (box.y + box.height - 0.01f);

        // On boucle sur toutes les tuiles touchées par le rectangle
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {

                // On récupère la cellule à cette position (x, y)
                TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);

                // Si la cellule existe (c'est-à-dire qu'il y a une tuile peinte sur le calque Collision),
                // alors c'est un obstacle.
                if (cell != null) {
                    return true;
                }
            }
        }

        // Aucune tuile trouvée sous le rectangle -> c'est libre
        return false;
    }
}
