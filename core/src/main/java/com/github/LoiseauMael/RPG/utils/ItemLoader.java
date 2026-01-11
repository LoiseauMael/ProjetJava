package com.github.LoiseauMael.RPG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.Main.ShopEntry;
import com.github.LoiseauMael.RPG.SwordMan;
import com.github.LoiseauMael.RPG.Wizard;
import com.github.LoiseauMael.RPG.items.*;

public class ItemLoader {

    public static Array<ShopEntry> loadShopItems(String jsonPath) {
        Array<ShopEntry> inventory = new Array<>();
        JsonReader reader = new JsonReader();

        try {
            JsonValue root = reader.parse(Gdx.files.internal(jsonPath));

            for (JsonValue itemVal : root) {
                String type = itemVal.getString("type");
                String name = itemVal.getString("name");
                String desc = itemVal.getString("description", "");
                int price = itemVal.getInt("price");

                Item item = null;

                switch (type) {
                    case "CONSUMABLE":
                        int val = itemVal.getInt("effectValue");
                        // Simplification : on détecte le type par le nom ou un champ 'effectType'
                        // Ici on suppose HealPotion par défaut pour l'exemple
                        if (name.contains("Vie")) item = new HealthPotion(name, desc, val);
                        else if (name.contains("Ether")) item = new ManaPotion(name, desc, val);
                        break;

                    case "WEAPON":
                        int dmg = itemVal.getInt("statBonus");
                        int magic = itemVal.getInt("secondaryStat", 0);
                        String reqClassStr = itemVal.getString("requiredClass", "");
                        Class reqClass = null;
                        if ("SwordMan".equals(reqClassStr)) reqClass = SwordMan.class;
                        if ("Wizard".equals(reqClassStr)) reqClass = Wizard.class;

                        item = new Weapon(name, desc, reqClass, dmg, magic);
                        break;

                    case "ARMOR":
                        // Logique similaire pour Armor...
                        break;
                }

                if (item != null) {
                    inventory.add(new ShopEntry(item, price));
                }
            }

        } catch (Exception e) {
            Gdx.app.error("ItemLoader", "Erreur chargement shop: " + e.getMessage());
        }

        return inventory;
    }
}
