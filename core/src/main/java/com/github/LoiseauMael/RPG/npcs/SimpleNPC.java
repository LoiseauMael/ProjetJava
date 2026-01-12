package com.github.LoiseauMael.RPG.npcs;

/**
 * PNJ générique utilisé pour les villageois sans fonction spéciale.
 * Instancié principalement via le chargement de carte (MapLoader).
 */
public class SimpleNPC extends NPC {

    /**
     * Crée un villageois simple.
     *
     * @param x Position X.
     * @param y Position Y.
     * @param name Nom affiché (défaut : "Villageois").
     * @param texturePath Chemin de la texture (défaut : "assets/VillagerSpriteSheet.png").
     * @param dialogues Liste des phrases.
     */
    public SimpleNPC(float x, float y, String name, String texturePath, String[] dialogues) {
        // Appelle le constructeur parent avec des valeurs de repli (fallback) sécurisées
        super(x, y,
            (name != null ? name : "Villageois"),
            dialogues,
            (texturePath != null ? texturePath : "assets/VillagerSpriteSheet.png"));
    }
}
