package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;

public class ManaPotion extends Item {
    private int manaAmount;

    public ManaPotion(String name, String description, int manaAmount) {
        super(name, description);
        this.manaAmount = manaAmount;
        this.count = 1;
    }

    @Override
    public void use(Fighter target) {
        target.restoreMana(manaAmount);
        this.count--;
    }

    // --- AJOUTEZ CETTE MÉTHODE ---
    public int getAmount() {
        return manaAmount;
    }
}
