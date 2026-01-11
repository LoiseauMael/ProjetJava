package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.LoiseauMael.RPG.skills.Skill;
import com.github.LoiseauMael.RPG.skills.SkillManager;

/**
 * Classe de base pour toutes les entités capables de combattre (Joueur et Ennemis).
 * Gère les statistiques, les ressources et la progression.
 */
public abstract class Fighter extends Entity implements Disposable {

    // --- STATISTIQUES DE RESSOURCES ---
    protected int maxPV, PV;
    protected int maxPM, PM; // Mana (utilisé pour les Sorts)
    protected int maxPA, PA; // Points d'Action / Énergie (utilisé pour les Arts/Déplacements)

    // --- STATISTIQUES DE COMBAT ---
    protected int FOR, DEF, FORM, DEFM, VIT;
    protected int DEP; // Capacité de déplacement (nombre de cases en combat tactique)

    // --- PROGRESSION & IDENTITÉ ---
    protected int money;
    protected int exp;
    protected int level;
    protected String nom;

    // --- COMPÉTENCES ---
    protected Array<Skill> knownSkills = new Array<>();

    public Fighter(float x, float y, int level, int exp, int PV, int PM, int PA,
                   int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, Sprite sprite) {
        super(x, y, sprite);

        this.level = level;
        this.exp = exp;

        this.maxPV = PV; this.PV = PV;
        this.maxPM = PM; this.PM = PM;
        this.maxPA = PA; this.PA = PA;

        this.FOR = FOR;
        this.DEF = DEF;
        this.FORM = FORM;
        this.DEFM = DEFM;
        this.VIT = VIT;
        this.DEP = DEP;
    }

    // --- GESTION DES SKILLS ---

    /**
     * Vérifie dans le SkillManager si de nouveaux sorts sont disponibles pour la classe et le niveau actuels.
     */
    public void updateKnownSkills() {
        if (this.nom == null) return;

        Array<Skill> available = SkillManager.getSkillsFor(this.nom, this.level);

        for(Skill s : available) {
            boolean alreadyKnown = false;
            for(Skill k : knownSkills) {
                if(k.id.equals(s.id)) {
                    alreadyKnown = true;
                    break;
                }
            }

            if(!alreadyKnown) {
                knownSkills.add(s);
                System.out.println(this.nom + " a appris : " + s.getName());
            }
        }
    }

    public Array<Skill> getSkillsByType(Skill.SkillType type) {
        Array<Skill> filtered = new Array<>();
        for(Skill s : knownSkills) {
            if(s.type == type) filtered.add(s);
        }
        return filtered;
    }

    // --- GESTION DES POINTS DE VIE (PV) ---
    public void heal(int amount) {
        this.PV = Math.min(this.maxPV, this.PV + amount);
    }

    public void takeDamage(int amount) {
        this.PV -= amount;
        if (this.PV < 0) this.PV = 0;
    }

    // --- GESTION DU MANA (PM) ---
    public void regenPM(int amount) {
        this.PM = Math.min(this.maxPM, this.PM + amount);
    }

    public void consumePM(int amount) {
        this.PM = Math.max(0, this.PM - amount);
    }

    // --- GESTION DE L'ÉNERGIE / PA ---
    public void regenPA(int amount) {
        this.PA = Math.min(this.maxPA, this.PA + amount);
    }

    public void consumePA(int amount) {
        this.PA = Math.max(0, this.PA - amount);
    }

    // --- GESTION DE L'ARGENT ---
    public void addMoney(int amount) {
        this.money += amount;
    }

    // --- GESTION DE L'EXPÉRIENCE ET DES NIVEAUX ---

    /**
     * Ajoute de l'expérience et gère la montée de niveau si nécessaire.
     */
    public void gainExp(int amount) {
        this.exp += amount;

        while (this.exp >= getExpForNextLevel()) {
            this.exp -= getExpForNextLevel();
            levelUp();
        }
    }

    /**
     * Formule d'XP requise : 100 * Niveau actuel
     */
    public int getExpForNextLevel() {
        return 100 * level;
    }

    protected void levelUp() {
        this.level++;

        // Application de l'augmentation (+10% ou +1 minimum)
        this.maxPV = scaleStat(this.maxPV);
        this.maxPM = scaleStat(this.maxPM);
        this.maxPA = scaleStat(this.maxPA);

        this.FOR = scaleStat(this.FOR);
        this.DEF = scaleStat(this.DEF);
        this.FORM = scaleStat(this.FORM);
        this.DEFM = scaleStat(this.DEFM);
        this.VIT = scaleStat(this.VIT);

        // Restauration complète (récompense)
        this.PV = this.maxPV;
        this.PM = this.maxPM;
        this.PA = this.maxPA;

        System.out.println(this.nom + " passe au niveau " + this.level + " !");

        updateKnownSkills();
    }

    /**
     * Méthode utilitaire : Calcule la nouvelle valeur d'une stat.
     * Augmente de 10% (arrondi à l'entier le plus proche).
     * Si le résultat n'est pas supérieur à l'ancienne valeur, force +1.
     */
    private int scaleStat(int currentVal) {
        int increased = (int) Math.round(currentVal * 1.1);
        // Retourne le max entre "valeur calculée" et "valeur actuelle + 1"
        return Math.max(currentVal + 1, increased);
    }

    // --- GETTERS ---
    public int getPV() { return PV; }
    public int getMaxPV() { return maxPV; }
    public int getPM() { return PM; }
    public int getMaxPM() { return maxPM; }
    public int getPA() { return PA; }
    public int getMaxPA() { return maxPA; }
    public int getFOR() { return FOR; }
    public int getDEF() { return DEF; }
    public int getFORM() { return FORM; }
    public int getDEFM() { return DEFM; }
    public int getVIT() { return VIT; }
    public int getDEP() { return DEP; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getMoney() { return money; }
    public String getName() { return nom; }
    public Array<Skill> getSkills() { return knownSkills; }

    // --- SETTERS ---
    public void setPV(int pv) { this.PV = Math.min(pv, maxPV); }
    public void setPM(int pm) { this.PM = Math.min(pm, maxPM); }
    public void setPA(int pa) { this.PA = Math.min(pa, maxPA); }
    public void setLevel(int level) { this.level = level; }
    public void setExp(int exp) { this.exp = exp; }
    public void setMoney(int money) { this.money = money; }

    public void setStats(int FOR, int DEF, int FORM, int DEFM, int VIT) {
        this.FOR = FOR;
        this.DEF = DEF;
        this.FORM = FORM;
        this.DEFM = DEFM;
        this.VIT = VIT;
    }

    @Override
    public void dispose() {
        if (getSprite() != null && getSprite().getTexture() != null) {
            getSprite().getTexture().dispose();
        }
    }
}
