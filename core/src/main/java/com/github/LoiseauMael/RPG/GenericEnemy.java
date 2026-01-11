package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.BattleAction;

public class GenericEnemy extends Enemy {

    private String aiType;

    public GenericEnemy(float x, float y, int id, MapProperties props) {
        super(
            x, y,
            props.get("level", 1, Integer.class),
            props.get("exp", 10, Integer.class),
            props.get("hp", 50, Integer.class),
            props.get("mp", 0, Integer.class),
            props.get("ap", 3, Integer.class),
            props.get("str", 5, Integer.class),
            props.get("def", 2, Integer.class),
            props.get("mag", 0, Integer.class),
            props.get("res", 0, Integer.class),
            props.get("spd", 5, Integer.class),
            props.get("move", 3, Integer.class),
            props.get("texture", "assets/GoblinSpriteSheet.png", String.class)
        );

        this.id = id;
        this.nom = props.get("name", "Monstre", String.class);

        // Gestion de l'argent (loot)
        int gold = props.get("gold", 0, Integer.class);
        if (gold == 0) gold = props.get("money", 5, Integer.class);
        this.setMoney(gold);

        this.aiType = props.get("ai", "normal", String.class);

        // --- MODIFICATION : TAILLE X2 ---
        if (this.sprite != null) {
            // On double la taille visuelle (2 tuiles de haut et de large)
            this.sprite.setSize(2f, 2f);
        }
        // On augmente aussi la hitbox pour qu'elle corresponde au visuel
        // (1.5f au lieu de 2f pour éviter de coincer trop facilement dans les murs)
        this.setCollisionBounds(1.5f, 1.5f, 0.25f, 0);
    }

    @Override
    protected void setupMoves() {
        this.availableMoves = new Array<>();
        this.availableMoves.add(new EnemyMove(new AttackAction(), 100));

        if ("boss".equals(aiType)) {
            // Logique boss future
        }
    }
}
