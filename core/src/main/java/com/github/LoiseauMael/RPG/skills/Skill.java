package com.github.LoiseauMael.RPG.skills;

import com.github.LoiseauMael.RPG.model.entities.Fighter;
import com.github.LoiseauMael.RPG.battle.BattleAction;
import com.github.LoiseauMael.RPG.battle.BattleSystem;

/**
 * Représente une compétence (Sort ou Art Martial).
 * <p>
 * Hérite de {@link BattleAction} pour s'intégrer dans le système de combat tour par tour.
 * Les données de la compétence sont généralement chargées depuis un fichier JSON.
 */
public class Skill extends BattleAction {

    public enum SkillType {
        ART,  // Compétence physique (utilise PA, basé sur FOR)
        MAGIC // Compétence magique (utilise PM, basé sur FORM)
    }

    public enum EffectType {
        DAMAGE,    // Inflige des dégâts
        HEAL_HP,   // Rend des PV
        HEAL_MP,   // Rend des PM
        HEAL_PA,   // Rend des PA
        BUFF_STR,  // Augmente la Force
        BUFF_DEF   // Augmente la Défense
    }

    public enum TargetType {
        SELF,  // Cible le lanceur (Soins, Buffs)
        ENEMY  // Cible l'adversaire (Attaques)
    }

    // --- Données mappées depuis le JSON ---
    public String id;
    public SkillType type;
    public EffectType effectType;
    public TargetType targetType;

    /** Coût en ressource (PA pour ART, PM pour MAGIC). */
    public int cost;

    /** Puissance de l'effet (Dégâts bruts, Multiplicateur, ou Montant de soin). */
    public float power;

    public int requiredLevel;
    /** Classe requise pour apprendre le skill ("Guerrier", "Mage" ou "ANY"). */
    public String requiredClass;

    public Skill() {
        super("", "", 0); // Constructeur vide requis pour le JSON Loader
    }

    // --- Surcharges BattleAction ---

    @Override
    public int getAPCost() {
        return type == SkillType.ART ? cost : 0;
    }

    @Override
    public int getMPCost() {
        return type == SkillType.MAGIC ? cost : 0;
    }

    @Override
    public boolean canExecute(Fighter user) {
        if (type == SkillType.ART) return user.getPA() >= cost;
        if (type == SkillType.MAGIC) return user.getPM() >= cost;
        return false;
    }

    /**
     * Exécute la logique de la compétence.
     * <ol>
     * <li>Consomme les ressources (PM/PA).</li>
     * <li>Calcule l'effet selon {@link EffectType} et les stats du lanceur.</li>
     * <li>Applique l'effet à la cible.</li>
     * <li>Log l'action dans la console de combat.</li>
     * </ol>
     */
    @Override
    public void execute(Fighter user, Fighter target) {
        // 1. Consommation
        if (type == SkillType.ART) user.consumePA(cost);
        if (type == SkillType.MAGIC) user.consumePM(cost);

        // 2. Application de l'effet
        switch (effectType) {
            case DAMAGE:
                int dmg = 0;
                if (type == SkillType.ART) {
                    // Formule Physique : (Force * Puissance) - Défense Cible
                    dmg = (int)(user.getFOR() * power) - target.getDEF();
                } else {
                    // Formule Magique : (ForceMagique + Puissance) - DéfenseMagique Cible
                    dmg = (int)(user.getFORM() + power) - target.getDEFM();
                }
                if (dmg < 1) dmg = 1; // Dégâts minimums garantis
                target.takeDamage(dmg);
                BattleSystem.addLog(user.getName() + " utilise " + name + " : " + dmg + " dégâts !");
                break;

            case HEAL_HP:
                target.heal((int)power);
                BattleSystem.addLog(user.getName() + " soigne " + (int)power + " PV.");
                break;

            case HEAL_MP:
                target.regenPM((int)power);
                BattleSystem.addLog(user.getName() + " régénère " + (int)power + " PM.");
                break;

            case HEAL_PA:
                target.regenPA((int)power);
                BattleSystem.addLog(user.getName() + " régénère " + (int)power + " PA.");
                break;

            case BUFF_STR:
                // Note : Ce buff est permanent jusqu'à la fin du combat ou rechargement.
                // Une amélioration serait d'avoir un système de StatusEffect temporaire.
                int buffStr = (int)power;
                target.setStats(target.getFOR() + buffStr, target.getDEF(), target.getFORM(), target.getDEFM(), target.getVIT());
                BattleSystem.addLog(user.getName() + " augmente sa Force de " + buffStr + " !");
                break;
        }
    }
}
