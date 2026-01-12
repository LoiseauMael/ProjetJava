package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Potion de Mana (PM).
 * <p>
 * Restaure une quantité fixe de Points de Mana.
 * Utile pour les Mages ou pour lancer des compétences coûteuses.
 */
public class ManaPotion extends Item {

    private int amount;

    public ManaPotion(String name, String description, int amount) {
        super(name, description);
        this.amount = amount;
    }

    @Override
    public boolean use(Player player) {
        if (player.getPM() < player.getMaxPM()) {
            player.regenPM(amount);
            System.out.println("Vous utilisez " + getName() + " et recuperez " + amount + " PM.");
            return true;
        }

        System.out.println("PM deja au max !");
        return false;
    }

    public int getAmount() {
        return amount;
    }
}
