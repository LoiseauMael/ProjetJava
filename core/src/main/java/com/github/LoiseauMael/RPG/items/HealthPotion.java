package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

// On étend Consumable (qui étend Item)
public class HealthPotion extends Consumable {
    private int amount;

    public HealthPotion(String name, String description, int amount) {
        super(name, description);
        this.amount = amount;
    }

    // CORRECTION : On renvoie un boolean (Vrai = consommé, Faux = annulé)
    @Override
    public boolean use(Player player) {
        // Si la vie est pleine, on ne boit pas la potion
        if (player.getPV() >= player.getMaxPV()) {
            return false;
        }

        player.heal(amount);
        return true;
    }

    public int getAmount() { return amount; }
}
