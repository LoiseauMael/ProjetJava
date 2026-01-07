package com.github.LoiseauMael.RPG.battle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.Fighter;

public abstract class BattleAction {

    protected String name;
    protected String description;
    protected float range;

    public BattleAction(String name, String description, float range) {
        this.name = name;
        this.description = description;
        this.range = range;
    }

    // --- GETTERS DE BASE ---
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getRange() { return range; }

    // --- METHODES ABSTRAITES (Celles qui manquaient) ---
    // Chaque action DOIT définir combien elle coûte et ce qu'elle fait
    public abstract int getAPCost();
    public abstract int getMPCost();
    public abstract boolean canExecute(Fighter user);
    public abstract void execute(Fighter user, Fighter target);

    // --- OPTIONNEL (Pour le BattleSystem) ---
    // Permet de définir des zones d'effet spécifiques (ex: ligne, croix)
    // Par défaut, renvoie null (cercle simple autour du joueur)
    public Array<Vector2> getTargetableTiles(Fighter user) {
        return null;
    }
}
