package com.github.LoiseauMael.RPG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.SwordMan;
import com.github.LoiseauMael.RPG.Wizard;
import com.github.LoiseauMael.RPG.items.*;

public class ShopLoader {

    public static void loadShop(Main game) {
        game.merchantInventory = new Array<>();

        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(Gdx.files.internal("data/shop.json"));

        for (JsonValue entry : root) {
            String type = entry.getString("type");
            String name = entry.getString("name");
            String desc = entry.getString("description");
            int price = entry.getInt("price");

            Item item = null;

            switch (type) {
                case "POTION_HP":
                    int heal = entry.getInt("value");
                    item = new HealthPotion(name, desc, heal);
                    break;

                case "POTION_MP":
                    int mana = entry.getInt("value");
                    item = new ManaPotion(name, desc, mana);
                    break;

                case "WEAPON":
                    int dmg = entry.getInt("damage");
                    int mag = entry.getInt("magic", 0); // 0 par défaut si non précisé
                    String reqClassW = entry.getString("requiredClass", "ANY");
                    item = new Weapon(name, desc, getClassFromString(reqClassW), dmg, mag);
                    break;

                case "ARMOR":
                    int def = entry.getInt("defense");
                    int mDef = entry.getInt("magicDef");
                    String reqClassA = entry.getString("requiredClass", "ANY");
                    item = new Armor(name, desc, getClassFromString(reqClassA), def, mDef);
                    break;
            }

            if (item != null) {
                game.merchantInventory.add(new Main.ShopEntry(item, price));
            }
        }

        Gdx.app.log("ShopLoader", "Chargé " + game.merchantInventory.size + " objets dans le magasin.");
    }

    private static Class getClassFromString(String className) {
        if (className.equalsIgnoreCase("Guerrier")) return SwordMan.class;
        if (className.equalsIgnoreCase("Mage")) return Wizard.class;
        return null; // Pour "ANY" ou autre
    }
}
