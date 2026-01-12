package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Potion de soin (PV).
 * <p>
 * Restaure une quantité fixe de Points de Vie lorsqu'elle est utilisée.
 * Ne peut pas être utilisée si le joueur a déjà tous ses PV.
 */
public class HealthPotion extends Consumable {
    private int amount;

    /**
     * @param name Nom de la potion.
     * @param description Description.
     * @param amount Quantité de PV rendus.
     */
    public HealthPotion(String name, String description, int amount) {
        super(name, description);
        this.amount = amount;
    }

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
