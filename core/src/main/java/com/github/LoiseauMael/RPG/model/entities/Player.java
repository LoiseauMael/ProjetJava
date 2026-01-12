package com.github.LoiseauMael.RPG.model.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.LoiseauMael.RPG.items.*;

/**
 * Classe abstraite représentant le personnage principal contrôlé par le joueur.
 * <p>
 * Responsabilités principales :
 * <ul>
 * <li><b>Contrôles :</b> Interprète les entrées clavier (ZQSD / Flèches) pour le mouvement.</li>
 * <li><b>Inventaire :</b> Stocke les objets et gère l'équipement actif (Arme, Armure, Relique).</li>
 * <li><b>Animation :</b> Gère le découpage de la SpriteSheet et l'état visuel (Marche/Arrêt).</li>
 * <li><b>Mécaniques de jeu :</b> Gère l'invincibilité temporaire après une fuite.</li>
 * </ul>
 */
public abstract class Player extends Fighter implements Disposable {

    // --- ANIMATIONS ---
    private Animation<TextureRegion> walkDown, walkLeft, walkRight, walkUp;
    private float stateTime;
    private static final int FRAME_COLS = 3;
    private static final int FRAME_ROWS = 4;

    // --- INVENTAIRE & ÉQUIPEMENT ---
    /** Liste des objets possédés (consommables et équipements non portés). */
    protected Array<Item> inventory;

    protected Weapon equippedWeapon;
    protected Armor equippedArmor;
    protected Relic equippedRelic;

    // --- ÉTATS DU JOUEUR ---
    /** Si false, le joueur ne peut plus bouger (ex: pendant un dialogue). */
    private boolean inputEnabled = true;

    // --- GESTION INVINCIBILITÉ (Post-Combat) ---
    private boolean isInvincible = false;
    private float invincibilityTimer = 0f;
    /** Durée de l'invincibilité après une fuite réussie (en secondes). */
    private final float COOLDOWN_TIME = 2.0f;

    /**
     * Constructeur principal du Joueur.
     * <p>
     * Initialise la physique, charge la texture, découpe les animations et prépare l'inventaire.
     * Configure également une hitbox plus petite que le sprite pour permettre de "glisser" visuellement
     * derrière certains décors (effet de profondeur).
     *
     * @param x Position X initiale.
     * @param y Position Y initiale.
     * @param PV Points de Vie max.
     * @param PM Points de Mana max.
     * @param PA Points d'Action.
     * @param FOR Force physique de base.
     * @param DEF Défense physique de base.
     * @param FORM Force magique de base.
     * @param DEFM Défense magique de base.
     * @param VIT Vitesse.
     * @param DEP Déplacement.
     * @param texturePath Chemin vers le fichier image (SpriteSheet) dans les assets.
     */
    public Player(float x, float y, int PV, int PM, int PA, int FOR, int DEF, int FORM, int DEFM, int VIT, int DEP, String texturePath) {
        super(x, y, 1, 0, PV, PM, PA, FOR, DEF, FORM, DEFM, VIT, DEP, new Sprite(new Texture(Gdx.files.internal(texturePath))));

        // Mise à l'échelle du sprite pour qu'il corresponde à la taille des cases (1 unité = 16 pixels)
        Texture t = this.getSprite().getTexture();
        float widthInUnits = (t.getWidth() / (float)FRAME_COLS) / 16f;
        float heightInUnits = (t.getHeight() / (float)FRAME_ROWS) / 16f;
        this.getSprite().setSize(widthInUnits, heightInUnits);

        // Hitbox ajustée aux "pieds" du personnage
        setCollisionBounds(0.5f, 0.3f, 0f, 0.1f);

        initAnimations();
        this.inventory = new Array<>();
    }

    // --- MÉTHODES POUR L'INVINCIBILITÉ ---

    /**
     * Active l'invincibilité temporaire.
     * <p>
     * Utilisé principalement par le {@code BattleSystem} après une fuite.
     * Rend le joueur semi-transparent (Alpha 0.5) pendant {@code COOLDOWN_TIME} secondes
     * pour éviter de relancer un combat immédiatement.
     */
    public void startFleeInvincibility() {
        this.isInvincible = true;
        this.invincibilityTimer = COOLDOWN_TIME;
        this.getSprite().setAlpha(0.5f);
        System.out.println("Invincibilité temporaire activée !");
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    /**
     * Bloque ou débloque les contrôles du joueur.
     * <p>
     * À appeler lors de l'ouverture d'un menu, d'un dialogue ou d'une cinématique.
     * Si désactivé, la vélocité est forcée à 0.
     * @param enabled {@code true} pour autoriser les mouvements.
     */
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
        if (!enabled) {
            this.velocityX = 0;
            this.velocityY = 0;
        }
    }

    /**
     * Gère la lecture des entrées clavier (Polling) à chaque frame.
     * <p>
     * Supporte les touches directionnelles et ZQSD (WASD).
     * Met à jour la vélocité et la direction du regard ({@code currentDirection}).
     * <p>
     * La vitesse de déplacement est fixée à 4.0 unités/seconde.
     */
    public void handleInput() {
        if (!inputEnabled) {
            this.velocityX = 0;
            this.velocityY = 0;
            return;
        }

        this.velocityX = 0;
        this.velocityY = 0;
        float speed = 4.0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            this.velocityX = -speed;
            setDirection(1); // Gauche
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            this.velocityX = speed;
            setDirection(2); // Droite
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            this.velocityY = speed;
            setDirection(3); // Haut
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            this.velocityY = -speed;
            setDirection(0); // Bas
        }
    }

    // --- GESTION OBJETS ---

    /**
     * Ajoute un objet à l'inventaire.
     * Si l'objet (même nom) existe déjà, ils sont empilés (quantité +1).
     * @param item L'objet à ramasser ou acheter.
     */
    public void addItem(Item item) {
        for (Item i : inventory) {
            if (i.getName().equals(item.getName())) {
                i.addCount(1);
                return;
            }
        }
        inventory.add(item);
    }

    /**
     * Tente de consommer un objet.
     * Si l'effet s'applique (ex: Potion de soin alors que PV < Max), la quantité est réduite.
     * Supprime l'objet de l'inventaire si la quantité atteint 0.
     * @param item L'objet à utiliser.
     */
    public void consumeItem(Item item) {
        if (item.use(this)) {
            item.removeCount(1);
            if (item.getCount() <= 0) {
                inventory.removeValue(item, true);
            }
        }
    }

    /**
     * Équipe une nouvelle pièce d'équipement.
     * <p>
     * Gestion intelligente :
     * <ul>
     * <li>Détecte le type (Arme, Armure, Relique).</li>
     * <li>Déséquipe automatiquement l'ancien objet du même slot.</li>
     * <li>Applique immédiatement les bonus de stats via {@link #applyEquipmentStats(Equipment, boolean)}.</li>
     * </ul>
     * @param item L'équipement à enfiler.
     */
    public void equip(Equipment item) {
        if (item instanceof Weapon) {
            if (equippedWeapon != null) unequip(equippedWeapon);
            equippedWeapon = (Weapon) item;
        } else if (item instanceof Armor) {
            if (equippedArmor != null) unequip(equippedArmor);
            equippedArmor = (Armor) item;
        } else if (item instanceof Relic) {
            if (equippedRelic != null) unequip(equippedRelic);
            equippedRelic = (Relic) item;
        }
        applyEquipmentStats(item, true);
        System.out.println("Équipé : " + item.getName());
    }

    /**
     * Retire une pièce d'équipement et annule ses bonus de stats.
     * @param item L'objet à retirer.
     */
    public void unequip(Equipment item) {
        applyEquipmentStats(item, false);
        if (item == equippedWeapon) equippedWeapon = null;
        else if (item == equippedArmor) equippedArmor = null;
        else if (item == equippedRelic) equippedRelic = null;
    }

    /**
     * Applique ou retire les bonus de stats d'un équipement sur le joueur.
     * Modifie directement les attributs (FOR, DEF, etc.).
     * @param item L'objet concerné.
     * @param isEquipping true pour ajouter les stats, false pour les retirer.
     */
    private void applyEquipmentStats(Equipment item, boolean isEquipping) {
        int factor = isEquipping ? 1 : -1;
        if (item instanceof Weapon) {
            Weapon w = (Weapon) item;
            this.FOR += w.bonusFOR * factor;
            this.FORM += w.bonusFORM * factor;
        } else if (item instanceof Armor) {
            Armor a = (Armor) item;
            this.DEF += a.bonusDEF * factor;
            this.DEFM += a.bonusDEFM * factor;
        }
    }

    // Getters pour l'interface utilisateur (Inventaire)
    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public Armor getEquippedArmor() { return equippedArmor; }
    public Relic getEquippedRelic() { return equippedRelic; }
    public Array<Item> getInventory() { return inventory; }

    @Override
    public void update(float delta) {
        // 1. Mise à jour du Timer d'invincibilité
        if (isInvincible) {
            invincibilityTimer -= delta;
            if (invincibilityTimer <= 0) {
                isInvincible = false;
                invincibilityTimer = 0;
                this.getSprite().setAlpha(1.0f); // Retour à l'opacité normale
            }
        }

        handleInput();      // Gestion clavier
        super.update(delta);// Physique et collisions (Entity)
        updateAnimation(delta); // Visuel
    }

    /** * Sélectionne la frame d'animation appropriée.
     * Si le joueur bouge, l'animation boucle. Sinon, on affiche la frame "debout" (index 1).
     */
    protected void updateAnimation(float delta) {
        stateTime += delta;
        boolean isMoving = (velocityX != 0 || velocityY != 0);
        TextureRegion currentFrame;

        switch (getDirection()) {
            case 1: currentFrame = isMoving ? walkLeft.getKeyFrame(stateTime, true) : walkLeft.getKeyFrames()[1]; break;
            case 2: currentFrame = isMoving ? walkRight.getKeyFrame(stateTime, true) : walkRight.getKeyFrames()[1]; break;
            case 3: currentFrame = isMoving ? walkUp.getKeyFrame(stateTime, true) : walkUp.getKeyFrames()[1]; break;
            default: currentFrame = isMoving ? walkDown.getKeyFrame(stateTime, true) : walkDown.getKeyFrames()[1]; break;
        }
        this.getSprite().setRegion(currentFrame);
    }

    @Override
    protected void updateSpriteRegion() {
        // Force la mise à jour visuelle immédiate (utile lors d'un changement de direction à l'arrêt)
        updateAnimation(0);
    }

    /** Découpe la SpriteSheet en 4 directions de 3 frames chacune. */
    private void initAnimations() {
        Texture texture = this.getSprite().getTexture();
        TextureRegion[][] tmp = TextureRegion.split(texture, texture.getWidth() / FRAME_COLS, texture.getHeight() / FRAME_ROWS);
        walkDown = new Animation<>(0.2f, tmp[0]);
        walkLeft = new Animation<>(0.2f, tmp[1]);
        walkRight = new Animation<>(0.2f, tmp[2]);
        walkUp = new Animation<>(0.2f, tmp[3]);
    }

    @Override
    public void dispose() {
        if (getSprite() != null) getSprite().getTexture().dispose();
    }
}
