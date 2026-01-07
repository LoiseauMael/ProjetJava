package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public class Armor extends Equipment {
    public int bonusDEF;
    public int bonusDEFM;

    public Armor(String name, String desc, Class<? extends Player> req, int bonusDEF, int bonusDEFM) {
        super(name, desc, req);
        this.bonusDEF = bonusDEF;
        this.bonusDEFM = bonusDEFM;
    }
}
