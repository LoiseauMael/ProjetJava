package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public class Relic extends Equipment {
    // 1.0 = 100% (normal). 1.2 = +20% de dégâts.
    public float damageMultiplier;
    public float defenseMultiplier; // Réduction de dégâts (0.9 = -10% subis)

    public Relic(String name, String desc, float dmgMulti, float defMulti) {
        super(name, desc, null); // Les reliques sont souvent pour tout le monde (null)
        this.damageMultiplier = dmgMulti;
        this.defenseMultiplier = defMulti;
    }
}
