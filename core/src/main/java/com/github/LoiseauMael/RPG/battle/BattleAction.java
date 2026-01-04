package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public abstract class BattleAction {
    protected String name;
    protected String description;
    protected float range;

    public BattleAction(String name, String description, float range) {
        this.name = name;
        this.description = description;
        this.range = range;
    }

    public abstract boolean canExecute(Fighter user);
    public abstract void execute(Fighter user, Fighter target);

    /**
     * NOUVEAU : Retourne la liste des positions (x, y) valides pour cette action.
     * Si retourne null, le jeu utilisera la "range" (cercle) par défaut.
     */
    public Array<Vector2> getTargetableTiles(Fighter user) {
        return null; // Par défaut : comportement circulaire (comme les sorts)
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getRange() { return range; }
}
