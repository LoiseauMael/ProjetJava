package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;

public abstract class Consumable extends Item {

    public Consumable(String name, String description) {
        super(name, description);
    }

    @Override
    public void use(Fighter target) {
        if (count > 0) {
            applyEffect(target);
            count--; // La consommation se fait ici automatiquement
        }
    }

    // Chaque type de potion définira son effet ici
    protected abstract void applyEffect(Fighter target);
}
