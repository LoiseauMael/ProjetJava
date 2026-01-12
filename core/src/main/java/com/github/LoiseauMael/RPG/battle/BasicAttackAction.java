package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.model.entities.Fighter;

/**
 * Représente l'attaque de base spécifique au Joueur.
 * <p>
 * Particularité : Elle ne coûte pas de PA mais en <b>régénère 2</b> à l'utilisation.
 * Permet au joueur de recharger ses points d'action pour lancer des sorts plus tard.
 */
public class BasicAttackAction extends BattleAction {

    public BasicAttackAction() {
        super("Attaque", "Coup simple (+2 PA)", 1.5f);
    }

    @Override
    public int getAPCost() { return 0; }

    @Override
    public int getMPCost() { return 0; }

    @Override
    public boolean canExecute(Fighter user) { return true; }

    @Override
    public void execute(Fighter user, Fighter target) {
        // Régénération de PA spécifique à cette action
        user.regenPA(2);

        int damage = user.getFOR() - target.getDEF();
        if (damage < 1) damage = 1;
        target.takeDamage(damage);

        BattleSystem.addLog(user.getName() + " attaque : " + damage + " dégâts (+2 PA)");
    }
}
