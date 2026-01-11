package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;

public class AttackAction extends BattleAction {

    private int apCost;
    private boolean isRegenAttack;

    // 1. Constructeur pour le JOUEUR (Attaque de base)
    // -> Coût 0 PA, Régénère 2 PA
    public AttackAction() {
        super("Attaque", "Coup basique (+2 PA)", 1);
        this.apCost = 0;
        this.isRegenAttack = true;
    }

    // 2. Constructeur pour les ENNEMIS (Attaques personnalisées)
    // -> Coût 2 PA (standard), Pas de régénération spéciale
    // Ce constructeur est celui requis par NormalEnemy, EliteEnemy, etc.
    public AttackAction(String name, String description, float range) {
        super(name, description, range);
        this.apCost = 2;
        this.isRegenAttack = false;
    }

    @Override
    public int getAPCost() {
        return apCost;
    }

    @Override
    public int getMPCost() {
        return 0;
    }

    @Override
    public boolean canExecute(Fighter user) {
        // On vérifie si l'utilisateur a assez de PA (sera toujours vrai pour le joueur car coût = 0)
        return user.getPA() >= apCost;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // Consommation (0 pour le joueur, 2 pour les ennemis)
        user.consumePA(apCost);

        // Régénération (Seulement pour l'attaque de base du joueur)
        if (isRegenAttack) {
            user.regenPA(2);
            BattleSystem.addLog(user.getName() + " récupère 2 PA.");
        }

        // Calcul des dégâts
        int damage = Math.max(1, user.getFOR() - target.getDEF());
        target.takeDamage(damage);

        // Logs
        BattleSystem.addLog(user.getName() + " utilise " + getName() + " !");
        BattleSystem.addLog("Inflige " + damage + " dégâts.");
    }
}
