package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Fighter extends Entity {
    private int PV;
    private int PM;
    private int PA;
    private int FOR;
    private int DEF;
    private int FORM;
    private int DEFM;
    private int VIT;
    private int DEP;

    public Fighter(float positionX, float positionY, float velocityX, float velocityY, int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, Sprite sprite) {
        super(positionX, positionY, velocityX, velocityY, sprite);
        this.PV = PV;
        this.PM = PM;
        this.PA = PA;
        this.FOR = FOR;
        this.DEF = DEF;
        this.FORM = FORM;
        this.DEFM = DEFM;
        this.VIT = VIT;
        this.DEP = DEP;
    }

    public int getPV() { return this.PV; }
    public int getPM() { return this.PM; }
    public int getPA() { return this.PA; }
    public int getFOR() { return this.FOR; }
    public int getDEF() { return this.DEF; }
    public int getFORM() { return this.FORM; }
    public int getDEFM() { return this.DEFM; }
    public int getVIT() { return this.VIT; }
    public int getDEP() { return this.DEP; }

    public void setPV(int PV) { this.PV = PV; }
    public void setPM(int PM) { this.PM = PM; }
    public void setPA(int PA) { this.PA = PA; }
    public void setFOR(int FOR) { this.FOR = FOR; }
    public void setDEF(int DEF) { this.DEF = DEF; }
    public void setFORM(int FORM) { this.FORM = FORM; }
    public void setDEFM(int DEFM) { this.DEFM = DEFM; }
    public void setVIT(int VIT) { this.VIT = VIT; }
    public void setDEP(int DEP) { this.DEP = DEP; }
}
