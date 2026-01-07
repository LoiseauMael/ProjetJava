package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;

public class EnergyPotion extends Item {
    private int energyAmount;

    public EnergyPotion(String name, String description, int energyAmount) {
        super(name, description);
        this.energyAmount = energyAmount;
        this.count = 1;
    }

    @Override
    public void use(Fighter target) {
        // CORRECTION ICI : On appelle la nouvelle méthode créée dans Fighter
        target.restorePA(energyAmount);
        this.count--;
    }

    public int getAmount() {
        return energyAmount;
    }
}
