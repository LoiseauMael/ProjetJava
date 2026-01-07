package com.github.LoiseauMael.RPG.npcs;

import com.github.LoiseauMael.RPG.Player;
import com.badlogic.gdx.Gdx;

public class MerchantNPC extends NPC {
    public MerchantNPC(float x, float y, String texturePath, String name, String... dialogues) {
        super(x, y, texturePath, name, dialogues);
    }

    @Override
    public void onInteract(Player player) {
        // Ici, on pourrait ouvrir un menu de magasin
        Gdx.app.log("PNJ", "Marchand : 'Regardez mes articles !'");
    }
}
