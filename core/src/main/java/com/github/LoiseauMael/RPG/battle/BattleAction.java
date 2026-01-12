package com.github.LoiseauMael.RPG.battle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.model.entities.Fighter;

/**
 * Classe abstraite représentant une action réalisable en combat.
 * <p>
 * Peut être une attaque physique, un sort, un art martial ou un déplacement.
 * Définit le coût (PA, PM), la portée et l'effet de l'action.
 */
public abstract class BattleAction {

    protected String name;
    protected String description;
    protected float range;

    /**
     * @param name Nom de l'action.
     * @param description Description courte.
     * @param range Portée en cases (Distance de Manhattan).
     */
    public BattleAction(String name, String description, float range) {
        this.name = name;
        this.description = description;
        this.range = range;
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getRange() { return range; }

    // --- METHODES ABSTRAITES ---

    /** @return Le coût en Points d'Action (PA). */
    public abstract int getAPCost();

    /** @return Le coût en Points de Mana (PM). */
    public abstract int getMPCost();

    /**
     * Vérifie si l'utilisateur possède les ressources nécessaires pour lancer l'action.
     * @param user L'entité qui tente l'action.
     * @return true si l'action est possible.
     */
    public abstract boolean canExecute(Fighter user);

    /**
     * Exécute l'action : consomme les ressources et applique les effets (dégâts, soin, etc.).
     * @param user L'attaquant.
     * @param target La cible (peut être null pour certaines actions de zone ou self).
     */
    public abstract void execute(Fighter user, Fighter target);

    /**
     * Optionnel : Définit les cases ciblables spécifiques (ex: zone en croix).
     * @param user L'utilisateur.
     * @return Liste des vecteurs ciblables, ou null par défaut (cercle simple).
     */
    public Array<Vector2> getTargetableTiles(Fighter user) {
        return null;
    }
}
