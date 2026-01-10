package com.github.LoiseauMael.RPG.quests;

import java.util.HashMap;

public class QuestManager {
    // Stocke l'état des quêtes : "started", "completed", ou null (pas commencée)
    private HashMap<String, String> quests = new HashMap<>();

    public void startQuest(String questId) {
        if (!quests.containsKey(questId)) {
            quests.put(questId, "started");
            System.out.println("Quête commencée : " + questId);
        }
    }

    public void completeQuest(String questId) {
        if ("started".equals(quests.get(questId))) {
            quests.put(questId, "completed");
            System.out.println("Quête terminée : " + questId);
        }
    }

    public boolean isQuestStarted(String questId) {
        return "started".equals(quests.get(questId));
    }

    public boolean isQuestCompleted(String questId) {
        return "completed".equals(quests.get(questId));
    }
}
