package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;

public class EnergyPotion extends Consumable {
    private int paAmount;

    public EnergyPotion(String name, String description, int paAmount) {
        super(name, description);
        this.paAmount = paAmount;
    }

    @Override
    protected void applyEffect(Fighter target) {
        // Logique de soin PA
        target.setPA(target.getPA() + paAmount);
        Gdx.app.log("Inventaire", target.getClass().getSimpleName() + " récupère " + paAmount + " PA.");
    }
}
