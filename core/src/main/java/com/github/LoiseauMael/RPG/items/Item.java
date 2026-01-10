package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public abstract class Item {
    protected String name;
    protected String description;
    protected int count;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.count = 1;
    }

    public abstract boolean use(Player player);

    public String getName() { return name; }
    public String getDescription() { return description; }

    public int getCount() { return count; }

    // --- AJOUT : La méthode manquante ---
    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int n) { this.count += n; }
    public void removeCount(int n) { this.count -= n; }
}
