package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.model.entities.Fighter;

/**
 * Action d'attaque physique standard.
 * <p>
 * Comportement double :
 * <ul>
 * <li>Pour le <b>Joueur</b> : Coûte 0 PA et régénère 2 PA.</li>
 * <li>Pour les <b>Ennemis</b> : Coûte des PA (généralement 2) et ne régénère rien.</li>
 * </ul>
 */
public class AttackAction extends BattleAction {

    private int apCost;
    private boolean isRegenAttack;

    /**
     * Constructeur pour le JOUEUR (Attaque par défaut).
     * Coût 0 PA, Régénère 2 PA.
     */
    public AttackAction() {
        super("Attaque", "Coup basique (+2 PA)", 1);
        this.apCost = 0;
        this.isRegenAttack = true;
    }

    /**
     * Constructeur pour les ENNEMIS (Attaques nommées).
     * Coût PA standard (2), pas de régénération.
     *
     * @param name Nom de l'attaque.
     * @param description Description.
     * @param range Portée.
     */
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
        // On vérifie si l'utilisateur a assez de PA
        return user.getPA() >= apCost;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // Consommation (0 pour le joueur, X pour les ennemis)
        user.consumePA(apCost);

        // Régénération (Seulement pour l'attaque de base du joueur)
        if (isRegenAttack) {
            user.regenPA(2);
            BattleSystem.addLog(user.getName() + " récupère 2 PA.");
        }

        // Calcul des dégâts : (Force Attaquant - Défense Cible)
        int damage = Math.max(1, user.getFOR() - target.getDEF());
        target.takeDamage(damage);

        // Logs
        BattleSystem.addLog(user.getName() + " utilise " + getName() + " !");
        BattleSystem.addLog("Inflige " + damage + " dégâts.");
    }
}
