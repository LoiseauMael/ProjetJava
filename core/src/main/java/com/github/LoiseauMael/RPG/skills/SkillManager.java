package com.github.LoiseauMael.RPG.skills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;

public class SkillManager {
    private static Array<Skill> allSkills = new Array<>();

    public static void loadSkills() {
        Json json = new Json();
        // Configuration pour lire correctement les types Enum depuis le JSON string
        json.setSerializer(Skill.SkillType.class, new Json.Serializer<Skill.SkillType>() {
            public void write(Json json, Skill.SkillType object, Class knownType) {}
            public Skill.SkillType read(Json json, JsonValue jsonData, Class type) {
                return Skill.SkillType.valueOf(jsonData.asString());
            }
        });
        // Répéter pour EffectType et TargetType si besoin, sinon Gdx le gère souvent automatiquement si les noms matchent.

        // Lecture du fichier
        ArrayList<Skill> list = json.fromJson(ArrayList.class, Skill.class, Gdx.files.internal("data/skills.json"));
        allSkills.clear();
        for(Skill s : list) {
            allSkills.add(s);
        }
        Gdx.app.log("Skills", "Loaded " + allSkills.size + " skills.");
    }

    public static Array<Skill> getSkillsFor(String className, int level) {
        Array<Skill> eligible = new Array<>();
        for (Skill s : allSkills) {
            boolean classMatch = s.requiredClass.equalsIgnoreCase("ANY") || s.requiredClass.equalsIgnoreCase(className);
            if (classMatch && level >= s.requiredLevel) {
                eligible.add(s);
            }
        }
        return eligible;
    }

    public static Skill getSkillById(String id) {
        for(Skill s : allSkills) if(s.id.equals(id)) return s;
        return null;
    }
}
