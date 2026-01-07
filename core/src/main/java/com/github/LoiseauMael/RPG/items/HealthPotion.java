package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;

public class HealthPotion extends Item {
    private int healAmount;

    public HealthPotion(String name, String description, int healAmount) {
        super(name, description);
        this.healAmount = healAmount;
        this.count = 1;
    }

    @Override
    public void use(Fighter target) {
        target.heal(healAmount);
        this.count--;
    }

    // --- AJOUTEZ CETTE MÉTHODE ---
    public int getAmount() {
        return healAmount;
    }
}
