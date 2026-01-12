package com.github.LoiseauMael.RPG.model.entities;

/**
 * Spécialisation de la classe {@link Player} représentant l'archétype "Mage".
 * <p>
 * <b>Profil de statistiques :</b>
 * <ul>
 * <li><b>PV :</b> Faibles (Fragile).</li>
 * <li><b>Force :</b> Faible.</li>
 * <li><b>Magie :</b> Très élevée (Dégâts magiques massifs).</li>
 * <li><b>Mana (PM) :</b> Réservoir important pour lancer de nombreux sorts.</li>
 * <li><b>Compétences :</b> Orientées Sortilèges (coût en PM).</li>
 * </ul>
 */
public class Wizard extends Player {

    /**
     * Crée une nouvelle instance de Mage avec ses statistiques de base prédéfinies.
     * Charge automatiquement la SpriteSheet "WizardSpriteSheet.png".
     *
     * @param x Position X initiale sur la carte.
     * @param y Position Y initiale sur la carte.
     */
    public Wizard(float x, float y) {
        // Appel au constructeur parent avec les stats d'équilibrage :
        // PV=70, PM=50, PA=6
        // FOR=4, DEF=3, FORM=15, DEFM=10
        // VIT=6, DEP=4
        super(x, y,
            70,  // PV (Fragile)
            50,  // PM (Beaucoup de mana)
            6,   // PA
            4,   // FOR (Faible physique)
            3,   // DEF
            15,  // FORM (Puissant magie)
            10,  // DEFM
            6,   // VIT
            4,   // DEP
            "WizardSpriteSheet.png"
        );

        this.nom = "Mage";

        // Débloque immédiatement les compétences de niveau 1
        updateKnownSkills();
    }

    /**
     * Méthode Factory (Fabrique) pour instancier un Mage de manière concise.
     *
     * @param x Position X.
     * @param y Position Y.
     * @return Une nouvelle instance de {@link Wizard}.
     */
    public static Wizard create(float x, float y) {
        return new Wizard(x, y);
    }
}
