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
    public int getAPCost() {
        return paCost;
    }

    @Override
    public int getMPCost() {
        return 0; // Pas de coût en mana pour les arts physiques
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPA() >= getAPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // 1. Payer le coût en PA (on utilise restorePA avec une valeur négative)
        user.restorePA(-getAPCost());

        // 2. Calcul des dégâts : (Force * Multiplicateur) - Défense ennemie
        int damage = (int)(user.getFOR() * multiplier) - target.getDEF();

        if (damage < 1) damage = 1;

        // 3. Appliquer les dégâts
        target.takeDamage(damage);

        // --- NOUVEAU LOG ---
        BattleSystem.addLog(user.getClass().getSimpleName() + " utilise " + getName() + " et inflige " + damage + " degats !");
    }
}
