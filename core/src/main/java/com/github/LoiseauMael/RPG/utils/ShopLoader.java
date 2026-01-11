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

        try {
            // Lecture du fichier
            JsonValue root = reader.parse(Gdx.files.internal("data/shop.json"));
            Gdx.app.log("ShopLoader", "Fichier JSON trouvé. Début du chargement...");

            for (JsonValue entry : root) {
                try {
                    String type = entry.getString("type", "UNKNOWN");
                    String name = entry.getString("name", "Objet Inconnu");
                    String desc = entry.getString("description", "");
                    int price = entry.getInt("price", 0);

                    Item item = null;

                    // --- 1. LECTURE INTELLIGENTE DES STATS ---
                    // On essaie de lire "statBonus", sinon "damage", sinon "defense"
                    int primaryStat = entry.getInt("statBonus", 0);
                    if (primaryStat == 0) primaryStat = entry.getInt("damage", 0);
                    if (primaryStat == 0) primaryStat = entry.getInt("defense", 0);

                    // Idem pour la stat secondaire
                    int secondaryStat = entry.getInt("secondaryStat", 0);
                    if (secondaryStat == 0) secondaryStat = entry.getInt("magic", 0);
                    if (secondaryStat == 0) secondaryStat = entry.getInt("magicDef", 0);

                    // Classe requise
                    String reqClassStr = entry.getString("requiredClass", "ANY");
                    Class reqClass = getClassFromString(reqClassStr);

                    // --- 2. CRÉATION DE L'OBJET ---
                    switch (type) {
                        case "POTION_HP":
                        case "CONSUMABLE":
                            int heal = entry.getInt("value", 0);
                            if (heal == 0) heal = entry.getInt("effectValue", 0);
                            item = new HealthPotion(name, desc, heal);
                            break;

                        case "POTION_MP":
                            int mana = entry.getInt("value", 0);
                            if (mana == 0) mana = entry.getInt("effectValue", 0);
                            item = new ManaPotion(name, desc, mana);
                            break;

                        case "WEAPON":
                            item = new Weapon(name, desc, reqClass, primaryStat, secondaryStat);
                            break;

                        case "ARMOR":
                            item = new Armor(name, desc, reqClass, primaryStat, secondaryStat);
                            break;

                        // C'EST ICI QU'IL MANQUAIT LE CAS POUR LES RELIQUES
                        case "RELIC":
                            float dmgMult = entry.getFloat("dmgMult", 1.0f);
                            float defMult = entry.getFloat("defMult", 1.0f);
                            item = new Relic(name, desc, dmgMult, defMult);
                            break;

                        default:
                            Gdx.app.error("ShopLoader", "Type ignoré : " + type);
                    }

                    if (item != null) {
                        game.merchantInventory.add(new Main.ShopEntry(item, price));
                    }

                } catch (Exception eItem) {
                    Gdx.app.error("ShopLoader", "Erreur sur l'objet : " + entry.getString("name", "???"));
                }
            }
        } catch (Exception eFile) {
            Gdx.app.error("ShopLoader", "Erreur lecture fichier JSON", eFile);
        }
    }

    private static Class getClassFromString(String className) {
        if (className.equalsIgnoreCase("Guerrier") || className.equalsIgnoreCase("SwordMan")) return SwordMan.class;
        if (className.equalsIgnoreCase("Mage") || className.equalsIgnoreCase("Wizard")) return Wizard.class;
        return null;
    }
}
