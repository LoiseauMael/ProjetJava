package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;

public class UltimateAction extends BattleAction {

    // On a retiré "int damage" des attributs car c'est calculé dynamiquement

    public UltimateAction(String name) {
        // Nom, Description, Portée (5.0f = touche de très loin)
        super(name, "Attaque Ultime (Dégâts x3) !", 5.0f);
    }

    @Override
    public int getAPCost() {
        return 0; // Souvent gratuit en PA car c'est un coup spécial
    }

    @Override
    public int getMPCost() {
        return 20; // Coûte beaucoup de Mana
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPM() >= getMPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // 1. Payer le coût
        user.restoreMana(-getMPCost());

        // 2. Calcul des dégâts DYNAMIQUES (x3 Force du lanceur)
        // On n'oublie pas de soustraire la défense de la cible
        int damage = (user.getFOR() * 3) - target.getDEF();

        // Sécurité : au moins 1 dégât
        if (damage < 1) damage = 1;

        // 3. Appliquer les dégâts
        target.takeDamage(damage);

        // Feedback Console
        Gdx.app.log("COMBAT", ">>> " + user.getClass().getSimpleName() + " lance l'ULTIME " + getName() + " !!! <<<");
        Gdx.app.log("COMBAT", "CRITIQUE ! " + damage + " dégâts infligés !");
    }
}
