package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class Fighter extends Entity {

    // Stats existantes...
    protected int PV, maxPV;
    protected int PM, maxPM;
    protected int PA, maxPA;
    protected int FOR, DEF, FORM, DEFM, VIT, DEP;

    // --- NOUVEAUX ATTRIBUTS ---
    protected int level;
    protected int exp;
    protected int money;

    public Fighter(float positionX, float positionY, float velocityX, float velocityY,
                   int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP,
                   Sprite sprite) {
        super(positionX, positionY, velocityX, velocityY, sprite);
        this.PV = PV; this.maxPV = PV;
        this.PM = PM; this.maxPM = PM;
        this.PA = PA; this.maxPA = PA;
        this.FOR = FOR; this.DEF = DEF;
        this.FORM = FORM; this.DEFM = DEFM;
        this.VIT = VIT;
        this.DEP = DEP;

        // Initialisation par défaut
        this.level = 1;
        this.exp = 0;
        this.money = 0;
    }

    // --- GETTERS & SETTERS ---
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExp() { return exp; }
    public void setExp(int exp) { this.exp = exp; }

    public int getMoney() { return money; }
    public void setMoney(int money) { this.money = money; }

    public void addMoney(int amount) { this.money += amount; }

    // ... Vos autres getters existants (getPV, getFOR, etc.) ...
    public int getPV() { return PV; }
    public void setPV(int pv) { this.PV = Math.max(0, Math.min(pv, maxPV)); }
    public int getPM() { return PM; }
    public void setPM(int pm) { this.PM = Math.max(0, Math.min(pm, maxPM)); }
    public int getPA() { return PA; }
    public void setPA(int pa) { this.PA = Math.max(0, Math.min(pa, maxPA)); }
    public int getFOR() { return FOR; }
    public int getDEF() { return DEF; }
    public int getFORM() { return FORM; }
    public int getDEFM() { return DEFM; }
    public int getVIT() { return VIT; }
    public int getDEP() { return DEP; }

    public abstract void update(float delta);
}
