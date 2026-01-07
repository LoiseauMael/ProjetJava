package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;

public class MoveAction extends BattleAction {

    public MoveAction() {
        // Nom, Description, Portée (0 car géré dynamiquement par la stat DEP)
        super("Deplacement", "Se deplacer sur le terrain", 0);
    }

    @Override
    public int getAPCost() {
        return 0; // Initier le déplacement est souvent gratuit, ou le coût est calculé par case
    }

    // --- CORRECTION : Ajout de la méthode manquante ---
    @Override
    public int getMPCost() {
        return 0; // Se déplacer ne coûte pas de Mana
    }

    @Override
    public boolean canExecute(Fighter user) {
        // On peut toujours essayer de bouger (sauf si immobilisé, logique à ajouter plus tard si besoin)
        return true;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // Le déplacement est un cas particulier souvent géré directement par le BattleSystem
        // via des clics sur la carte, donc cette méthode peut rester simple ou servir de log.
        Gdx.app.log("Combat", user.getClass().getSimpleName() + " se prepare a bouger.");
    }
}
