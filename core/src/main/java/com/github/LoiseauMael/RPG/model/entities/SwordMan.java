package com.github.LoiseauMael.RPG.model.entities;

/**
 * Spécialisation de la classe {@link Player} représentant l'archétype "Guerrier".
 * <p>
 * <b>Profil de statistiques :</b>
 * <ul>
 * <li><b>PV :</b> Élevés (Tank).</li>
 * <li><b>Force :</b> Très élevée (Dégâts physiques).</li>
 * <li><b>Magie :</b> Faible (Peu de Mana, sorts peu efficaces).</li>
 * <li><b>Compétences :</b> Orientées Arts martiaux (coût en PA).</li>
 * </ul>
 */
public class SwordMan extends Player {

    /**
     * Crée une nouvelle instance de Guerrier avec ses statistiques de base prédéfinies.
     * Charge automatiquement la SpriteSheet "SwordmanSpriteSheet.png".
     *
     * @param x Position X initiale sur la carte.
     * @param y Position Y initiale sur la carte.
     */
    public SwordMan(float x, float y) {
        // Appel au constructeur parent avec les stats d'équilibrage :
        // PV=100, PM=20, PA=6
        // FOR=12, DEF=5, FORM=2, DEFM=3
        // VIT=5, DEP=4
        super(x, y,
            100, // PV
            20,  // PM
            6,   // PA
            12,  // FOR (Fort)
            5,   // DEF
            2,   // FORM (Faible magie)
            3,   // DEFM
            5,   // VIT
            4,   // DEP
            "SwordmanSpriteSheet.png"
        );

        this.nom = "Guerrier";

        // Débloque immédiatement les compétences de niveau 1
        updateKnownSkills();
    }

    /**
     * Méthode Factory (Fabrique) pour instancier un Guerrier de manière concise.
     * Utilisé principalement par le sélecteur de classe (ClassSelectionState).
     *
     * @param x Position X.
     * @param y Position Y.
     * @return Une nouvelle instance de {@link SwordMan}.
     */
    public static SwordMan create(float x, float y) {
        return new SwordMan(x, y);
    }
}
