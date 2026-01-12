package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.LoiseauMael.RPG.model.entities.Entity;

/**
 * Entité invisible représentant une zone de sortie/transition vers une autre carte.
 * <p>
 * Elle hérite de {@link Entity} pour bénéficier du système de collision, mais ne s'affiche pas.
 * Lorsqu'elle est touchée par le joueur, le système d'exploration déclenche le changement de map.
 */
public class MapExit extends Entity {

    private String targetMap;
    private float targetX, targetY;

    /**
     * Crée une zone de sortie.
     *
     * @param x Position X (coin bas-gauche).
     * @param y Position Y (coin bas-gauche).
     * @param width Largeur de la zone sensible.
     * @param height Hauteur de la zone sensible.
     * @param targetMap Nom du fichier de la map cible (ex: "map2.tmx").
     * @param tX Position X d'arrivée sur la nouvelle map.
     * @param tY Position Y d'arrivée sur la nouvelle map.
     */
    public MapExit(float x, float y, float width, float height, String targetMap, float tX, float tY) {
        super(x, y, null); // Pas de sprite
        this.targetMap = targetMap;
        this.targetX = tX;
        this.targetY = tY;

        // On définit manuellement la hitbox correspondant à la taille de l'objet Tiled
        this.setCollisionBounds(width, height, 0, 0);
    }

    /**
     * Surcharge de la méthode de dessin pour ne RIEN afficher.
     * L'entité existe physiquement mais est invisible.
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Invisible
    }

    public String getTargetMap() { return targetMap; }
    public float getTargetX() { return targetX; }
    public float getTargetY() { return targetY; }
}
