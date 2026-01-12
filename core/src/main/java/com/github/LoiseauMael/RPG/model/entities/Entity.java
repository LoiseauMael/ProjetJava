package com.github.LoiseauMael.RPG.model.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.github.LoiseauMael.RPG.physics.CollisionSystem;

/**
 * Classe abstraite de base représentant n'importe quel objet vivant ou interactif dans le monde du jeu.
 * <p>
 * Cette classe sert de fondation pour le système ECS (Entity-Component-System) simplifié.
 * Elle gère :
 * <ul>
 * <li>La <b>Position</b> : Synchronisation entre les coordonnées du monde (float) et de la grille (int).</li>
 * <li>Le <b>Rendu</b> : Affichage via un {@link Sprite} LibGDX.</li>
 * <li>La <b>Physique</b> : Déplacement, vélocité et détection de collision via {@link CollisionSystem}.</li>
 * </ul>
 */
public abstract class Entity {
    // --- POSITION & PHYSIQUE ---
    /** Position X absolue (Unités monde). Représente le CENTRE horizontal de l'entité. */
    protected float positionX;
    /** Position Y absolue (Unités monde). Représente le BAS de l'entité (les pieds). */
    protected float positionY;

    protected float velocityX;
    protected float velocityY;
    protected Sprite sprite;

    /** Direction actuelle du regard : 0=Bas, 1=Gauche, 2=Droite, 3=Haut. */
    protected int currentDirection = 0;

    protected int id;
    private static int idCounter = 0;

    // --- COLLISIONS ---
    private Rectangle tempRect = new Rectangle();
    /** Référence statique au système de collision partagé par toutes les entités. */
    protected static CollisionSystem collisionSystem;

    protected float collisionWidth = 1f;
    protected float collisionHeight = 1f;
    protected float collisionOffsetX = 0;
    protected float collisionOffsetY = 0;

    /**
     * Constructeur de base d'une entité.
     * <p>
     * Place l'entité au <b>CENTRE</b> horizontal de la case spécifiée (x + 0.5f) pour un alignement visuel correct.
     *
     * @param x Coordonnée X de la case de départ (colonne).
     * @param y Coordonnée Y de la case de départ (ligne).
     * @param sprite Le sprite graphique associé (peut être null pour des entités invisibles).
     */
    public Entity(float x, float y, Sprite sprite) {
        // IMPORTANT : On place le personnage au CENTRE de la case (x + 0.5f)
        // Cela aligne le modèle logique avec le visuel pour éviter le décalage
        this.positionX = x + 0.5f;
        this.positionY = y;

        this.sprite = sprite;
        this.id = idCounter++;

        if (this.sprite != null) {
            this.collisionWidth = sprite.getWidth();
            this.collisionHeight = sprite.getHeight();
            this.collisionOffsetX = 0;
            this.collisionOffsetY = 0;
        } else {
            this.collisionWidth = 1f;
            this.collisionHeight = 1f;
        }
        tempRect.setSize(collisionWidth, collisionHeight);
    }

    // --- LOGIQUE DE GRILLE & CENTRE ---

    /** * Récupère la colonne de la grille correspondant à la position actuelle.
     * @return L'index X de la case (entier).
     */
    public int getTileX() {
        return (int) positionX;
    }

    /** * Récupère la ligne de la grille correspondant à la position actuelle.
     * @return L'index Y de la case (entier).
     */
    public int getTileY() {
        return (int) positionY;
    }

    /** * @return La position X absolue du CENTRE de l'entité (en unités monde).
     */
    public float getCenterX() {
        return positionX;
    }

    /** * @return La position Y absolue du CENTRE vertical de l'entité (en unités monde), calculée via la hauteur de collision.
     */
    public float getCenterY() {
        return positionY + (collisionHeight / 2f);
    }

    /**
     * Calcule la distance de Manhattan (déplacement par cases sans diagonales) vers une cible.
     * Utilise les coordonnées de grille {@link #getTileX()} et {@link #getTileY()}.
     * * @param target L'entité cible.
     * @return La distance en nombre de cases (X + Y). Retourne 999 si la cible est nulle.
     */
    public int getGridDistance(Entity target) {
        if (target == null) return 999;
        return Math.abs(this.getTileX() - target.getTileX()) + Math.abs(this.getTileY() - target.getTileY());
    }

    /**
     * Recentre l'entité parfaitement au milieu de sa case actuelle.
     * <p>
     * Utile après un changement de map, une téléportation, ou pour corriger des micro-décalages
     * après des collisions physiques.
     */
    public void snapToGrid() {
        // Aligne le centre X au milieu de la case
        this.positionX = getTileX() + 0.5f;
        this.positionY = getTileY();
        if (sprite != null) {
            sprite.setPosition(positionX - (sprite.getWidth() / 2f), positionY);
        }
    }

    // --- ORIENTATION ---

    /**
     * Oriente l'entité pour regarder vers une autre entité.
     * @param target L'entité à regarder.
     */
    public void lookAt(Entity target) {
        if (target == null) return;
        lookAt(target.getCenterX(), target.getCenterY());
    }

    /**
     * Calcule la direction (0, 1, 2, 3) pour regarder vers un point précis (x, y).
     * <p>
     * Met à jour {@code currentDirection} en comparant les deltas X et Y.
     * Déclenche {@link #updateSpriteRegion()} pour rafraîchir le visuel immédiatement.
     * * @param targetX Coordonnée X cible.
     * @param targetY Coordonnée Y cible.
     */
    public void lookAt(float targetX, float targetY) {
        float dx = targetX - this.positionX;
        float dy = targetY - this.positionY;

        if (Math.abs(dx) > Math.abs(dy)) {
            this.currentDirection = (dx > 0) ? 2 : 1;
        } else {
            this.currentDirection = (dy > 0) ? 3 : 0;
        }

        // Met à jour le sprite immédiatement pour la réactivité
        updateSpriteRegion();
    }

    /**
     * Méthode hook destinée à être surchargée par les classes filles (Player, Enemy).
     * Permet de mettre à jour la texture (TextureRegion) du sprite selon la direction actuelle
     * sans attendre le cycle de update.
     */
    protected void updateSpriteRegion() {
        // Vide par défaut, surchargée dans Enemy/Player
    }

    // --- UPDATE & DRAW ---

    /**
     * Boucle principale de mise à jour logique.
     * <p>
     * Calcule la prochaine position en fonction de la vélocité et du temps écoulé (delta).
     * Vérifie les collisions avec le décor via le {@link CollisionSystem} avant d'appliquer le mouvement.
     *
     * @param delta Temps écoulé depuis la dernière frame (en secondes).
     */
    public void update(float delta) {
        float nextX = positionX + velocityX * delta;
        float nextY = positionY + velocityY * delta;

        // Hitbox centrée sur le futur X
        tempRect.set(nextX - (collisionWidth / 2f) + collisionOffsetX,
            nextY + collisionOffsetY,
            collisionWidth,
            collisionHeight);

        if (collisionSystem == null || !collisionSystem.isColliding(tempRect)) {
            positionX = nextX;
            positionY = nextY;
        }
    }

    /**
     * Affiche l'entité à l'écran.
     * La position de dessin est calculée pour centrer le sprite sur {@code positionX}.
     * * @param batch Le {@link SpriteBatch} actif utilisé pour le rendu.
     */
    public void draw(SpriteBatch batch) {
        if (sprite != null) {
            // DESSIN : On dessine le sprite centré sur positionX
            sprite.setPosition(positionX - (sprite.getWidth() / 2f), positionY);
            sprite.draw(batch);
        }
    }

    // --- GETTERS / SETTERS ---

    public int getId() { return id; }

    /**
     * Définit le système de collision global. Doit être appelé lors de l'initialisation du niveau.
     * @param cs L'instance du CollisionSystem.
     */
    public static void setCollisionSystem(CollisionSystem cs) { collisionSystem = cs; }

    /**
     * Définit manuellement la taille et le décalage de la boîte de collision (Hitbox).
     * @param w Largeur de la hitbox.
     * @param h Hauteur de la hitbox.
     * @param ox Décalage X (offset).
     * @param oy Décalage Y (offset).
     */
    public void setCollisionBounds(float w, float h, float ox, float oy) {
        this.collisionWidth = w; this.collisionHeight = h;
        this.collisionOffsetX = ox; this.collisionOffsetY = oy;
    }

    // Accesseurs legacy ou utilitaires
    public float get_positionX() { return positionX; }
    public float get_positionY() { return positionY; }

    /**
     * Téléporte l'entité sur une case spécifique de la grille.
     * @param tx Index X de la tuile.
     * @param ty Index Y de la tuile.
     */
    public void setGridPosition(int tx, int ty) {
        this.positionX = tx + 0.5f;
        this.positionY = ty;
    }

    public void set_position(float x, float y) {
        this.positionX = x;
        this.positionY = y;
    }

    public Sprite getSprite() { return sprite; }
    public void setSprite(Sprite s) {
        this.sprite = s;
        if(s != null) {
            this.collisionWidth = s.getWidth();
            this.collisionHeight = s.getHeight();
        }
    }

    public float get_velocityX() { return velocityX; }
    public void set_velocityX(float v) { this.velocityX = v; }
    public float get_velocityY() { return velocityY; }
    public void set_velocityY(float v) { this.velocityY = v; }

    public int getDirection() { return currentDirection; }
    public void setDirection(int dir) { this.currentDirection = dir; }

    /**
     * @return Un rectangle représentant la hitbox actuelle de l'entité dans le monde.
     */
    public Rectangle getBoundingBox() {
        return new Rectangle(positionX - (collisionWidth / 2f) + collisionOffsetX,
            positionY + collisionOffsetY,
            collisionWidth,
            collisionHeight);
    }
}
