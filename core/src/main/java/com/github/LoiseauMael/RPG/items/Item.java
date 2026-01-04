package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Fighter;

public abstract class Item {
    protected String name;
    protected String description;
    protected int count;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.count = 1;
    }

    // Méthode abstraite : chaque objet définit son effet
    public abstract void use(Fighter target);

    public void addCount(int amount) { this.count += amount; }
    public void removeCount(int amount) { this.count -= amount; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCount() { return count; }
}
