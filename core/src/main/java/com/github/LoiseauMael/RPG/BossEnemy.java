package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;

public class BossEnemy extends Enemy {

    public BossEnemy(float x, float y, int level, int id) {
        super(x, y,
            level,
            500 * level,
            200 + (level * 20),
            50,
            20,
            20 + (level * 2),
            15 + level,
            15 + level,
            15 + level,
            5,
            5,
            "Boss"
        );

        this.id = id;
        this.nom = "Grand Boss Final";
    }

    @Override
    protected void setupMoves() {
        // Le Boss a une attaque dévastatrice
        this.availableMoves.add(new EnemyMove(new AttackAction("Annihilation", "La fin est proche.", 1.5f), 100));
    }
}
