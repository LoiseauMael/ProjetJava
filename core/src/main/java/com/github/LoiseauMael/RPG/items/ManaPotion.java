package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;

public class ManaPotion extends Consumable {
    private int manaAmount;

    public ManaPotion(String name, String description, int manaAmount) {
        super(name, description);
        this.manaAmount = manaAmount;
    }

    @Override
    protected void applyEffect(Fighter target) {
        // Logique de soin PM
        target.setPM(target.getPM() + manaAmount);
        Gdx.app.log("Inventaire", target.getClass().getSimpleName() + " récupère " + manaAmount + " PM.");
    }
}
