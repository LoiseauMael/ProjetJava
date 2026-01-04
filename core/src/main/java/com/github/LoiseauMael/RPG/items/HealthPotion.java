package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;

public class HealthPotion extends Consumable {
    private int healAmount;

    public HealthPotion(String name, String description, int healAmount) {
        super(name, description);
        this.healAmount = healAmount;
    }

    @Override
    protected void applyEffect(Fighter target) {
        // Logique de soin PV
        target.setPV(target.getPV() + healAmount);
        Gdx.app.log("Inventaire", target.getClass().getSimpleName() + " récupère " + healAmount + " PV.");
    }
}
