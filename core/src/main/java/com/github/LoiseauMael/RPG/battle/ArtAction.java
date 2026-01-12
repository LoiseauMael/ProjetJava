package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.model.entities.Fighter;

/**
 * Représente une technique physique (Art martial ou d'arme).
 * <p>
 * Caractéristiques :
 * <ul>
 * <li>Coût élevé en PA (Points d'Action), pas de coût en Mana.</li>
 * <li>Dégâts basés sur un multiplicateur de la Force physique (FOR).</li>
 * </ul>
 */
public class ArtAction extends BattleAction {

    private int paCost;
    private float multiplier;

    /**
     * @param name Nom de la technique.
     * @param paCost Coût en PA.
     * @param multiplier Multiplicateur de dégâts (ex: 1.5f pour +50% de dégâts).
     */
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
        user.consumePA(getAPCost());

        // Dégâts = (Force * Multiplicateur) - Défense
        int damage = (int)(user.getFOR() * multiplier) - target.getDEF();
        if (damage < 1) damage = 1;

        target.takeDamage(damage);

        BattleSystem.addLog(user.getName() + " utilise " + getName() + " : " + damage + " degats !");
    }
}
