package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Potion d'Énergie (PA).
 * <p>
 * Restaure des Points d'Action immédiatement.
 * Permet de prolonger un tour de combat ou d'enchaîner plus d'actions.
 */
public class EnergyPotion extends Item {

    private int amount;

    public EnergyPotion(String name, String description, int amount) {
        super(name, description);
        this.amount = amount;
    }

    @Override
    public boolean use(Player player) {
        if (player.getPA() < player.getMaxPA()) {
            player.regenPA(amount);
            System.out.println("Vous utilisez " + getName() + " et recuperez " + amount + " PA.");
            return true;
        }

        System.out.println("Energie (PA) deja au max !");
        return false;
    }

    public int getAmount() {
        return amount;
    }
}
