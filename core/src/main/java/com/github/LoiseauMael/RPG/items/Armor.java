package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Représente une Armure.
 * <p>
 * Augmente les statistiques défensives du joueur :
 * <ul>
 * <li><b>bonusDEF</b> : Défense Physique.</li>
 * <li><b>bonusDEFM</b> : Défense Magique.</li>
 * </ul>
 */
public class Armor extends Equipment {
    public int bonusDEF;
    public int bonusDEFM;

    public Armor(String name, String desc, Class<? extends Player> req, int bonusDEF, int bonusDEFM) {
        super(name, desc, req);
        this.bonusDEF = bonusDEF;
        this.bonusDEFM = bonusDEFM;
    }
}
