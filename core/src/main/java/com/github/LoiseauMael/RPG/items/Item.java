package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;

public abstract class Item {

    protected String name;
    protected String description;
    protected int count;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.count = 1; // Par défaut, on en a 1
    }

    /**
     * Méthode abstraite définissant l'effet de l'objet.
     */
    public abstract void use(Fighter target);

    public void addCount(int amount) {
        this.count += amount;
    }

    // --- C'EST LA MÉTHODE QUI MANQUAIT ---
    public void setCount(int count) {
        this.count = count;
    }

    // --- GETTERS ---
    public int getCount() { return count; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    // Utile pour le debug ou l'affichage simple
    @Override
    public String toString() {
        return name + " x" + count;
    }
}
