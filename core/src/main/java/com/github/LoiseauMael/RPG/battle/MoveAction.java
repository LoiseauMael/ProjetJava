package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;

public class MoveAction extends BattleAction {

    public MoveAction() {
        // Portée 0 ou 1, peu importe car non utilisé pour cibler un ennemi
        super("Déplacement", "Se rapprocher de la cible.", 0f);
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getDEP() > 0;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        // Logique désormais gérée par BattleSystem.tryMovePlayerTo
        Gdx.app.log("Battle", "Action de déplacement appelée.");
    }
}
