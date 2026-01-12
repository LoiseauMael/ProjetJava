package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.model.entities.Fighter;

/**
 * Représente une attaque ultime dévastatrice.
 * <p>
 * Caractéristiques :
 * <ul>
 * <li>Coût très élevé en Mana (PM).</li>
 * <li>Multiplicateur de dégâts massif (x3).</li>
 * <li>Portée élevée.</li>
 * </ul>
 */
public class UltimateAction extends BattleAction {

    /**
     * @param name Nom de l'attaque ultime.
     */
    public UltimateAction(String name) {
        super(name, "Attaque Ultime (Dégâts x3) !", 5.0f);
    }

    @Override
    public int getAPCost() { return 0; }

    @Override
    public int getMPCost() { return 20; }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPM() >= getMPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        user.consumePM(getMPCost());

        // Dégâts = (Force * 3) - Défense
        int damage = (user.getFOR() * 3) - target.getDEF();
        if (damage < 1) damage = 1;

        target.takeDamage(damage);

        BattleSystem.addLog(">>> " + user.getName() + " lance l'ULTIME " + getName() + " !!! <<<");
        BattleSystem.addLog("CRITIQUE ! " + damage + " degats infliges !");
    }
}
