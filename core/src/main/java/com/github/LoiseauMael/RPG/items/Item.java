package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.model.entities.Player;

/**
 * Classe abstraite représentant un objet générique du jeu.
 * <p>
 * Tout élément de l'inventaire (Potion, Épée, Clé...) doit hériter de cette classe.
 * Elle gère le nom, la description et la quantité (empilement).
 */
public abstract class Item {
    protected String name;
    protected String description;
    protected int count;

    /**
     * @param name Nom affiché de l'objet.
     * @param description Description courte visible dans l'inventaire.
     */
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.count = 1;
    }

    /**
     * Tente d'utiliser l'objet sur un joueur.
     *
     * @param player Le joueur qui utilise l'objet.
     * @return {@code true} si l'objet a été utilisé (et doit être décompté), {@code false} sinon.
     */
    public abstract boolean use(Player player);

    public String getName() { return name; }
    public String getDescription() { return description; }

    public int getCount() { return count; }

    /**
     * Définit la quantité exacte de cet objet dans la pile.
     * @param count Nouvelle quantité.
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Ajoute une quantité à la pile actuelle.
     * @param n Nombre d'objets à ajouter.
     */
    public void addCount(int n) { this.count += n; }

    /**
     * Retire une quantité de la pile actuelle.
     * @param n Nombre d'objets à retirer.
     */
    public void removeCount(int n) { this.count -= n; }
}
