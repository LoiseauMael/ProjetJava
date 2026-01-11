package com.github.LoiseauMael.RPG.npcs;

public class SimpleNPC extends NPC {

    public SimpleNPC(float x, float y, String name, String texturePath, String[] dialogues) {
        // On passe les infos au parent.
        // Si texturePath est null, on met un sprite par défaut pour éviter le crash.
        super(x, y,
            (name != null ? name : "Villageois"),
            dialogues,
            (texturePath != null ? texturePath : "assets/VillagerSpriteSheet.png"));
    }

    // Pas besoin de surcharge de update/draw si la classe NPC s'en occupe déjà
}
