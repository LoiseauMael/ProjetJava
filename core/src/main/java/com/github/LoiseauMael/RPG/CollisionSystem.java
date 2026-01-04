package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class CollisionSystem {

    private final Array<Rectangle> walls;
    private static final String COLLISION_LAYER_NAME = "Collision";
    private static final float TILE_SIZE = 1.0f;

    public CollisionSystem(TiledMap map) {
        this.walls = new Array<>();

        if (map == null) throw new IllegalArgumentException("La TiledMap ne peut pas être nulle.");

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(COLLISION_LAYER_NAME);

        if (layer == null) {
            Gdx.app.error("CollisionSystem", "Le calque de TUILES '" + COLLISION_LAYER_NAME + "' est introuvable.");
            return;
        }

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    walls.add(new Rectangle(x, y, TILE_SIZE, TILE_SIZE));
                }
            }
        }
        Gdx.app.log("CollisionSystem", "Scan terminé : " + walls.size + " murs.");
    }

    /**
     * Vérifie uniquement si on touche un mur.
     */
    public boolean checkCollision(Rectangle newBounds) {
        for (Rectangle wall : walls) {
            if (newBounds.overlaps(wall)) {
                return true;
            }
        }
        return false;
    }
}
