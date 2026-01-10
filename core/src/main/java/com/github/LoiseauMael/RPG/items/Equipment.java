package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public abstract class Equipment extends Item {

    // Si null, tout le monde peut l'équiper. Sinon, seul ce type de joueur (ou sous-classe) le peut.
    protected Class<? extends Player> requiredClass;

    public Equipment(String name, String description, Class<? extends Player> requiredClass) {
        super(name, description);
        this.requiredClass = requiredClass;
    }

    /**
     * Vérifie si le joueur passé en paramètre a la bonne classe pour cet objet.
     */
    public boolean canEquip(Player p) {
        if (requiredClass == null) return true;
        return requiredClass.isInstance(p);
    }

    @Override
    public boolean use(Player player) {
        if (canEquip(player)) {
            player.equip(this);
            // On renvoie false pour ne pas "consommer" (détruire) l'objet comme une potion.
            // Il reste dans l'inventaire mais est marqué comme équipé via la référence dans Player.
            return false;
        } else {
            System.out.println("Impossible d'équiper : Mauvaise classe !");
            return false;
        }
    }

    public Class<? extends Player> getRequiredClass() { return requiredClass; }
}
