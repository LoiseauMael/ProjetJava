package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Représente une Arme.
 * <p>
 * Augmente les statistiques offensives du joueur :
 * <ul>
 * <li><b>bonusFOR</b> : Force Physique.</li>
 * <li><b>bonusFORM</b> : Force Magique.</li>
 * </ul>
 */
public class Weapon extends Equipment {
    public int bonusFOR;
    public int bonusFORM;

    public Weapon(String name, String desc, Class<? extends Player> req, int bonusFOR, int bonusFORM) {
        super(name, desc, req);
        this.bonusFOR = bonusFOR;
        this.bonusFORM = bonusFORM;
    }
}
