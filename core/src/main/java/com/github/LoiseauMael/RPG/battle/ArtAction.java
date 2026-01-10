package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;

public class ArtAction extends BattleAction {

    private int paCost;
    private float multiplier;

    public ArtAction(String name, int paCost, float multiplier) {
        super(name, "Technique physique (x" + multiplier + ")", 1.5f);
        this.paCost = paCost;
        this.multiplier = multiplier;
    }

    @Override
    public int getAPCost() { return paCost; }

    @Override
    public int getMPCost() { return 0; }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPA() >= getAPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // CORRECTION : Utilisation de consumePA
        user.consumePA(getAPCost());

        int damage = (int)(user.getFOR() * multiplier) - target.getDEF();
        if (damage < 1) damage = 1;

        target.takeDamage(damage);

        BattleSystem.addLog(user.getName() + " utilise " + getName() + " : " + damage + " degats !");
    }
}
