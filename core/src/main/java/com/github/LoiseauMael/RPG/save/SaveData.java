package com.github.LoiseauMael.RPG.save;

import java.util.ArrayList;

public class SaveData {
    // Infos de base
    public String playerClass;
    public int level, exp, money;
    public int currentPV, currentPM;
    public float x, y;

    // État du monde
    public String currentMap;
    public ArrayList<Integer> deadEnemyIds = new ArrayList<>();

    // Inventaire (Liste des objets dans le sac)
    public ArrayList<ItemData> inventory = new ArrayList<>();

    // Equipement actuel (Sauvegardé à part pour le rééquiper facilement)
    public ItemData equippedWeapon;
    public ItemData equippedArmor;
    public ItemData equippedRelic;

    // Classe interne pour sauvegarder un item
    public static class ItemData {
        public String type; // "WEAPON", "ARMOR", "RELIC", "CONSUMABLE"
        public String specificType; // "HEALTH", "MANA", "ENERGY" (Pour savoir quelle potion recréer)

        public String name;
        public String description;
        public int count;

        // On passe en float pour gérer les Reliques (ex: 1.2f) et on castera en int pour les Armes
        public float bonus1;
        public float bonus2;

        public String requiredClass; // "Wizard", "SwordMan" ou null

        public ItemData() {}
    }
}
