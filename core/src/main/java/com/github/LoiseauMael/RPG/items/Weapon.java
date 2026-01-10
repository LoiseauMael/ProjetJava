package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public class Weapon extends Equipment {
    public int bonusFOR;
    public int bonusFORM;

    public Weapon(String name, String desc, Class<? extends Player> req, int bonusFOR, int bonusFORM) {
        super(name, desc, req); // Appel correct à Equipment
        this.bonusFOR = bonusFOR;
        this.bonusFORM = bonusFORM;
    }
}
