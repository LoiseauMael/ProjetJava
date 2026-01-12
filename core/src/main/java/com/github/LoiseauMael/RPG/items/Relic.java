package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Représente une Relique (Accessoire).
 * <p>
 * Offre des bonus passifs sous forme de multiplicateurs de dégâts ou de défense.
 * Généralement utilisable par toutes les classes.
 */
public class Relic extends Equipment {
    public float damageMultiplier;
    public float defenseMultiplier;

    /**
     * @param name Nom de la relique.
     * @param desc Description.
     * @param dmgMult Multiplicateur de dégâts infligés (ex: 1.1 pour +10%).
     * @param defMult Multiplicateur de défense (réduction dégâts reçus).
     */
    public Relic(String name, String desc, float dmgMult, float defMult) {
        super(name, desc, null); // Pas de restriction de classe par défaut
        this.damageMultiplier = dmgMult;
        this.defenseMultiplier = defMult;
    }
}
