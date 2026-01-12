package com.github.LoiseauMael.RPG.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.Main.ShopEntry;
import com.github.LoiseauMael.RPG.model.entities.SwordMan;
import com.github.LoiseauMael.RPG.model.entities.Wizard;
import com.github.LoiseauMael.RPG.items.*;

/**
 * Utilitaire générique pour charger des listes d'objets depuis un JSON.
 * <p>
 * Similaire à ShopLoader, mais potentiellement utilisé pour d'autres contextes (loot, coffres).
 * Note : Sa logique de détection des types de potions est basée sur le nom de l'objet.
 */
public class ItemLoader {

    /**
     * Charge une liste d'objets avec leurs prix.
     *
     * @param jsonPath Le chemin interne vers le fichier JSON.
     * @return Une liste de ShopEntry (Item + Prix).
     */
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
                        // Détection simple du type de potion basée sur le nom
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
                        // Logique pour les armures (non implémentée dans cet extrait)
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
