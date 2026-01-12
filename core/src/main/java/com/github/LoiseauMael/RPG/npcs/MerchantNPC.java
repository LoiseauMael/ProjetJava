package com.github.LoiseauMael.RPG.npcs;

/**
 * Type spécifique de PNJ qui ouvre une boutique à la fin de son dialogue.
 */
public class MerchantNPC extends NPC {

    /**
     * Crée un Marchand.
     * Utilise par défaut "assets/MerchantSpriteSheet.png".
     */
    public MerchantNPC(float x, float y, String texturePath, String[] dialogues) {
        super(x, y, "Marchand", dialogues,
            (texturePath != null ? texturePath : "assets/MerchantSpriteSheet.png"));
    }

    /**
     * Gère l'avancement du dialogue et déclenche l'ouverture du shop à la fin.
     */
    @Override
    public boolean advanceDialogue() {
        boolean hasMore = super.advanceDialogue();

        if (!hasMore) {
            System.out.println("Ouverture de la boutique...");
            // TODO: Connecter ceci au ShopState via le gestionnaire d'états
            // game.changeState(game.shopState);
        }
        return hasMore;
    }
}
