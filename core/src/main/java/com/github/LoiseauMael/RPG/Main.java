package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.LoiseauMael.RPG.battle.BattleSystem;
import com.github.LoiseauMael.RPG.items.HealthPotion;
import com.github.LoiseauMael.RPG.items.Item;
import com.github.LoiseauMael.RPG.items.ManaPotion;
import com.github.LoiseauMael.RPG.model.entities.Enemy;
import com.github.LoiseauMael.RPG.model.entities.Entity;
import com.github.LoiseauMael.RPG.model.entities.Player;
import com.github.LoiseauMael.RPG.model.entities.SwordMan;
import com.github.LoiseauMael.RPG.model.entities.Wizard;
import com.github.LoiseauMael.RPG.npcs.NPC;
import com.github.LoiseauMael.RPG.physics.CollisionSystem;
import com.github.LoiseauMael.RPG.save.SaveManager;
import com.github.LoiseauMael.RPG.skills.SkillManager;
import com.github.LoiseauMael.RPG.states.*;
import com.github.LoiseauMael.RPG.utils.MapLoader;
import com.github.LoiseauMael.RPG.utils.ShopLoader;

/**
 * Classe principale du jeu (Point d'entrée).
 * <p>
 * Elle hérite de {@link ApplicationAdapter} et agit comme le "Chef d'orchestre" du jeu.
 * Ses responsabilités incluent :
 * <ul>
 * <li>La gestion du cycle de vie LibGDX (create, render, resize, dispose).</li>
 * <li>La centralisation des ressources graphiques lourdes (SpriteBatch, TiledMapRenderer).</li>
 * <li>L'implémentation de la machine à états finis (Exploration, Combat, Menus...).</li>
 * <li>Le stockage des données globales (Joueur, Entités, Inventaire Marchand).</li>
 * </ul>
 */
public class Main extends ApplicationAdapter {

    // --- MOTEUR GRAPHIQUE ---
    /** Batch utilisé pour dessiner toutes les textures 2D. */
    public SpriteBatch batch;
    /** Outil pour dessiner des formes géométriques (debug, grilles). */
    public ShapeRenderer shapeRenderer;
    /** La carte Tiled (TMX) actuellement chargée en mémoire. */
    public TiledMap map;
    /** Le renderer officiel de LibGDX pour afficher la carte Tiled. */
    public OrthogonalTiledMapRenderer tiledMapRenderer;
    /** Caméra orthographique centrée sur le joueur. */
    public OrthographicCamera camera;

    // --- DONNÉES DE JEU ---
    /** Nom du fichier de la carte actuelle (ex: "map.tmx"). Utilisé pour la sauvegarde. */
    public String currentMapName = "map.tmx";
    /** Système gérant la détection de collisions avec le décor. */
    public CollisionSystem collisionSystem;
    /** L'instance du joueur (Héros). */
    public Player player;
    /** Liste de tous les ennemis présents sur la carte actuelle. */
    public Array<Enemy> enemies;
    /** Liste générique de toutes les entités (NPC + Ennemis + Décors interactifs). */
    public Array<Entity> entities;
    /** Liste des personnages non-joueurs (pour les interactions). */
    public Array<NPC> npcs;
    /** Liste des IDs des ennemis vaincus pour éviter qu'ils ne réapparaissent au rechargement de la carte. */
    public Array<Integer> deadEnemyIds = new Array<>();

    // --- GESTION DES ÉTATS (STATE MACHINE) ---
    /** L'état actuellement actif et mis à jour à chaque frame. */
    private IGameState currentState;

    // Instances des différents états du jeu (Singleton-like)
    public ExplorationState explorationState;
    public CombatState combatState;
    public DialogueState dialogueState;
    public MenuState menuState;
    public ShopState shopState;
    public StartMenuState startMenuState;
    public ClassSelectionState classSelectionState;

    /** Échelle de rendu : 1 unité = 16 pixels (taille d'une tuile standard). */
    private static final float UNIT_SCALE = 1/16f;

    // --- UI (INTERFACE UTILISATEUR) ---
    /** Skin LibGDX contenant les styles des widgets (boutons, labels). */
    public Skin skin;
    /** Police d'écriture par défaut. */
    public BitmapFont font;

    /** Système logique gérant le tour par tour pendant les combats. */
    public BattleSystem battleSystem;

    // Stages Scene2D pour chaque écran d'interface
    public Stage combatStage;
    public Stage menuStage;
    public Table menuTable;
    public Stage uiStage;
    public Table dialogTable;
    public Label dialogNameLabel;
    public Label dialogTextLabel;
    public Stage shopStage;
    public Table shopTable;
    public Stage startStage;
    public Stage classStage;
    public Table classTable;

    /**
     * Classe interne simple représentant une entrée dans la boutique.
     * Associe un objet à un prix.
     */
    public static class ShopEntry {
        public Item item;
        public int price;
        public ShopEntry(Item item, int price) { this.item = item; this.price = price; }
    }
    /** Inventaire global du marchand (chargé depuis JSON). */
    public Array<ShopEntry> merchantInventory;

    /**
     * Initialisation du jeu.
     * <p>
     * Appelée une seule fois au démarrage. Elle charge les compétences, le magasin,
     * initialise les systèmes graphiques, crée les instances d'états et lance le Menu Principal.
     */
    @Override
    public void create() {
        SkillManager.loadSkills();
        initShop();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        entities = new Array<>();
        npcs = new Array<>();
        enemies = new Array<>();
        deadEnemyIds = new Array<>();

        initSkin();

        // Initialisation de la caméra
        camera = new OrthographicCamera();
        // Configure la caméra pour afficher 16 unités de large (16 tuiles)
        camera.setToOrtho(false, 16f, 16f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
        camera.update();

        // Chargement initial pour éviter les NullPointerException
        loadMapInitial(currentMapName);

        // Initialisation des états
        explorationState = new ExplorationState(this);
        combatState = new CombatState(this);
        dialogueState = new DialogueState(this);
        menuState = new MenuState(this);
        shopState = new ShopState(this);
        startMenuState = new StartMenuState(this);
        classSelectionState = new ClassSelectionState(this);

        // Construction des interfaces
        initStartMenuUI();
        initClassSelectionUI();
        initGameUIs();

        rebuildStartMenu();
        changeState(startMenuState);
    }

    /**
     * Charge les données du magasin via {@link ShopLoader}.
     * Si le chargement échoue, ajoute une potion par défaut pour éviter un shop vide.
     */
    public void initShop() {
        ShopLoader.loadShop(this);
        if (this.merchantInventory == null || this.merchantInventory.size == 0) {
            if (this.merchantInventory == null) this.merchantInventory = new Array<>();
            merchantInventory.add(new ShopEntry(new HealthPotion("Potion Secours", "Default", 10), 1));
        }
    }

    /**
     * Méthode interne pour charger la carte au démarrage de l'application.
     * Contrairement à {@link #loadMap(String)}, elle ne tente pas de positionner le joueur
     * car celui-ci n'est pas encore créé.
     *
     * @param mapName Nom du fichier carte.
     */
    private void loadMapInitial(String mapName) {
        try {
            if (map != null) map.dispose();
            map = new TmxMapLoader().load("tiled/map/" + mapName);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);
            this.collisionSystem = new CollisionSystem(map);
            Entity.setCollisionSystem(this.collisionSystem);
        } catch (Exception e) {
            Gdx.app.error("Main", "Erreur init map: " + e.getMessage());
        }
    }

    // --- CHANGEMENT DE CARTE SIMPLE (Spawn par défaut) ---
    public void loadMap(String mapName) {
        loadMap(mapName, -1, -1);
    }

    /**
     * Charge une nouvelle carte Tiled (.tmx) et gère la transition.
     * <p>
     * Étapes :
     * 1. Dispose l'ancienne carte.
     * 2. Nettoie les listes d'entités.
     * 3. Charge la nouvelle carte et met à jour le CollisionSystem.
     * 4. Utilise {@link MapLoader} pour spawner les ennemis et PNJs.
     * 5. Repositionne le joueur (soit au spawn par défaut, soit à des coordonnées précises).
     *
     * @param mapName Le nom du fichier de la map (ex: "foret.tmx").
     * @param startX  La position X de départ du joueur (en cases). Si -1, utilise le spawn par défaut de la map.
     * @param startY  La position Y de départ du joueur (en cases). Si -1, utilise le spawn par défaut de la map.
     */
    public void loadMap(String mapName, float startX, float startY) {
        if (map != null) {
            map.dispose();
        }

        // Nettoyage complet
        if (entities != null) entities.clear();
        if (npcs != null) npcs.clear();
        if (enemies != null) enemies.clear();

        this.currentMapName = mapName;
        System.out.println("Chargement de la carte : " + mapName);

        try {
            // Gestion robuste des chemins (absolus vs relatifs)
            String fullPath = mapName.contains("/") ? mapName : "tiled/map/" + mapName;
            map = new TmxMapLoader().load(fullPath);
        } catch(Exception e) {
            Gdx.app.error("Main", "Impossible de charger la map: " + mapName + " -> " + e.getMessage());
            // Fallback pour éviter le crash
            try { map = new TmxMapLoader().load("map.tmx"); } catch (Exception ignored) {}
            return;
        }

        // Mise à jour du Renderer
        if (tiledMapRenderer != null) {
            tiledMapRenderer.setMap(map);
        } else {
            tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);
        }

        // Mise à jour des collisions
        this.collisionSystem = new CollisionSystem(map);
        Entity.setCollisionSystem(this.collisionSystem);

        // Chargement des entités via le MapLoader
        this.entities = MapLoader.loadEntities(map, this);

        // Répartition dans les listes spécifiques
        for(Entity e : entities) {
            if(e instanceof NPC) npcs.add((NPC)e);
            if(e instanceof Enemy) enemies.add((Enemy)e);
        }
        if(collisionSystem != null) collisionSystem.setNpcs(npcs);

        // Repositionnement du joueur
        if (player != null) {
            if (startX != -1 && startY != -1) {
                // Position spécifique (transition entre zones)
                player.set_position(startX + 0.5f, startY);
            } else {
                // Position par défaut (Spawn Point défini dans Tiled)
                Vector2 spawn = MapLoader.getPlayerSpawn(map);
                player.set_position(spawn.x, spawn.y);
            }
            player.snapToGrid();

            // Recalage de la caméra
            camera.position.set(player.get_positionX(), player.get_positionY(), 0);
            camera.update();
        }

        changeState(explorationState);
    }

    /**
     * Change l'état actuel du jeu (ex: passer de l'Exploration au Combat).
     * Gère automatiquement l'appel à {@link IGameState#exit()} de l'ancien état
     * et {@link IGameState#enter()} du nouveau.
     *
     * @param newState La nouvelle instance d'état à activer (ex: this.combatState).
     */
    public void changeState(IGameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        if (currentState != null) currentState.enter();
    }

    /**
     * Boucle principale de rendu du jeu. Appelé à chaque frame (60 fois par seconde).
     * <p>
     * Délègue la logique (update) et le dessin (draw) à l'état courant (currentState).
     * Gère également le rendu des Stages UI Scene2D par-dessus le jeu.
     */
    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1/30f);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        if (currentState != null) {
            currentState.handleInput();
            currentState.update(delta);
            currentState.draw(batch);
        }

        // Rendu de l'UI correspondant à l'état actif
        if (currentState == combatState) {
            combatStage.act(delta); combatStage.draw();
        } else if (currentState == menuState) {
            menuStage.act(delta); menuStage.draw();
        } else if (currentState == shopState) {
            shopStage.act(delta); shopStage.draw();
        } else if (currentState == startMenuState) {
            startStage.act(delta); startStage.draw();
        } else if (currentState == classSelectionState) {
            classStage.act(delta); classStage.draw();
        } else if (currentState == dialogueState) {
            uiStage.act(delta); uiStage.draw();
        }
    }

    /**
     * Appelé lors du redimensionnement de la fenêtre.
     * Met à jour le viewport de la caméra et de tous les stages UI pour éviter les déformations.
     */
    @Override
    public void resize(int width, int height) {
        Stage[] stages = {combatStage, startStage, classStage, shopStage, menuStage, uiStage};
        for (Stage s : stages) if (s != null) s.getViewport().update(width, height, true);
        camera.setToOrtho(false, 16f, 16f * height / width);
        camera.update();
    }

    /**
     * Lance une nouvelle partie en créant le personnage joueur et en réinitialisant le monde.
     *
     * @param isWizard Si {@code true}, instancie un Mage (Wizard). Sinon, instancie un Guerrier (SwordMan).
     */
    public void launchGame(boolean isWizard) {
        if (isWizard) player = Wizard.create(0, 0);
        else player = SwordMan.create(0, 0);

        // Équipement de départ
        player.addMoney(500);
        player.addItem(new HealthPotion("Potion de Vie", "Rend 20 PV", 20));
        player.addItem(new ManaPotion("Ether", "Rend 10 PM", 10));

        deadEnemyIds.clear();
        currentMapName = "map.tmx";
        loadMap(currentMapName); // Charge la map et place le joueur au spawn
    }

    // --- SETUP UI ET SKIN (Méthodes privées d'initialisation) ---

    /** Initialise le style par défaut (Skin) pour les widgets UI (couleurs, polices). */
    private void initSkin() {
        skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE); pixmap.fill();
        Texture whiteTexture = new Texture(pixmap);
        TextureRegion whiteRegion = new TextureRegion(whiteTexture);
        skin.add("white", whiteRegion);
        skin.add("default", new BitmapFont());
        TextureRegionDrawable whiteDrawable = new TextureRegionDrawable(whiteRegion);
        Drawable darkBackground = whiteDrawable.tint(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        skin.add("default-rect", darkBackground, Drawable.class);
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = whiteDrawable.tint(Color.DARK_GRAY); tbs.down = whiteDrawable.tint(Color.BLACK); tbs.over = whiteDrawable.tint(Color.GRAY);
        tbs.font = skin.getFont("default"); skin.add("default", tbs);
        Label.LabelStyle ls = new Label.LabelStyle(); ls.font = skin.getFont("default"); skin.add("default", ls);
        skin.add("default", new ScrollPane.ScrollPaneStyle());
        ProgressBar.ProgressBarStyle pbs = new ProgressBar.ProgressBarStyle();
        pbs.background = whiteDrawable.tint(Color.DARK_GRAY); pbs.knobBefore = whiteDrawable.tint(Color.CYAN);
        skin.add("default-horizontal", pbs);
    }

    /** Appelle l'initialisation de tous les sous-systèmes UI. */
    private void initGameUIs() {
        initCombatUI(); initMenuUI(); initDialogUI(); initShopUI();
    }

    /** Initialise l'UI de combat. */
    private void initCombatUI() { combatStage = new Stage(new ScreenViewport()); combatStage.addActor(new Table()); }

    /** Initialise l'UI du menu pause. */
    private void initMenuUI() { menuStage = new Stage(new ScreenViewport()); menuTable = new Table(); menuTable.setFillParent(true); menuTable.center(); menuTable.setBackground(skin.getDrawable("default-rect")); menuStage.addActor(menuTable); }

    /** Initialise l'UI de dialogue. */
    private void initDialogUI() { uiStage = new Stage(new ScreenViewport()); dialogTable = new Table(); dialogTable.setFillParent(true); dialogTable.bottom(); dialogTable.setVisible(false); dialogTable.setBackground(skin.getDrawable("default-rect")); dialogNameLabel = new Label("", skin); dialogTextLabel = new Label("", skin); dialogTable.add(dialogNameLabel).left().pad(20).row(); dialogTable.add(dialogTextLabel).width(Gdx.graphics.getWidth() - 100).left().pad(20); uiStage.addActor(dialogTable); }

    /** Initialise l'UI du magasin. */
    private void initShopUI() { shopStage = new Stage(new ScreenViewport()); shopTable = new Table(); shopTable.setFillParent(true); shopTable.center(); shopTable.setBackground(skin.getDrawable("default-rect")); shopStage.addActor(shopTable); }

    /** Initialise le stage du menu principal. */
    private void initStartMenuUI() { startStage = new Stage(new ScreenViewport()); }

    /** Initialise l'écran de sélection de classe. */
    private void initClassSelectionUI() { classStage = new Stage(new ScreenViewport()); classTable = new Table(); classTable.setFillParent(true); classTable.center(); TextButton btnWarrior = new TextButton("Guerrier", skin); btnWarrior.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { launchGame(false); } }); TextButton btnWizard = new TextButton("Mage", skin); btnWizard.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { launchGame(true); } }); classTable.add(btnWarrior).pad(20); classTable.add(btnWizard).pad(20); classStage.addActor(classTable); }

    /**
     * Reconstruit dynamiquement le menu principal.
     * Vérifie si une sauvegarde existe via {@link SaveManager} pour afficher "Continuer" ou "Nouvelle Partie".
     */
    public void rebuildStartMenu() {
        startStage.clear();
        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(10).width(250).height(50);
        if (SaveManager.saveExists()) {
            TextButton btnContinue = new TextButton("Continuer", skin);
            btnContinue.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    SaveManager.loadGame(Main.this);
                    loadMap(currentMapName); // Recharge la map sauvegardée
                }
            });
            table.add(btnContinue).row();
            TextButton btnDelete = new TextButton("Supprimer Sauvegarde", skin);
            btnDelete.setColor(Color.FIREBRICK);
            btnDelete.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    SaveManager.deleteSave(); rebuildStartMenu();
                }
            });
            table.add(btnDelete).row();
        } else {
            TextButton btnNew = new TextButton("Nouvelle Partie", skin);
            btnNew.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { goToClassSelection(); } });
            table.add(btnNew).row();
        }
        TextButton btnExit = new TextButton("Quitter", skin);
        btnExit.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { Gdx.app.exit(); } });
        table.add(btnExit).row();
        startStage.addActor(table);
    }

    /** Transition vers l'écran de sélection de classe. */
    public void goToClassSelection() { changeState(classSelectionState); }

    /**
     * Libération de la mémoire à la fermeture de l'application.
     * Détruit les Batchs, Textures, Maps, Stages et la Police.
     */
    @Override
    public void dispose() {
        batch.dispose(); shapeRenderer.dispose();
        if (map != null) map.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
        if (player != null) player.dispose();
        Stage[] stages = {combatStage, menuStage, startStage, classStage, shopStage, uiStage};
        for (Stage s : stages) if (s != null) s.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
    }
}
