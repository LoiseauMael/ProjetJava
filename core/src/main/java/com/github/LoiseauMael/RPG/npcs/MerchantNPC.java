package com.github.LoiseauMael.RPG.npcs;

public class MerchantNPC extends NPC {

    public MerchantNPC(float x, float y, String[] dialogues) {
        // Appelle le constructeur parent avec le chemin de la spritesheet
        super(x, y, "Marchand", dialogues, "MerchantSpriteSheet.png");
    }

    // Pas besoin de surcharger update/draw, la classe NPC s'en occupe
}
