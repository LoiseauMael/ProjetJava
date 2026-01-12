package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Classe abstraite pour les objets équipables (Armes, Armures, Reliques).
 * <p>
 * Gère les restrictions de classe (ex: Une épée lourde réservée au Guerrier).
 * Lorsqu'on "utilise" un équipement depuis l'inventaire, on tente de l'équiper.
 */
public abstract class Equipment extends Item {

    /** Classe requise pour équiper cet objet (null = aucune restriction). */
    protected Class<? extends Player> requiredClass;

    /**
     * @param name Nom de l'équipement.
     * @param description Description.
     * @param requiredClass Classe du joueur requise (ex: Wizard.class). Si null, tout le monde peut l'équiper.
     */
    public Equipment(String name, String description, Class<? extends Player> requiredClass) {
        super(name, description);
        this.requiredClass = requiredClass;
    }

    /**
     * Vérifie si le joueur possède la classe requise pour cet équipement.
     * @param p Le joueur.
     * @return true si l'équipement est compatible.
     */
    public boolean canEquip(Player p) {
        if (requiredClass == null) return true;
        return requiredClass.isInstance(p);
    }

    @Override
    public boolean use(Player player) {
        if (canEquip(player)) {
            player.equip(this);
            // On renvoie false pour ne pas "consommer" (détruire) l'objet.
            return false;
        } else {
            System.out.println("Impossible d'équiper : Mauvaise classe !");
            return false;
        }
    }

    public Class<? extends Player> getRequiredClass() { return requiredClass; }
}
