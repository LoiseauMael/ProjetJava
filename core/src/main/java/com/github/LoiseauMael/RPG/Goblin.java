package com.github.LoiseauMael.RPG;

import com.github.LoiseauMael.RPG.battle.AttackAction;

public class Goblin extends Enemy {
    public Goblin(float x, float y, int level, int id) {
        super(x, y, level, 15 * level, 20 + (level * 5), 0, 10, 5 + level, 2 + level, 2, 2, 3, 3, "GoblinSpriteSheet.png");
        this.id = id;
        this.nom = "Gobelin";

        // TAILLE AUGMENTÉE : 2.0 unités
        if (this.sprite != null) {
            this.sprite.setSize(2.0f, 2.0f);
        }

        // Hitbox ajustée
        this.setCollisionBounds(1.2f, 0.6f, 0.4f, 0f);
        this.moveSpeed = 1.1f;
    }

    @Override
    protected void setupMoves() {
        // Le gobelin a juste une attaque de base (Portée 1 case)
        // 100% de chance d'utiliser cette attaque si possible
        this.availableMoves.add(new EnemyMove(new AttackAction("Coup de dague", "Une attaque sournoise.", 1.0f), 100));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // Centrage visuel constant
        if (sprite != null) {
            float visualX = positionX - (sprite.getWidth() / 2f) + (collisionWidth / 2f);
            sprite.setPosition(visualX, positionY);
        }
    }
}
