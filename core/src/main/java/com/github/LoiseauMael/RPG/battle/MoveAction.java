package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.model.entities.Fighter;
import com.badlogic.gdx.Gdx;

/**
 * Représente l'intention de se déplacer lors d'un tour de combat.
 * <p>
 * Contrairement aux autres actions, celle-ci ne cible pas une entité mais
 * change l'état du {@link BattleSystem} pour permettre la sélection d'une case.
 * Coût : 0 ressource (généralement limité par la statistique DEP).
 */
public class MoveAction extends BattleAction {

    public MoveAction() {
        // Nom, Description, Portée (0 car géré dynamiquement par la stat DEP)
        super("Deplacement", "Se deplacer sur le terrain", 0);
    }

    @Override
    public int getAPCost() {
        return 0; // Le déplacement est gratuit en PA (géré par la stat DEP)
    }

    @Override
    public int getMPCost() {
        return 0;
    }

    @Override
    public boolean canExecute(Fighter user) {
        // On peut toujours sélectionner l'option "Bouger"
        // (La vérification "a déjà bougé" est faite par le BattleSystem)
        return true;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // Le déplacement effectif est géré par le BattleSystem via les clics souris.
        // Cette méthode sert principalement de log ou de trigger visuel si nécessaire.
        Gdx.app.log("Combat", user.getClass().getSimpleName() + " se prepare a bouger.");
    }
}
