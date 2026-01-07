package com.github.LoiseauMael.RPG.npcs;

import com.github.LoiseauMael.RPG.Player;
import com.badlogic.gdx.Gdx;

public class HealerNPC extends NPC {
    public HealerNPC(float x, float y, String texturePath, String name, String... dialogues) {
        super(x, y, texturePath, name, dialogues);
    }

    @Override
    public void onInteract(Player player) {
        player.setPV(player.getMaxPV());
        player.setPM(player.getMaxPM());
        Gdx.app.log("PNJ", "Soigneur : PV et PM restaurés !");
    }
}
