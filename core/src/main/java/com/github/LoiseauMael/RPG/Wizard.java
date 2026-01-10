package com.github.LoiseauMael.RPG;

public class Wizard extends Player {

    public Wizard(float x, float y) {
        // x, y, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, CheminTexture
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
            "WizardSpriteSheet.png" // Assurez-vous que ce fichier existe
        );

        this.nom = "Mage";

        // Le mage commence avec un sort
        // Note: Assurez-vous d'avoir SpellAction importé ou disponible
        // this.spells.add(new SpellAction("Boule de Feu", 10, 20));
    }

    public static Wizard create(float x, float y) {
        return new Wizard(x, y);
    }
}
