package com.github.LoiseauMael.RPG.npcs;

import com.github.LoiseauMael.RPG.model.entities.Entity;

/**
 * Type spécifique de PNJ capable de soigner le joueur à la fin de son dialogue.
 */
public class HealerNPC extends NPC {

    /**
     * Crée un Guérisseur.
     * Utilise par défaut "assets/HealerSpriteSheet.png" si aucune texture n'est fournie.
     */
    public HealerNPC(float x, float y, String texturePath, String[] dialogues) {
        super(x, y, "Guérisseur", dialogues,
            (texturePath != null ? texturePath : "assets/HealerSpriteSheet.png"));
    }

    /**
     * Gère l'avancement du dialogue et déclenche le soin à la fin.
     *
     * @return true si le dialogue continue, false s'il est fini.
     */
    @Override
    public boolean advanceDialogue() {
        boolean hasMore = super.advanceDialogue();

        // Si le dialogue se termine (return false), on déclenche l'effet
        if (!hasMore) {
            // Note : L'implémentation réelle nécessiterait une référence au Player ou au GameState
            // Exemple : Game.player.heal(999);
            System.out.println("Le guérisseur a soigné vos blessures !");
        }
        return hasMore;
    }
}
