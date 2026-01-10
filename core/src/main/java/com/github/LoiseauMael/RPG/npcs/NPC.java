package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.LoiseauMael.RPG.Entity;

public abstract class NPC extends Entity {
    protected String name;
    protected String[] dialogues;
    protected int currentDialogueIndex = 0;

    // Variables pour l'animation
    private Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    private float stateTime;
    protected static final int FRAME_COLS = 3;
    protected static final int FRAME_ROWS = 4;

    /**
     * Constructeur principal prenant le chemin de la texture (Recommandé).
     */
    public NPC(float x, float y, String name, String[] dialogues, String texturePath) {
        // 1. Chargement de la texture
        super(x, y, new Sprite(new Texture(Gdx.files.internal(texturePath))));
        this.name = name;
        this.dialogues = dialogues;

        // 2. Initialisation du découpage et des animations
        initAnimations();

        // 3. Mise à l'échelle (1 unité = 1 tuile)
        // On force la taille à 1x1 pour que le sprite tienne dans la grille du jeu
        if (this.sprite != null) {
            this.sprite.setSize(2f, 2f);
        }

        // 4. Collision
        this.setCollisionBounds(1f, 1f, 0, 0);
    }

    /**
     * Constructeur alternatif si on a déjà un Sprite.
     * Note: Ce constructeur suppose que le sprite contient bien la feuille de style complète 3x4.
     */
    public NPC(float x, float y, String name, String[] dialogues, Sprite sprite) {
        super(x, y, sprite);
        this.name = name;
        this.dialogues = dialogues;

        if (sprite != null) {
            // Important : on initialise aussi les animations ici si possible
            if (sprite.getTexture() != null) {
                initAnimations();
            }
            sprite.setSize(2f, 2f);
            this.setCollisionBounds(1f, 1f, 0, 0);
        }
    }

    private void initAnimations() {
        Texture texture = this.getSprite().getTexture();
        // Découpage de la texture en grille 3x4
        TextureRegion[][] tmp = TextureRegion.split(texture,
            texture.getWidth() / FRAME_COLS,
            texture.getHeight() / FRAME_ROWS);

        // Initialisation des animations (0.2f = vitesse)
        // Ligne 0 : Bas, Ligne 1 : Gauche, Ligne 2 : Droite, Ligne 3 : Haut
        walkDown  = new Animation<>(0.2f, tmp[0]);
        walkLeft  = new Animation<>(0.2f, tmp[1]);
        walkRight = new Animation<>(0.2f, tmp[2]);
        walkUp    = new Animation<>(0.2f, tmp[3]);

        // Frame par défaut (Regarde vers le bas, colonne du milieu)
        if (tmp.length > 0 && tmp[0].length > 1) {
            this.getSprite().setRegion(tmp[0][1]);
        }
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        // On ne force plus l'animation ici pour permettre à 'lookAt' de changer la direction.
        // Si vous voulez ajouter une animation de marche (patrouille), c'est ici qu'il faudra le faire.

        super.update(delta); // Important : met à jour la position physique dans Entity
    }

    public String getName() {
        return name;
    }

    public String getCurrentDialogue() {
        if (dialogues == null || dialogues.length == 0) return "...";
        return dialogues[currentDialogueIndex];
    }

    public boolean advanceDialogue() {
        currentDialogueIndex++;
        if (currentDialogueIndex >= dialogues.length) {
            currentDialogueIndex = 0;
            return false;
        }
        return true;
    }

    /* * CORRECTION IMPORTANTE :
     * La méthode draw() a été supprimée.
     * La classe Entity possède déjà une méthode draw() qui gère :
     * sprite.setPosition(x, y) puis sprite.draw(batch).
     * En la supprimant ici, on s'assure que le sprite suit bien le PNJ.
     */

    /**
     * Oriente le PNJ vers la cible (le joueur).
     */
    @Override
    public void lookAt(Entity target) {
        super.lookAt(target); // Calcule la 'currentDirection' (0, 1, 2, ou 3)

        // On met à jour le sprite immédiatement pour regarder dans la bonne direction
        // On utilise la frame "Idle" (indice 1, celle du milieu) de l'animation correspondante
        if (walkDown != null) {
            switch(currentDirection) {
                case 0: // Bas
                    this.getSprite().setRegion(walkDown.getKeyFrames()[1]);
                    break;
                case 1: // Gauche
                    this.getSprite().setRegion(walkLeft.getKeyFrames()[1]);
                    break;
                case 2: // Droite
                    this.getSprite().setRegion(walkRight.getKeyFrames()[1]);
                    break;
                case 3: // Haut
                    this.getSprite().setRegion(walkUp.getKeyFrames()[1]);
                    break;
            }
        }
    }
}
