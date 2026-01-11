package com.github.LoiseauMael.RPG.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.Player;
import com.github.LoiseauMael.RPG.SwordMan;
import com.github.LoiseauMael.RPG.Wizard;
import com.github.LoiseauMael.RPG.items.*;

public class SaveManager {

    private static final String SAVE_FILE = "save.json";

    public static void saveGame(Main game) {
        if (game.player == null) return;

        Json json = new Json();
        SaveData data = new SaveData();
        Player player = game.player;

        // --- 1. Sauvegarde du Joueur ---
        data.playerClass = (player instanceof Wizard) ? "Wizard" : "SwordMan";
        data.level = player.getLevel();
        data.exp = player.getExp();
        data.money = player.getMoney();
        data.currentPV = player.getPV();
        data.currentPM = player.getPM();

        data.x = player.get_positionX();
        data.y = player.get_positionY();

        // --- 2. Sauvegarde de l'Inventaire ---
        for (Item item : player.getInventory()) {
            data.inventory.add(createItemData(item));
        }

        // --- 3. Sauvegarde de l'Équipement ---
        if (player.getEquippedWeapon() != null) data.equippedWeapon = createItemData(player.getEquippedWeapon());
        if (player.getEquippedArmor() != null) data.equippedArmor = createItemData(player.getEquippedArmor());
        if (player.getEquippedRelic() != null) data.equippedRelic = createItemData(player.getEquippedRelic());

        // --- 4. Sauvegarde du Monde ---
        // On sauvegarde le nom de la carte actuelle pour pouvoir la recharger au redémarrage
        data.currentMapName = game.currentMapName;

        // Gestion des ennemis morts (Boucle pour éviter l'erreur de type Array vs ArrayList)
        data.deadEnemyIds.clear();
        if (game.deadEnemyIds != null) {
            for (Integer id : game.deadEnemyIds) {
                data.deadEnemyIds.add(id);
            }
        }

        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(json.prettyPrint(data), false);
        System.out.println("Partie sauvegardée : Map=" + data.currentMapName + " Pos=(" + data.x + "," + data.y + ")");
    }

    public static void loadGame(Main game) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) return;

        Json json = new Json();
        SaveData data = json.fromJson(SaveData.class, file.readString());

        // 1. Recréation du Joueur
        if ("Wizard".equals(data.playerClass)) {
            game.player = Wizard.create(0, 0);
        } else {
            game.player = SwordMan.create(0, 0);
        }

        // Recharger les compétences (Important !)
        game.player.updateKnownSkills();

        // 2. Application de la position
        game.player.set_position(data.x, data.y);

        // 3. Restauration Stats
        game.player.setLevel(data.level);
        game.player.setExp(data.exp);
        game.player.setMoney(data.money);
        game.player.setPV(data.currentPV);
        game.player.setPM(data.currentPM);

        // 4. Restauration de l'Inventaire
        if (data.inventory != null) {
            for (SaveData.ItemData itemData : data.inventory) {
                Item item = createItemFromData(itemData);
                if (item != null) game.player.addItem(item);
            }
        }

        // 5. Restauration de l'Équipement
        if (data.equippedWeapon != null) {
            Item w = createItemFromData(data.equippedWeapon);
            if (w instanceof Weapon) game.player.equip((Weapon) w);
        }
        if (data.equippedArmor != null) {
            Item a = createItemFromData(data.equippedArmor);
            if (a instanceof Armor) game.player.equip((Armor) a);
        }
        if (data.equippedRelic != null) {
            Item r = createItemFromData(data.equippedRelic);
            if (r instanceof Relic) game.player.equip((Relic) r);
        }

        // 6. Restauration du Monde
        // C'est ICI que se corrige le bug de l'écran noir/mauvaise map
        if (data.currentMapName != null && !data.currentMapName.isEmpty()) {
            game.currentMapName = data.currentMapName;
        } else {
            game.currentMapName = "map.tmx"; // Sécurité
        }

        game.deadEnemyIds.clear();
        if (data.deadEnemyIds != null) {
            for (Integer id : data.deadEnemyIds) {
                game.deadEnemyIds.add(id);
            }
        }

        System.out.println("SaveManager chargé. Map: " + game.currentMapName + " Pos: " + game.player.get_positionX() + "," + game.player.get_positionY());
    }

    // --- Helpers inchangés ---
    private static SaveData.ItemData createItemData(Item item) {
        SaveData.ItemData data = new SaveData.ItemData();
        data.name = item.getName();
        data.description = item.getDescription();
        data.count = item.getCount();

        if (item instanceof Weapon) {
            data.type = "WEAPON";
            Weapon w = (Weapon) item;
            data.bonus1 = w.bonusFOR;
            data.bonus2 = w.bonusFORM;
            data.requiredClass = getClassName(w.getRequiredClass());
        } else if (item instanceof Armor) {
            data.type = "ARMOR";
            Armor a = (Armor) item;
            data.bonus1 = a.bonusDEF;
            data.bonus2 = a.bonusDEFM;
            data.requiredClass = getClassName(a.getRequiredClass());
        } else if (item instanceof Relic) {
            data.type = "RELIC";
            Relic r = (Relic) item;
            data.bonus1 = r.damageMultiplier;
            data.bonus2 = r.defenseMultiplier;
        } else if (item instanceof Consumable) {
            data.type = "CONSUMABLE";
            if (item instanceof HealthPotion) {
                data.specificType = "HEALTH";
                data.bonus1 = ((HealthPotion) item).getAmount();
            } else if (item instanceof ManaPotion) {
                data.specificType = "MANA";
                data.bonus1 = ((ManaPotion) item).getAmount();
            } else if (item instanceof EnergyPotion) {
                data.specificType = "ENERGY";
                data.bonus1 = ((EnergyPotion) item).getAmount();
            }
        }
        return data;
    }

    private static Item createItemFromData(SaveData.ItemData data) {
        Class<? extends Player> reqClass = resolveClass(data.requiredClass);
        if ("WEAPON".equals(data.type)) {
            Weapon w = new Weapon(data.name, data.description, reqClass, (int) data.bonus1, (int) data.bonus2);
            w.setCount(data.count);
            return w;
        } else if ("ARMOR".equals(data.type)) {
            Armor a = new Armor(data.name, data.description, reqClass, (int) data.bonus1, (int) data.bonus2);
            a.setCount(data.count);
            return a;
        } else if ("RELIC".equals(data.type)) {
            Relic r = new Relic(data.name, data.description, data.bonus1, data.bonus2);
            r.setCount(data.count);
            return r;
        } else if ("CONSUMABLE".equals(data.type)) {
            Item c = null;
            if ("HEALTH".equals(data.specificType)) c = new HealthPotion(data.name, data.description, (int) data.bonus1);
            else if ("MANA".equals(data.specificType)) c = new ManaPotion(data.name, data.description, (int) data.bonus1);
            else if ("ENERGY".equals(data.specificType)) c = new EnergyPotion(data.name, data.description, (int) data.bonus1);
            if (c != null) c.setCount(data.count);
            return c;
        }
        return null;
    }

    private static String getClassName(Class<? extends Player> c) {
        if (c == null) return null;
        if (c.equals(Wizard.class)) return "Wizard";
        if (c.equals(SwordMan.class)) return "SwordMan";
        return null;
    }

    private static Class<? extends Player> resolveClass(String name) {
        if ("Wizard".equals(name)) return Wizard.class;
        if ("SwordMan".equals(name)) return SwordMan.class;
        return null;
    }

    public static boolean saveExists() { return Gdx.files.local(SAVE_FILE).exists(); }
    public static void deleteSave() { if (saveExists()) Gdx.files.local(SAVE_FILE).delete(); }
}
