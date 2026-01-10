package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;
// Vous pourrez ajouter SpellAction ici si vous voulez qu'il lance des sorts

public class KingGoblin extends Enemy {
    public KingGoblin(float x, float y, int level, int id) {
        super(x, y, level, 100 * level, 100 + (level * 10), 20, 15, 15 + level, 8 + level, 10, 10, 4, 4, "KingGoblinSpriteSheet.png");
        this.id = id;
        this.nom = "Roi Gobelin";

        // TAILLE RÉTRÉCIE : 2.5 unités
        if (this.sprite != null) {
            this.sprite.setSize(2.5f, 2.5f);
        }

        // Hitbox ajustée
        this.setCollisionBounds(1.5f, 0.8f, 0.5f, 0f);
        this.moveSpeed = 0.9f;
        this.wanderRange = 2.0f;
    }

    @Override
    protected void setupMoves() {
        // Le Roi a deux attaques possibles
        // 1. Attaque standard (60% de chance)
        this.availableMoves.add(new EnemyMove(new AttackAction("Sceptre Royal", "Un coup lourd.", 1.0f), 60));

        // 2. Une attaque "Zone" ou plus puissante (40% de chance)
        // Pour l'instant on simule avec une attaque à portée 2
        this.availableMoves.add(new EnemyMove(new AttackAction("Ordre de tuer", "Attaque a distance.", 2.0f), 40));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // Centrage visuel
        if (sprite != null) {
            float visualX = positionX - (sprite.getWidth() / 2f) + (collisionWidth / 2f);
            sprite.setPosition(visualX, positionY);
        }
    }
}
