package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.github.LoiseauMael.RPG.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class AttackAction extends BattleAction {

    public AttackAction() {
        super("Attaque", "Coup d'épée en zone", 0);
    }

    @Override
    public boolean canExecute(Fighter user) {
        return true;
    }

    @Override
    public Array<Vector2> getTargetableTiles(Fighter user) {
        Array<Vector2> tiles = new Array<>();

        // --- FIX: CENTERING ON THE SPRITE ---
        // Sprite is 2 units wide (32px), so center is at x + 1.0f
        // Collision box might be only 1 unit. We trust the Sprite for visuals.
        float visualCenterX = user.get_positionX() + (user.getSprite().getWidth() / 2f);
        float visualCenterY = user.get_positionY() + (user.getSprite().getHeight() / 2f);

        // Lock to grid index
        int x = (int) Math.floor(visualCenterX);
        int y = (int) Math.floor(visualCenterY);

        int dir = 0;
        if (user instanceof Player) {
            dir = ((Player) user).getDirection();
        }

        // Zone configuration (O=Target, P=Player)
        // LEFT example:
        //   O
        //  OO P
        //   O
        switch (dir) {
            case 1: // LEFT
                tiles.add(new Vector2(x - 1, y));     // Front
                tiles.add(new Vector2(x - 2, y));     // Far Front
                tiles.add(new Vector2(x - 1, y + 1)); // Front-Top
                tiles.add(new Vector2(x - 1, y - 1)); // Front-Bottom
                break;

            case 2: // RIGHT
                tiles.add(new Vector2(x + 1, y));
                tiles.add(new Vector2(x + 2, y));
                tiles.add(new Vector2(x + 1, y + 1));
                tiles.add(new Vector2(x + 1, y - 1));
                break;

            case 3: // TOP
                tiles.add(new Vector2(x, y + 1));
                tiles.add(new Vector2(x, y + 2));
                tiles.add(new Vector2(x - 1, y + 1));
                tiles.add(new Vector2(x + 1, y + 1));
                break;

            case 0: // BOTTOM
            default:
                tiles.add(new Vector2(x, y - 1));
                tiles.add(new Vector2(x, y - 2));
                tiles.add(new Vector2(x - 1, y - 1));
                tiles.add(new Vector2(x + 1, y - 1));
                break;
        }

        return tiles;
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        int damage = Math.max(1, user.getFOR() - target.getDEF());
        target.setPV(target.getPV() - damage);
        Gdx.app.log("Battle", "Hit! " + damage + " damage.");
    }
}
