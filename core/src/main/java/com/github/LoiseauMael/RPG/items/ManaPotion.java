package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public class ManaPotion extends Item {

    private int amount;

    public ManaPotion(String name, String description, int amount) {
        super(name, description);
        this.amount = amount;
    }

    @Override
    public boolean use(Player player) {
        // On vérifie si le joueur a besoin de mana (optionnel, mais mieux)
        if (player.getPM() < player.getMaxPM()) {

            // On utilise la nouvelle méthode regenPM
            player.regenPM(amount);

            System.out.println("Vous utilisez " + getName() + " et recuperez " + amount + " PM.");
            return true; // Renvoie VRAI : L'objet a été utilisé et doit être supprimé
        }

        System.out.println("PM deja au max !");
        return false; // Renvoie FAUX : L'objet n'est pas utilisé (pas supprimé)
    }

    public int getAmount() {
        return amount;
    }
}
