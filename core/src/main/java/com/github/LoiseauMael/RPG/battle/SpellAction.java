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
    public int getAPCost() {
        return 2; // Coût fixe en PA pour un sort
    }

    @Override
    public int getMPCost() {
        return mpCost;
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPM() >= getMPCost() && user.getPA() >= getAPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // 1. Payer les coûts
        user.restoreMana(-getMPCost());
        user.restorePA(-getAPCost());

        // 2. Calcul Dégâts Magiques
        int damage = (user.getFORM() + power) - target.getDEFM();

        if (damage < 1) damage = 1;

        // 3. Appliquer les dégâts
        target.takeDamage(damage);

        // --- NOUVEAU LOG ---
        BattleSystem.addLog(user.getClass().getSimpleName() + " lance " + getName() + " : " + damage + " degats magiques !");
    }
}
