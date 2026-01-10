package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    // --- POSITION & PHYSIQUE ---
    // positionX = CENTRE horizontal de l'entité
    // positionY = BAS de l'entité (pieds)
    protected float positionX;
    protected float positionY;

    protected float velocityX;
    protected float velocityY;
    protected Sprite sprite;
    protected int currentDirection = 0; // 0=Bas, 1=Gauche, 2=Droite, 3=Haut

    protected int id;
    private static int idCounter = 0;

    // --- COLLISIONS ---
    private Rectangle tempRect = new Rectangle();
    protected static CollisionSystem collisionSystem;
    protected float collisionWidth = 1f;
    protected float collisionHeight = 1f;
    protected float collisionOffsetX = 0;
    protected float collisionOffsetY = 0;

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

    /** Renvoie le X de la case. Cast (int) car positionX est le centre (ex: 5.5 -> 5). */
    public int getTileX() {
        return (int) positionX;
    }

    /** Renvoie le Y de la case. */
    public int getTileY() {
        return (int) positionY;
    }

    // --- AJOUTS MANQUANTS (CORRECTION ERREUR) ---

    /** Retourne la position X du CENTRE de l'entité */
    public float getCenterX() {
        return positionX;
    }

    /** Retourne la position Y du CENTRE de l'entité */
    public float getCenterY() {
        return positionY + (collisionHeight / 2f);
    }
    // --------------------------------------------

    public int getGridDistance(Entity target) {
        if (target == null) return 999;
        return Math.abs(this.getTileX() - target.getTileX()) + Math.abs(this.getTileY() - target.getTileY());
    }

    public void snapToGrid() {
        // Aligne le centre X au milieu de la case
        this.positionX = getTileX() + 0.5f;
        this.positionY = getTileY();
        if (sprite != null) {
            sprite.setPosition(positionX - (sprite.getWidth() / 2f), positionY);
        }
    }

    // --- ORIENTATION ---

    public void lookAt(Entity target) {
        if (target == null) return;
        lookAt(target.getCenterX(), target.getCenterY());
    }

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
     * Méthode destinée à être surchargée par les classes filles (Player, Enemy)
     * pour mettre à jour la texture du sprite selon la direction actuelle.
     */
    protected void updateSpriteRegion() {
        // Vide par défaut, surchargée dans Enemy/Player
    }

    // --- UPDATE & DRAW ---

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

    public void draw(SpriteBatch batch) {
        if (sprite != null) {
            // DESSIN : On dessine le sprite centré sur positionX
            sprite.setPosition(positionX - (sprite.getWidth() / 2f), positionY);
            sprite.draw(batch);
        }
    }

    // --- GETTERS / SETTERS ---

    public int getId() { return id; }
    public static void setCollisionSystem(CollisionSystem cs) { collisionSystem = cs; }

    public void setCollisionBounds(float w, float h, float ox, float oy) {
        this.collisionWidth = w; this.collisionHeight = h;
        this.collisionOffsetX = ox; this.collisionOffsetY = oy;
    }

    // Accesseurs legacy ou utilitaires
    public float get_positionX() { return positionX; }
    public float get_positionY() { return positionY; }

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

    public Rectangle getBoundingBox() {
        return new Rectangle(positionX - (collisionWidth / 2f) + collisionOffsetX,
            positionY + collisionOffsetY,
            collisionWidth,
            collisionHeight);
    }
}
