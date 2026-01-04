package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ArtAction extends BattleAction {
    private int paCost;
    private float damageMultiplier;

    public ArtAction(String name, int paCost, float multiplier) {
        // Portée de 2.0f pour les Arts (Cercle proche)
        super(name, "Art physique (Coût: " + paCost + " PA)", 2.0f);
        this.paCost = paCost;
        this.damageMultiplier = multiplier;
    }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPA() >= paCost;
    }

    /**
     * NOUVEAU : Retourne les cases accessibles (Cercle court).
     */
    @Override
    public Array<Vector2> getTargetableTiles(Fighter user) {
        Array<Vector2> tiles = new Array<>();

        float centerX = user.get_positionX() + (user.getSprite().getWidth() / 2f);
        float centerY = user.get_positionY() + (user.getSprite().getHeight() / 2f);

        int cx = (int) Math.floor(centerX);
        int cy = (int) Math.floor(centerY);
        int r = (int) Math.ceil(this.range);

        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (Vector2.dst(cx, cy, x, y) <= this.range) {
                    tiles.add(new Vector2(x, y));
                }
            }
        }
        return tiles;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        user.setPA(user.getPA() - paCost);

        int damage = Math.max(1, (int)(user.getFOR() * damageMultiplier) - target.getDEF());
        target.setPV(target.getPV() - damage);

        Gdx.app.log("Battle", user.getClass().getSimpleName() + " utilise l'art " + name + " ! " + damage + " dégâts.");
    }
}
