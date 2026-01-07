package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;

public class Goblin extends Enemy {

    public Goblin(float x, float y, int level) {
        // On passe des 0 au super constructeur car on va tout recalculer avec initStats
        super(x, y, 0, 0, 0, 0, 0, 0, 0, 0, 0, "GoblinSpriteSheet.png");

        // Stats de BASE (Niveau 1)
        int bPV = 40;
        int bPM = 10;
        int bPA = 6;
        int bFOR = 10;  // Force correcte
        int bDEF = 3;   // Peu d'armure
        int bFORM = 0;  // Pas de magie
        int bDEFM = 2;
        int bVIT = 6;   // Assez rapide
        int bDEP = 4;

        // Calcul automatique selon le niveau demandé
        initStats(level, bPV, bPM, bPA, bFOR, bDEF, bFORM, bDEFM, bVIT, bDEP);
    }

    @Override
    protected void setupMoves() {
        this.availableMoves.clear();
        // Le gobelin fait principalement des attaques de base
        this.availableMoves.add(new EnemyMove(new AttackAction(), 100));
    }
}
