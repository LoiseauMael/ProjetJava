package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.model.entities.Fighter;

/**
 * Représente un sort magique offensif.
 * <p>
 * Caractéristiques :
 * <ul>
 * <li>Coût en Mana (PM) et en PA (généralement 2).</li>
 * <li>Dégâts basés sur la Force Magique (FORM) de l'utilisateur et la Défense Magique (DEFM) de la cible.</li>
 * <li>Portée généralement élevée (ex: 4 cases).</li>
 * </ul>
 */
public class SpellAction extends BattleAction {

    private int power;
    private int mpCost;

    /**
     * @param name Nom du sort.
     * @param power Puissance ajoutée à la FORM.
     * @param mpCost Coût en Mana.
     */
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
        user.consumePM(getMPCost());
        user.consumePA(getAPCost());

        // Dégâts = (FORM + PuissanceSort) - DEFM Cible
        int damage = (user.getFORM() + power) - target.getDEFM();
        if (damage < 1) damage = 1;

        target.takeDamage(damage);

        BattleSystem.addLog(user.getName() + " lance " + getName() + " : " + damage + " degats magiques !");
    }
}
