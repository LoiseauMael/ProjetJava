package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;
import com.github.LoiseauMael.RPG.Player;
import com.badlogic.gdx.Gdx;

public abstract class Equipment extends Item {

    // La classe Java requise (ex: SwordMan.class). Si null, tout le monde peut l'équiper.
    protected Class<? extends Player> requiredClass;

    public Equipment(String name, String description, Class<? extends Player> requiredClass) {
        super(name, description);
        this.requiredClass = requiredClass;
    }

    /**
     * Vérifie si le joueur a la bonne classe pour cet objet.
     */
    public boolean canEquip(Player player) {
        if (requiredClass == null) return true; // Universel
        return requiredClass.isInstance(player); // Vérifie si 'player' est une instance de 'requiredClass'
    }

    // --- AJOUT POUR LE SAVEMANAGER ---
    public Class<? extends Player> getRequiredClass() {
        return requiredClass;
    }

    @Override
    public void use(Fighter target) {
        Gdx.app.log("Equipement", "Cet objet doit être équipé via le menu.");
    }
}
