package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class SpellAction extends BattleAction {
    private int pmCost;
    private int power;

    public SpellAction(String name, int pmCost, int power) {
        // Portée de 5.0f par exemple
        super(name, "Sort magique (Coût: " + pmCost + " PM)", 5.0f);
        this.pmCost = pmCost;
        this.power = power;
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPM() >= pmCost;
    }

    /**
     * NOUVEAU : Retourne toutes les cases dans le rayon (Cercle).
     * Cela permet d'utiliser la validation stricte "Case par Case" du BattleSystem.
     */
    @Override
    public Array<Vector2> getTargetableTiles(Fighter user) {
        Array<Vector2> tiles = new Array<>();

        // Centre du lanceur
        float centerX = user.get_positionX() + (user.getSprite().getWidth() / 2f);
        float centerY = user.get_positionY() + (user.getSprite().getHeight() / 2f);

        int cx = (int) Math.floor(centerX);
        int cy = (int) Math.floor(centerY);
        int r = (int) Math.ceil(this.range);

        // On parcourt le carré autour du rayon
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                // Si la case est dans le cercle de portée
                if (Vector2.dst(cx, cy, x, y) <= this.range) {
                    tiles.add(new Vector2(x, y));
                }
            }
        }
        return tiles;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        user.setPM(user.getPM() - pmCost);

        int damage = Math.max(1, (user.getFORM() + power) - target.getDEFM());
        target.setPV(target.getPV() - damage);

        Gdx.app.log("Battle", user.getClass().getSimpleName() + " lance " + name + " ! " + damage + " dégâts.");
    }
}
