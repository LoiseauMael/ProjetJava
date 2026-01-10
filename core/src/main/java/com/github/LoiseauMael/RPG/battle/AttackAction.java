package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;

public class AttackAction extends BattleAction {

    public AttackAction() {
        // Nom, Description, Portée fixe à 2 comme demandé
        super("Attaque", "Coup basique (Portée 2)", 2);
    }

    // Constructeur pour les ennemis qui voudraient changer la portée
    public AttackAction(String name, String description, float range) {
        super(name, description, range);
    }

    @Override
    public int getAPCost() {
        return 2;
    }

    @Override
    public int getMPCost() {
        return 0;
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPA() >= getAPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        user.consumePA(getAPCost());

        // Calcul simple des dégâts
        int damage = Math.max(1, user.getFOR() - target.getDEF());
        target.takeDamage(damage);

        BattleSystem.addLog(user.getName() + " attaque et inflige " + damage + " dgts !");
    }
}
