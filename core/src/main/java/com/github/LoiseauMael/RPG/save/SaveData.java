package com.github.LoiseauMael.RPG.save;

import java.util.ArrayList;

public class SaveData {
    // Infos de base
    public String playerClass;
    public int level, exp, money;
    public int currentPV, currentPM;
    public float x, y;

    // État du monde
    public String currentMapName; // RENOMMÉ pour correspondre à Main.java
    public ArrayList<Integer> deadEnemyIds = new ArrayList<>();

    // Inventaire
    public ArrayList<ItemData> inventory = new ArrayList<>();

    // Equipement
    public ItemData equippedWeapon;
    public ItemData equippedArmor;
    public ItemData equippedRelic;

    public static class ItemData {
        public String type;
        public String specificType;
        public String name;
        public String description;
        public int count;
        public float bonus1;
        public float bonus2;
        public String requiredClass;

        public ItemData() {}
    }
}
