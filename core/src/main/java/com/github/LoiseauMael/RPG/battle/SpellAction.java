package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;

public class SpellAction extends BattleAction {

    private int power;
    private int mpCost;

    public SpellAction(String name, int power, int mpCost) {
        super(name, "Sort magique (Puissance: " + power + ")", 4.0f);
        this.power = power;
        this.mpCost = mpCost;
    }

    @Override
    public int getAPCost() { return 2; }

    @Override
    public int getMPCost() { return mpCost; }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPM() >= getMPCost() && user.getPA() >= getAPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // CORRECTION : Utilisation de consumePM et consumePA
        user.consumePM(getMPCost());
        user.consumePA(getAPCost());

        int damage = (user.getFORM() + power) - target.getDEFM();
        if (damage < 1) damage = 1;

        target.takeDamage(damage);

        BattleSystem.addLog(user.getName() + " lance " + getName() + " : " + damage + " degats magiques !");
    }
}
