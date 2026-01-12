package com.github.LoiseauMael.RPG.npcs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.LoiseauMael.RPG.model.entities.Entity;

/**
 * Classe abstraite de base représentant un Personnage Non-Joueur (PNJ).
 * <p>
 * Responsabilités :
 * <ul>
 * <li><b>Interaction :</b> Stocke et gère le défilement des dialogues.</li>
 * <li><b>Rendu :</b> Charge et découpe automatiquement une SpriteSheet (format 3x4).</li>
 * <li><b>Animation :</b> Gère l'orientation visuelle (regarder le joueur) via {@link #lookAt(Entity)}.</li>
 * <li><b>Physique :</b> Définit une collision standard pour bloquer le joueur.</li>
 * </ul>
 */
public abstract class NPC extends Entity {
    protected String name;
    protected String[] dialogues;
    protected int currentDialogueIndex = 0;

    // --- ANIMATION ---
    private Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    private float stateTime;
    protected static final int FRAME_COLS = 3;
    protected static final int FRAME_ROWS = 4;

    /**
     * Constructeur principal utilisant un chemin de fichier.
     * Recommandé pour charger proprement les textures via LibGDX.
     *
     * @param x Position X (colonne de grille).
     * @param y Position Y (ligne de grille).
     * @param name Nom affiché dans l'interface de dialogue.
     * @param dialogues Tableau des chaînes de caractères du dialogue.
     * @param texturePath Chemin interne vers le fichier image (ex: "assets/pnj.png").
     */
    public NPC(float x, float y, String name, String[] dialogues, String texturePath) {
        // 1. Chargement de la texture
        super(x, y, new Sprite(new Texture(Gdx.files.internal(texturePath))));
        this.name = name;
        this.dialogues = dialogues;

        // 2. Initialisation du découpage et des animations
        initAnimations();

        // 3. Mise à l'échelle (1 unité = 1 tuile)
        // On force la taille à 2x2 pour un rendu visuel plus grand (style "Big Head" ou haute résolution)
        if (this.sprite != null) {
            this.sprite.setSize(2f, 2f);
        }

        // 4. Collision (1x1 pour rester cohérent avec la grille logique)
        this.setCollisionBounds(1f, 1f, 0, 0);
    }

    /**
     * Constructeur alternatif utilisant un Sprite déjà chargé.
     * Utile si le sprite est généré dynamiquement ou partagé.
     *
     * @param x Position X.
     * @param y Position Y.
     * @param name Nom du PNJ.
     * @param dialogues Dialogues.
     * @param sprite Instance de Sprite (doit contenir une texture complète 3x4).
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

    /**
     * Découpe la texture du sprite en grille (3 colonnes, 4 lignes) pour créer les animations de direction.
     * Configure la frame par défaut (regard vers le bas).
     */
    private void initAnimations() {
        Texture texture = this.getSprite().getTexture();
        // Découpage de la texture en grille 3x4
        TextureRegion[][] tmp = TextureRegion.split(texture,
            texture.getWidth() / FRAME_COLS,
            texture.getHeight() / FRAME_ROWS);

        // Initialisation des animations (0.2f = vitesse par frame)
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
        // Pour l'instant, pas d'animation de patrouille automatique (Idle statique).
        // C'est ici qu'on ajouterait une IA de mouvement pacifique.

        super.update(delta); // Met à jour la position physique et les collisions
    }

    // --- INTERACTION ---

    public String getName() {
        return name;
    }

    /**
     * @return La ligne de dialogue actuelle.
     */
    public String getCurrentDialogue() {
        if (dialogues == null || dialogues.length == 0) return "...";
        return dialogues[currentDialogueIndex];
    }

    /**
     * Passe à la phrase suivante du dialogue.
     *
     * @return {@code true} s'il reste du dialogue à afficher, {@code false} si le dialogue est terminé (et boucle au début).
     */
    public boolean advanceDialogue() {
        currentDialogueIndex++;
        if (currentDialogueIndex >= dialogues.length) {
            currentDialogueIndex = 0;
            return false;
        }
        return true;
    }

    /**
     * Oriente le PNJ pour qu'il regarde vers une entité cible (généralement le joueur).
     * Met à jour immédiatement le sprite affiché avec la frame "Idle" de la direction calculée.
     *
     * @param target L'entité à regarder.
     */
    @Override
    public void lookAt(Entity target) {
        super.lookAt(target); // Calcule la 'currentDirection' (0, 1, 2, ou 3)

        // On force l'affichage de la frame statique (milieu) de la direction concernée
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
