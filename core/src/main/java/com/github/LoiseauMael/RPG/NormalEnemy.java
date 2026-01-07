package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;

public class NormalEnemy extends Enemy {
    public NormalEnemy(float x, float y, String texturePath) {
        // PV=50, PM=0, PA=4... Stats faibles
        super(x, y, 50, 0, 4, 8, 2, 0, 0, 4, 3, texturePath);
    }

    @Override
    protected void setupMoves() {
        // 100% de chance d'attaque de base
        this.availableMoves.add(new EnemyMove(new AttackAction(), 100));
    }
}
