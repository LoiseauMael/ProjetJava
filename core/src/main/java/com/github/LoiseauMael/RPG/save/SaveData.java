package com.github.LoiseauMael.RPG.save;

import java.util.ArrayList;

public class SaveData {
    // Infos de base
    public String playerClass; // "SwordMan" ou "Wizard"
    public int level, exp, money;
    public int currentPV, currentPM;
    public float x, y;

    // Inventaire (Liste simplifiée)
    public ArrayList<ItemData> inventory = new ArrayList<>();

    // Equipement
    public ItemData equippedWeapon;
    public ItemData equippedArmor;
    public ItemData equippedRelic;

    // Classe interne pour sauvegarder un item
    public static class ItemData {
        public String type; // "WEAPON", "ARMOR", "RELIC", "CONSUMABLE"
        public String name;
        public String description;
        public int count;

        // Stats spécifiques (si equipement)
        public int bonus1; // FOR ou DEF ou DmgMulti
        public int bonus2; // FORM ou DEFM ou ReducMulti
        public String requiredClass; // Nom de la classe requise

        public ItemData() {} // Pour Json
    }
}
