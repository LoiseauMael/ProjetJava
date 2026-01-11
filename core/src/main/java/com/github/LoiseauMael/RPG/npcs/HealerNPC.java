package com.github.LoiseauMael.RPG.npcs;

import com.github.LoiseauMael.RPG.Entity;

public class HealerNPC extends NPC {

    public HealerNPC(float x, float y, String texturePath, String[] dialogues) {
        // On passe le chemin de la texture au constructeur parent (NPC)
        // Valeur par défaut de sécurité si texturePath est null
        super(x, y, "Guérisseur", dialogues,
            (texturePath != null ? texturePath : "assets/HealerSpriteSheet.png"));
    }

    // Logique spécifique : interaction pour soigner
    @Override
    public boolean advanceDialogue() {
        boolean hasMore = super.advanceDialogue();

        // Si le dialogue est fini, on soigne le joueur (Exemple simple)
        if (!hasMore) {
            // Tu pourras ajouter ici la logique : game.player.healWrapper();
            System.out.println("Le guérisseur a soigné vos blessures !");
        }
        return hasMore;
    }
}
