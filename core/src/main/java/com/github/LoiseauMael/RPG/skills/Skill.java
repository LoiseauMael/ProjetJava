package com.github.LoiseauMael.RPG.skills;

import com.github.LoiseauMael.RPG.Fighter;
import com.github.LoiseauMael.RPG.battle.BattleAction;
import com.github.LoiseauMael.RPG.battle.BattleSystem;

public class Skill extends BattleAction {

    public enum SkillType { ART, MAGIC }
    public enum EffectType { DAMAGE, HEAL_HP, HEAL_MP, HEAL_PA, BUFF_STR, BUFF_DEF }
    public enum TargetType { SELF, ENEMY }

    // Données chargées depuis le JSON
    public String id;
    public SkillType type;
    public EffectType effectType;
    public TargetType targetType;
    public int cost; // Coût en PA (Art) ou PM (Magie)
    public float power; // Dégâts (fixe ou multiplicateur), Soin, ou valeur de Buff
    public int requiredLevel;
    public String requiredClass; // "Guerrier", "Mage", ou "ANY"

    public Skill() {
        super("", "", 0); // Constructeur vide pour le JSON Loader
    }

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

    @Override
    public void execute(Fighter user, Fighter target) {
        // Consommation des ressources
        if (type == SkillType.ART) user.consumePA(cost);
        if (type == SkillType.MAGIC) user.consumePM(cost);

        // Application de l'effet
        switch (effectType) {
            case DAMAGE:
                int dmg = 0;
                if (type == SkillType.ART) {
                    // Pour les arts, power est souvent un multiplicateur (ex: 1.5x Force)
                    dmg = (int)(user.getFOR() * power) - target.getDEF();
                } else {
                    // Pour la magie, power est souvent une valeur fixe ajoutée à la FORM
                    dmg = (int)(user.getFORM() + power) - target.getDEFM();
                }
                if (dmg < 1) dmg = 1;
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
                // Simplification : Buff permanent pour le combat (à reset à la fin du combat idéalement)
                // Dans une version avancée, il faudrait une liste de Buffs temporaires dans Fighter
                int buffStr = (int)power;
                target.setStats(target.getFOR() + buffStr, target.getDEF(), target.getFORM(), target.getDEFM(), target.getVIT());
                BattleSystem.addLog(user.getName() + " augmente sa Force de " + buffStr + " !");
                break;

            // Ajoutez d'autres cas (BUFF_DEF, etc.)
        }
    }
}
