package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.SpellAction;

public class EliteEnemy extends Enemy {
    public EliteEnemy(float x, float y, String texturePath) {
        // PV=100, PM=20, PA=6... Stats moyennes
        super(x, y, 100, 20, 6, 12, 5, 10, 5, 6, 4, texturePath);
    }

    @Override
    protected void setupMoves() {
        // 70% Attaque normale
        this.availableMoves.add(new EnemyMove(new AttackAction(), 70));
        // 30% Compétence (Boule de feu ici, pour l'exemple)
        this.availableMoves.add(new EnemyMove(new SpellAction("Feu Sombre", 5, 20), 30));
    }
}
