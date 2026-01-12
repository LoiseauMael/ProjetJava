package com.github.LoiseauMael.RPG.items;

/**
 * Classe abstraite pour les objets à usage unique (Potions, Parchemins...).
 * <p>
 * Sert principalement de marqueur pour distinguer les consommables des équipements
 * dans les tris d'inventaire.
 */
public abstract class Consumable extends Item {

    public Consumable(String name, String description) {
        super(name, description);
    }
}
