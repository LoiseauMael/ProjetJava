package com.github.LoiseauMael.RPG.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.github.LoiseauMael.RPG.Player;
import com.github.LoiseauMael.RPG.SwordMan;
import com.github.LoiseauMael.RPG.Wizard;
import com.github.LoiseauMael.RPG.items.*;

public class SaveManager {

    private static final String SAVE_FILE = "savegame.json";

    public static boolean saveExists() {
        return Gdx.files.local(SAVE_FILE).exists();
    }

    public static void deleteSave() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (file.exists()) {
            file.delete();
            Gdx.app.log("SaveManager", "Sauvegarde supprimee.");
        }
    }

    public static void saveGame(Player player) {
        SaveData data = new SaveData();

        // 1. Sauvegarder Stats
        data.playerClass = player.getClass().getSimpleName(); // "SwordMan" ou "Wizard"
        data.level = player.getLevel();
        data.exp = player.getExp();
        data.money = player.getMoney();
        data.currentPV = player.getPV();
        data.currentPM = player.getPM();
        data.x = player.get_positionX();
        data.y = player.get_positionY();

        // 2. Sauvegarder Inventaire
        for (Item item : player.getInventory()) {
            data.inventory.add(convertToData(item));
        }

        // 3. Sauvegarder Equipement
        if (player.getEquippedWeapon() != null) data.equippedWeapon = convertToData(player.getEquippedWeapon());
        if (player.getEquippedArmor() != null) data.equippedArmor = convertToData(player.getEquippedArmor());
        if (player.getEquippedRelic() != null) data.equippedRelic = convertToData(player.getEquippedRelic());

        // Ecriture fichier
        Json json = new Json();
        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(json.prettyPrint(data), false);
        Gdx.app.log("SaveManager", "Partie sauvegardée !");
    }

    public static Player loadGame() {
        if (!saveExists()) return null;

        FileHandle file = Gdx.files.local(SAVE_FILE);
        Json json = new Json();
        SaveData data = json.fromJson(SaveData.class, file.readString());

        // 1. Instanciation dynamique selon la classe sauvegardée
        Player player;
        if ("Wizard".equals(data.playerClass)) {
            player = Wizard.create(data.x, data.y);
        } else {
            player = SwordMan.create(data.x, data.y);
        }

        // 2. Charger Stats
        player.setLevel(data.level);
        player.setExp(data.exp);
        player.setMoney(data.money);
        player.setPV(data.currentPV);
        player.setPM(data.currentPM);

        // 3. Charger Inventaire
        player.getInventory().clear();
        for (SaveData.ItemData iData : data.inventory) {
            Item item = convertFromData(iData);
            if (item != null) player.addItem(item);
        }

        // 4. Charger Equipement
        if (data.equippedWeapon != null) {
            Item w = convertFromData(data.equippedWeapon);
            if (w instanceof Equipment) player.equip((Equipment) w);
        }
        if (data.equippedArmor != null) {
            Item a = convertFromData(data.equippedArmor);
            if (a instanceof Equipment) player.equip((Equipment) a);
        }
        if (data.equippedRelic != null) {
            Item r = convertFromData(data.equippedRelic);
            if (r instanceof Equipment) player.equip((Equipment) r);
        }

        Gdx.app.log("SaveManager", "Partie chargée !");
        return player;
    }

    // ==========================================
    // MÉTHODES DE CONVERSION (Celles qui manquaient)
    // ==========================================



    private static SaveData.ItemData convertToData(Item item) {
        SaveData.ItemData d = new SaveData.ItemData();
        d.name = item.getName();
        d.description = item.getDescription();
        d.count = item.getCount();

        if (item instanceof Weapon) {
            d.type = "WEAPON";
            d.bonus1 = ((Weapon) item).bonusFOR;
            d.bonus2 = ((Weapon) item).bonusFORM;
            d.requiredClass = getClassNameFromClass(((Weapon) item).getRequiredClass());
        } else if (item instanceof Armor) {
            d.type = "ARMOR";
            d.bonus1 = ((Armor) item).bonusDEF;
            d.bonus2 = ((Armor) item).bonusDEFM;
            d.requiredClass = getClassNameFromClass(((Armor) item).getRequiredClass());
        } else if (item instanceof Relic) {
            d.type = "RELIC";
            // On stocke les floats en int (x100) pour garder la précision simple
            d.bonus1 = (int)(((Relic) item).damageMultiplier * 100);
            d.bonus2 = (int)(((Relic) item).defenseMultiplier * 100);
        } else {
            d.type = "CONSUMABLE";
            if (item instanceof HealthPotion) d.bonus1 = ((HealthPotion)item).getAmount();
            else if (item instanceof ManaPotion) d.bonus1 = ((ManaPotion)item).getAmount();
            else if (item instanceof EnergyPotion) d.bonus1 = ((EnergyPotion)item).getAmount();
        }
        return d;
    }

    private static Item convertFromData(SaveData.ItemData d) {
        Item item = null;

        // Récupération de la classe requise
        Class<? extends Player> reqClass = null;
        if (d.requiredClass != null) {
            if (d.requiredClass.equals("SwordMan")) reqClass = SwordMan.class;
            else if (d.requiredClass.equals("Wizard")) reqClass = Wizard.class;
        }

        switch (d.type) {
            case "WEAPON":
                item = new Weapon(d.name, d.description, reqClass, d.bonus1, d.bonus2);
                break;
            case "ARMOR":
                item = new Armor(d.name, d.description, reqClass, d.bonus1, d.bonus2);
                break;
            case "RELIC":
                item = new Relic(d.name, d.description, d.bonus1 / 100f, d.bonus2 / 100f);
                break;
            case "CONSUMABLE":
                if (d.name.contains("Vie")) item = new HealthPotion(d.name, d.description, d.bonus1);
                else if (d.name.contains("Ether") || d.name.contains("Mana")) item = new ManaPotion(d.name, d.description, d.bonus1);
                else if (d.name.contains("Energ")) item = new EnergyPotion(d.name, d.description, d.bonus1);
                break;
        }

        if (item != null) item.setCount(d.count);
        return item;
    }

    // Petit helper pour convertir la Class en String proprement
    private static String getClassNameFromClass(Class<? extends Player> clazz) {
        if (clazz == null) return null;
        return clazz.getSimpleName();
    }
}
