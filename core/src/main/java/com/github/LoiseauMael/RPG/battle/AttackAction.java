package com.github.LoiseauMael.RPG.battle;

import com.github.LoiseauMael.RPG.Fighter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class AttackAction extends BattleAction {

    public AttackAction() {
        super("Attaque", "Coup basique", 1.5f);
    }

    @Override
    public int getAPCost() { return 2; }

    @Override
    public int getMPCost() { return 0; }

    @Override
    public boolean canExecute(Fighter user) {
        return user.getPA() >= getAPCost();
    }

    @Override
    public void execute(Fighter user, Fighter target) {
        user.restorePA(-getAPCost());

        int damage = user.getFOR() - target.getDEF();
        if (damage < 1) damage = 1;

        target.takeDamage(damage);

        // MODIFICATION ICI : On utilise le système de log centralisé
        BattleSystem.addLog(user.getClass().getSimpleName() + " attaque et inflige " + damage + " degats !");
    }

    @Override
    public Array<Vector2> getTargetableTiles(Fighter user) {
        // Retourne la "Croix" devant le joueur (zone d'attaque)
        Array<Vector2> tiles = new Array<>();
        int cx = Math.round(user.get_positionX());
        int cy = Math.round(user.get_positionY());

        // Exemple simple : La case devant selon la direction serait mieux,
        // mais ici on prend une zone autour pour simplifier l'exemple.
        // Idéalement, utilisez user.getDirection() pour ne retourner que les cases DEVANT.
        tiles.add(new Vector2(cx + 1, cy));
        tiles.add(new Vector2(cx - 1, cy));
        tiles.add(new Vector2(cx, cy + 1));
        tiles.add(new Vector2(cx, cy - 1));

        return tiles;
    }
}
