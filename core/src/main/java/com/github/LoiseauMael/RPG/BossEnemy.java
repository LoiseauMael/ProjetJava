package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.SpellAction;
import com.github.LoiseauMael.RPG.battle.UltimateAction;

public class BossEnemy extends Enemy {
    public BossEnemy(float x, float y, String texturePath) {
        // PV=300, PM=50, PA=8... Stats Boss
        super(x, y, 300, 50, 8, 20, 10, 20, 10, 5, 4, texturePath);
    }

    @Override
    protected void setupMoves() {
        // 50% Attaque normale
        this.availableMoves.add(new EnemyMove(new AttackAction(), 50));

        // 30% Compétence Magique (Nom, Puissance, Coût Mana)
        this.availableMoves.add(new EnemyMove(new SpellAction("Explosion", 8, 30), 30));

        // 20% Ultime
        // CORRECTION ICI : On retire la valeur "60" car les dégâts sont calculés dans UltimateAction
        this.availableMoves.add(new EnemyMove(new UltimateAction("APOCALYPSE"), 20));
    }
}
