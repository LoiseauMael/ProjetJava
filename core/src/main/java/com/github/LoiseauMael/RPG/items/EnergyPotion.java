package com.github.LoiseauMael.RPG.items;

import com.github.LoiseauMael.RPG.Player;

public class EnergyPotion extends Item {

    private int amount;

    public EnergyPotion(String name, String description, int amount) {
        super(name, description);
        this.amount = amount;
    }

    @Override
    public boolean use(Player player) {
        // On vérifie si le joueur a besoin d'énergie (PA)
        if (player.getPA() < player.getMaxPA()) {

            // CORRECTION ICI : On utilise la nouvelle méthode regenPA()
            player.regenPA(amount);

            System.out.println("Vous utilisez " + getName() + " et recuperez " + amount + " PA.");
            return true; // L'objet est consommé
        }

        System.out.println("Energie (PA) deja au max !");
        return false; // L'objet n'est pas consommé
    }

    public int getAmount() {
        return amount;
    }
}
