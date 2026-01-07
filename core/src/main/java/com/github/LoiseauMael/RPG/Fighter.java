package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class Fighter extends Entity {

    // Stats de base (Points de Vie, Mana, Action)
    protected int maxPV;
    protected int PV;
    protected int maxPM;
    protected int PM;
    protected int maxPA;
    protected int PA;

    // Attributs de combat
    protected int FOR;  // Force Physique
    protected int DEF;  // Défense Physique
    protected int FORM; // Force Magique
    protected int DEFM; // Défense Magique
    protected int VIT;  // Vitesse (Initiative)
    protected int DEP;  // Déplacement (Cases)

    // Progression
    protected int level;
    protected int exp;
    protected int money;

    /**
     * Constructeur complet utilisé par Player et Enemy.
     */
    public Fighter(float positionX, float positionY, float velocityX, float velocityY,
                   int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP,
                   Sprite sprite) {

        super(positionX, positionY, sprite);

        // Initialisation Physique
        this.set_velocityX(velocityX);
        this.set_velocityY(velocityY);

        // Initialisation Stats
        this.maxPV = PV;
        this.PV = PV;
        this.maxPM = PM;
        this.PM = PM;
        this.maxPA = PA;
        this.PA = PA;

        this.FOR = FOR;
        this.DEF = DEF;
        this.FORM = FORM;
        this.DEFM = DEFM;
        this.VIT = VIT;
        this.DEP = DEP;

        // Valeurs par défaut
        this.level = 1;
        this.exp = 0;
        this.money = 0;
    }

    // ==========================================
    // COMBAT (Dégâts)
    // ==========================================

    /**
     * Applique des dégâts directs au combattant.
     * Note : La réduction par la défense (DEF) est généralement calculée
     * dans l'objet BattleAction avant d'appeler cette méthode,
     * ou vous pouvez l'intégrer ici si vous préférez.
     */
    public void takeDamage(int amount) {
        this.PV -= amount;
        if (this.PV < 0) {
            this.PV = 0;
        }
    }

    // ==========================================
    // GESTION SOIN & RESTAURATION
    // ==========================================

    public void heal(int amount) {
        this.PV += amount;
        if (this.PV > this.maxPV) this.PV = this.maxPV;
    }

    public void restoreMana(int amount) {
        this.PM += amount;
        if (this.PM > this.maxPM) this.PM = this.maxPM;
    }

    public void restorePA(int amount) {
        this.PA += amount;
        if (this.PA > this.maxPA) this.PA = this.maxPA;
    }

    // ==========================================
    // GETTERS & SETTERS
    // ==========================================

    // --- PV ---
    public int getPV() { return PV; }
    public void setPV(int pv) {
        this.PV = pv;
        if (this.PV > maxPV) this.PV = maxPV;
        if (this.PV < 0) this.PV = 0;
    }
    public int getMaxPV() { return maxPV; }

    // --- PM ---
    public int getPM() { return PM; }
    public void setPM(int pm) {
        this.PM = pm;
        if (this.PM > maxPM) this.PM = maxPM;
        if (this.PM < 0) this.PM = 0;
    }
    public int getMaxPM() { return maxPM; }

    // --- PA ---
    public int getPA() { return PA; }

    // Setter PA (Correction de l'erreur précédente)
    public void setPA(int pa) {
        this.PA = pa;
        if (this.PA > maxPA) this.PA = maxPA;
        if (this.PA < 0) this.PA = 0;
    }

    public int getMaxPA() { return maxPA; }

    public abstract void update(float delta);

    // --- STATS COMBAT ---
    // Ces getters sont surchargés dans Player pour inclure les bonus d'équipement
    public int getFOR() { return FOR; }
    public int getDEF() { return DEF; }
    public int getFORM() { return FORM; }
    public int getDEFM() { return DEFM; }
    public int getVIT() { return VIT; }
    public int getDEP() { return DEP; }

    // --- PROGRESSION ---
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }

    public int getMoney() { return money; }
    public void addMoney(int amount) { this.money += amount; }
    public void setMoney(int money) { this.money = money; }
}
