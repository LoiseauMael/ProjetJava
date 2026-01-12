package com.github.LoiseauMael.RPG.model.entities;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.BattleAction;
import com.github.LoiseauMael.RPG.model.entities.Enemy;

/**
 * Implémentation concrète et flexible d'un ennemi.
 * <p>
 * Cette classe est conçue pour être instanciée dynamiquement via le chargeur de carte (MapLoader).
 * Elle lit toutes ses statistiques depuis les propriétés personnalisées d'un objet Tiled.
 */
public class GenericEnemy extends Enemy {

    private String aiType;

    /**
     * Construit un ennemi à partir des propriétés de la carte Tiled.
     * <p>
     * Propriétés lues (avec valeurs par défaut) :
     * <ul>
     * <li><b>hp, mp, ap</b> : Stats de ressources (défaut: 50, 0, 3).</li>
     * <li><b>str, def, mag, res, spd</b> : Stats de combat.</li>
     * <li><b>gold / money</b> : Or donné à la mort.</li>
     * <li><b>texture</b> : Fichier image (défaut: "assets/GoblinSpriteSheet.png").</li>
     * <li><b>ai</b> : Type d'IA ("normal", "boss"...).</li>
     * </ul>
     * <p>
     * <b>Note importante :</b> Les ennemis génériques sont agrandis (taille x2) par rapport à la grille
     * pour les rendre plus imposants visuellement.
     *
     * @param x Position X.
     * @param y Position Y.
     * @param id Identifiant unique Tiled.
     * @param props Objet MapProperties contenant les données.
     */
    public GenericEnemy(float x, float y, int id, MapProperties props) {
        super(
            x, y,
            props.get("level", 1, Integer.class),
            props.get("exp", 10, Integer.class),
            props.get("hp", 50, Integer.class),
            props.get("mp", 0, Integer.class),
            props.get("ap", 3, Integer.class),
            props.get("str", 5, Integer.class),
            props.get("def", 2, Integer.class),
            props.get("mag", 0, Integer.class),
            props.get("res", 0, Integer.class), // Résistance magique (DEFM)
            props.get("spd", 5, Integer.class),
            props.get("move", 3, Integer.class),
            props.get("texture", "assets/GoblinSpriteSheet.png", String.class)
        );

        this.id = id;
        this.nom = props.get("name", "Monstre", String.class);

        // Gestion flexible de la propriété "argent" (deux noms possibles dans Tiled)
        int gold = props.get("gold", 0, Integer.class);
        if (gold == 0) gold = props.get("money", 5, Integer.class);
        this.setMoney(gold);

        this.aiType = props.get("ai", "normal", String.class);

        // --- MISE À L'ÉCHELLE VISUELLE ---
        if (this.sprite != null) {
            // On double la taille visuelle (2 tuiles de haut et de large) pour un aspect "Menace"
            this.sprite.setSize(2f, 2f);
        }

        // Ajustement de la hitbox pour correspondre au visuel agrandi
        // (1.5f au lieu de 2f pour éviter de coincer trop facilement dans les couloirs étroits)
        this.setCollisionBounds(1.5f, 1.5f, 0.25f, 0);
    }

    /**
     * Configure les attaques disponibles.
     * Par défaut, ajoute une attaque simple (100% de probabilité).
     * <p>
     * Pourrait être étendu pour lire des sorts depuis les propriétés Tiled (ex: "skill1", "skill2").
     */
    @Override
    protected void setupMoves() {
        this.availableMoves = new Array<>();

        // Ajout de l'attaque de base par défaut
        this.availableMoves.add(new EnemyMove(new AttackAction(), 100));

        if ("boss".equals(aiType)) {
            // Placeholder pour une logique de boss future
            // Ex: availableMoves.add(new EnemyMove(new UltimateAction(), 20));
        }
    }
}
