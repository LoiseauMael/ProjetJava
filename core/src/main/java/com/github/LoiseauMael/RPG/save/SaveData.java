package com.github.LoiseauMael.RPG.save;

import java.util.ArrayList;

/**
 * Classe DTO (Data Transfer Object) représentant la structure des données sauvegardées.
 * <p>
 * Cette classe ne contient aucune logique, seulement des champs publics destinés à être
 * lus et écrits automatiquement par {@link com.badlogic.gdx.utils.Json}.
 * Elle sert de pont entre les objets complexes du jeu (Player, Item) et le fichier texte.
 */
public class SaveData {
    // --- INFOS JOUEUR ---
    /** Type de classe ("Wizard" ou "SwordMan"). */
    public String playerClass;
    public int level, exp, money;
    public int currentPV, currentPM;
    public float x, y;

    // --- ÉTAT DU MONDE ---
    /** Nom du fichier .tmx de la carte actuelle (ex: "map.tmx"). */
    public String currentMapName;
    /** Liste des ID uniques des ennemis vaincus (pour ne pas les faire réapparaître). */
    public ArrayList<Integer> deadEnemyIds = new ArrayList<>();

    // --- INVENTAIRE ---
    public ArrayList<ItemData> inventory = new ArrayList<>();

    // --- ÉQUIPEMENT ACTIF ---
    public ItemData equippedWeapon;
    public ItemData equippedArmor;
    public ItemData equippedRelic;

    /**
     * Sous-classe représentant un Item de manière simplifiée pour le JSON.
     * Stocke les stats brutes au lieu des objets Java complexes.
     */
    public static class ItemData {
        public String type;         // WEAPON, ARMOR, RELIC, CONSUMABLE
        public String specificType; // HEALTH, MANA (si consommable)
        public String name;
        public String description;
        public int count;
        public float bonus1;        // Stocke FOR/DEF ou montant de soin
        public float bonus2;        // Stocke FORM/DEFM
        public String requiredClass;// "Wizard", "SwordMan", ou null

        public ItemData() {}
    }
}
