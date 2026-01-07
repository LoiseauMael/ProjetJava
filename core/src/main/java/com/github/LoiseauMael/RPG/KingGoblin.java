package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.SpellAction;
import com.github.LoiseauMael.RPG.battle.UltimateAction;

public class KingGoblin extends Enemy {

    public KingGoblin(float x, float y, int level) {
        super(x, y, 0, 0, 0, 0, 0, 0, 0, 0, 0, "KingGoblinSpriteSheet.png");

        // Stats de BASE (Niveau 1 - version Boss)
        int bPV = 300; // Très gros sac à PV
        int bPM = 80;
        int bPA = 8;   // Plus d'actions potentielles
        int bFOR = 20; // Tape fort
        int bDEF = 10; // Armure lourde
        int bFORM = 15;
        int bDEFM = 10;
        int bVIT = 5;  // Un peu lent
        int bDEP = 3;

        initStats(level, bPV, bPM, bPA, bFOR, bDEF, bFORM, bDEFM, bVIT, bDEP);
    }

    @Override
    protected void setupMoves() {
        this.availableMoves.clear();
        // 40% Attaque normale
        this.availableMoves.add(new EnemyMove(new AttackAction(), 40));
        // 30% Sort de Foudre
        this.availableMoves.add(new EnemyMove(new SpellAction("Foudre Royale", 15, 20), 30));
        // 30% ULTIME (Dégâts x3)
        this.availableMoves.add(new EnemyMove(new UltimateAction("ECRASEMENT"), 30));
    }
}
