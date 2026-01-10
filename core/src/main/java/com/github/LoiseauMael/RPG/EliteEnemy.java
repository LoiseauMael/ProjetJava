package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;

public class EliteEnemy extends Enemy {

    public EliteEnemy(float x, float y, int level, int id) {
        super(x, y,
            level,              // Niveau
            30 * level,         // EXP
            50 + (level * 8),   // PV
            10,                 // PM
            12,                 // PA
            8 + level,          // FOR
            5 + level,          // DEF
            4 + level,          // FORM
            4 + level,          // DEFM
            4,                  // VIT
            4,                  // DEP
            "Elite"             // Type (Attention, assurez-vous que Elite.png existe ou changez le nom)
        );

        this.id = id;
        this.nom = "Garde Elite Lvl " + level;
    }

    @Override
    protected void setupMoves() {
        // L'Elite frappe fort
        this.availableMoves.add(new EnemyMove(new AttackAction("Tranche-Tete", "Attaque fatale.", 1.0f), 100));
    }
}
