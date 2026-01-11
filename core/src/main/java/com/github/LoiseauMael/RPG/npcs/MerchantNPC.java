package com.github.LoiseauMael.RPG.npcs;

public class MerchantNPC extends NPC {

    public MerchantNPC(float x, float y, String texturePath, String[] dialogues) {
        super(x, y, "Marchand", dialogues,
            (texturePath != null ? texturePath : "assets/MerchantSpriteSheet.png"));
    }

    @Override
    public boolean advanceDialogue() {
        boolean hasMore = super.advanceDialogue();

        // À la fin du dialogue, on pourrait ouvrir le ShopState
        if (!hasMore) {
            System.out.println("Ouverture de la boutique...");
            // game.changeState(game.shopState); // Nécessiterait une référence au jeu
        }
        return hasMore;
    }
}
