package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;

public class NormalEnemy extends Enemy {

    public NormalEnemy(float x, float y, int level, int id) {
        super(x, y,
            level,
            10 * level,
            20 + (level * 5),
            10,
            10,
            4 + level,
            2 + level,
            2,
            2,
            3,
            3,
            "Goblin" // Type générique
        );

        this.id = id;
        this.nom = "Gobelin Lvl " + level;
    }

    @Override
    protected void setupMoves() {
        this.availableMoves.add(new EnemyMove(new AttackAction("Griffure", "Attaque faible.", 1.0f), 100));
    }
}
