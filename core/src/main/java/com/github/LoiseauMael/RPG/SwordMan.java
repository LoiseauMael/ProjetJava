package com.github.LoiseauMael.RPG;

public class SwordMan extends Player {

    public SwordMan(float x, float y) {
        // On appelle le nouveau constructeur de Player :
        // x, y, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, CheminTexture
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

        // Initialisation des compétences (Arts) du niveau 1
        updateKnownSkills();
    }

    // Factory method statique pour faciliter la création
    public static SwordMan create(float x, float y) {
        return new SwordMan(x, y);
    }
}
