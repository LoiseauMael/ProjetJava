package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public class Relic extends Equipment {
    public float damageMultiplier;
    public float defenseMultiplier;

    // Les reliques sont souvent utilisables par tout le monde (req = null)
    public Relic(String name, String desc, float dmgMult, float defMult) {
        super(name, desc, null); // Pas de restriction de classe
        this.damageMultiplier = dmgMult;
        this.defenseMultiplier = defMult;
    }
}
