package com.github.LoiseauMael.RPG.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.model.entities.Player;
import com.github.LoiseauMael.RPG.model.entities.SwordMan;
import com.github.LoiseauMael.RPG.model.entities.Wizard;
import com.github.LoiseauMael.RPG.items.*;

/**
 * Gestionnaire statique responsable de la sauvegarde et du chargement de la partie.
 * <p>
 * Il convertit l'état actuel du jeu (Main, Player) en {@link SaveData} pour l'écriture,
 * et inversement pour la lecture.
 */
public class SaveManager {

    private static final String SAVE_FILE = "save.json";

    /**
     * Sauvegarde l'état actuel du jeu dans un fichier JSON local.
     * @param game L'instance principale du jeu contenant les données à sauver.
     */
    public static void saveGame(Main game) {
        if (game.player == null) return;

        Json json = new Json();
        SaveData data = new SaveData();
        Player player = game.player;

        // --- 1. Extraction des données du Joueur ---
        data.playerClass = (player instanceof Wizard) ? "Wizard" : "SwordMan";
        data.level = player.getLevel();
        data.exp = player.getExp();
        data.money = player.getMoney();
        data.currentPV = player.getPV();
        data.currentPM = player.getPM();

        data.x = player.get_positionX();
        data.y = player.get_positionY();

        // --- 2. Conversion de l'Inventaire ---
        for (Item item : player.getInventory()) {
            data.inventory.add(createItemData(item));
        }

        // --- 3. Conversion de l'Équipement ---
        if (player.getEquippedWeapon() != null) data.equippedWeapon = createItemData(player.getEquippedWeapon());
        if (player.getEquippedArmor() != null) data.equippedArmor = createItemData(player.getEquippedArmor());
        if (player.getEquippedRelic() != null) data.equippedRelic = createItemData(player.getEquippedRelic());

        // --- 4. Sauvegarde de l'état du Monde ---
        data.currentMapName = game.currentMapName;

        // Copie des IDs d'ennemis morts (copie défensive pour éviter les problèmes de références)
        data.deadEnemyIds.clear();
        if (game.deadEnemyIds != null) {
            for (Integer id : game.deadEnemyIds) {
                data.deadEnemyIds.add(id);
            }
        }

        // Écriture sur le disque
        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(json.prettyPrint(data), false);
        System.out.println("Partie sauvegardée : Map=" + data.currentMapName + " Pos=(" + data.x + "," + data.y + ")");
    }

    /**
     * Charge la partie depuis le fichier JSON et reconstruit l'état du jeu.
     * <p>
     * Cette méthode réinstancie le Joueur, vide et remplit l'inventaire, et restaure la carte.
     * @param game L'instance du jeu à mettre à jour.
     */
    public static void loadGame(Main game) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) return;

        Json json = new Json();
        SaveData data = json.fromJson(SaveData.class, file.readString());

        // 1. Recréation de l'instance Joueur (Factory)
        if ("Wizard".equals(data.playerClass)) {
            game.player = Wizard.create(0, 0);
        } else {
            game.player = SwordMan.create(0, 0);
        }

        // Important : Réapprendre les skills basés sur le niveau chargé
        game.player.updateKnownSkills();

        // 2. Restauration Position & Stats
        game.player.set_position(data.x, data.y);
        game.player.setLevel(data.level);
        game.player.setExp(data.exp);
        game.player.setMoney(data.money);
        game.player.setPV(data.currentPV);
        game.player.setPM(data.currentPM);

        // 3. Reconstruction de l'Inventaire (ItemData -> Item Java)
        if (data.inventory != null) {
            for (SaveData.ItemData itemData : data.inventory) {
                Item item = createItemFromData(itemData);
                if (item != null) game.player.addItem(item);
            }
        }

        // 4. Rééquipement
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

        // 5. Restauration de la Carte
        if (data.currentMapName != null && !data.currentMapName.isEmpty()) {
            game.currentMapName = data.currentMapName;
        } else {
            game.currentMapName = "map.tmx"; // Fallback de sécurité
        }

        game.deadEnemyIds.clear();
        if (data.deadEnemyIds != null) {
            for (Integer id : data.deadEnemyIds) {
                game.deadEnemyIds.add(id);
            }
        }

        System.out.println("Partie chargée avec succès.");
    }

    // --- Méthodes utilitaires de conversion (Helpers) ---

    /** Convertit un Item Java en ItemData sérialisable. */
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
            // Gestion des sous-types de potions
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

    /** Convertit un ItemData chargé en objet Item Java concret. */
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
