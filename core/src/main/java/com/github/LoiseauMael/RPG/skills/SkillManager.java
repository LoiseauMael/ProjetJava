package com.github.LoiseauMael.RPG.skills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;

/**
 * Gestionnaire global des compétences.
 * Chargé au démarrage, il lit le fichier "data/skills.json" et fournit les compétences
 * aux entités lorsqu'elles montent de niveau.
 */
public class SkillManager {
    /** Liste complète de toutes les compétences du jeu chargées en mémoire. */
    private static Array<Skill> allSkills = new Array<>();

    /**
     * Charge les compétences depuis le fichier JSON des assets.
     * Configure le sérialiseur JSON pour gérer correctement les Enums (SkillType, etc.).
     */
    public static void loadSkills() {
        Json json = new Json();

        // Configuration du serializer pour lire les Enums sous forme de chaîne de caractères
        json.setSerializer(Skill.SkillType.class, new Json.Serializer<Skill.SkillType>() {
            public void write(Json json, Skill.SkillType object, Class knownType) {}
            public Skill.SkillType read(Json json, JsonValue jsonData, Class type) {
                return Skill.SkillType.valueOf(jsonData.asString());
            }
        });
        // Note: LibGDX gère souvent les autres Enums automatiquement s'ils correspondent exactement.

        // Lecture du fichier
        ArrayList<Skill> list = json.fromJson(ArrayList.class, Skill.class, Gdx.files.internal("data/skills.json"));
        allSkills.clear();
        for(Skill s : list) {
            allSkills.add(s);
        }
        Gdx.app.log("Skills", "Loaded " + allSkills.size + " skills.");
    }

    /**
     * Récupère la liste des compétences disponibles pour une classe et un niveau donnés.
     * Utilisé lors de la montée de niveau ou du chargement de la partie.
     *
     * @param className Nom de la classe du joueur ("Guerrier", "Mage").
     * @param level Niveau actuel du joueur.
     * @return Une liste de compétences éligibles.
     */
    public static Array<Skill> getSkillsFor(String className, int level) {
        Array<Skill> eligible = new Array<>();
        for (Skill s : allSkills) {
            // Vérifie si le skill est pour "TOUS" ou pour la classe spécifique
            boolean classMatch = s.requiredClass.equalsIgnoreCase("ANY") || s.requiredClass.equalsIgnoreCase(className);
            if (classMatch && level >= s.requiredLevel) {
                eligible.add(s);
            }
        }
        return eligible;
    }

    /**
     * Recherche une compétence par son ID unique.
     */
    public static Skill getSkillById(String id) {
        for(Skill s : allSkills) if(s.id.equals(id)) return s;
        return null;
    }
}
