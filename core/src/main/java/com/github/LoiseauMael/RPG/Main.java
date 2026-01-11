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
import com.github.LoiseauMael.RPG.npcs.NPC;
import com.github.LoiseauMael.RPG.items.*;
import com.github.LoiseauMael.RPG.save.SaveManager;
import com.github.LoiseauMael.RPG.skills.SkillManager;
import com.github.LoiseauMael.RPG.utils.MapLoader;
import com.github.LoiseauMael.RPG.utils.ShopLoader;
import com.github.LoiseauMael.RPG.states.*;

public class Main extends ApplicationAdapter {

    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public TiledMap map;
    public OrthogonalTiledMapRenderer tiledMapRenderer;
    public OrthographicCamera camera;
    public String currentMapName = "map.tmx";

    public Player player;
    public Array<Enemy> enemies;
    public Array<Entity> entities;
    public Array<NPC> npcs;
    public CollisionSystem collisionSystem;

    public Array<Integer> deadEnemyIds = new Array<>();

    private IGameState currentState;
    public ExplorationState explorationState;
    public CombatState combatState;
    public DialogueState dialogueState;
    public MenuState menuState;
    public ShopState shopState;
    public StartMenuState startMenuState;
    public ClassSelectionState classSelectionState;

    private static final float UNIT_SCALE = 1/16f;

    public Skin skin;
    public BitmapFont font;

    public BattleSystem battleSystem;
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

    public static class ShopEntry {
        public Item item;
        public int price;
        public ShopEntry(Item item, int price) { this.item = item; this.price = price; }
    }
    public Array<ShopEntry> merchantInventory;

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
        camera.setToOrtho(false, 16f, 16f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
        camera.update();

        // Chargement de la map par défaut (pour éviter les null pointers au démarrage)
        loadMapInitial(currentMapName);

        // Initialisation des états
        explorationState = new ExplorationState(this);
        combatState = new CombatState(this);
        dialogueState = new DialogueState(this);
        menuState = new MenuState(this);
        shopState = new ShopState(this);
        startMenuState = new StartMenuState(this);
        classSelectionState = new ClassSelectionState(this);

        initStartMenuUI();
        initClassSelectionUI();
        initGameUIs();

        rebuildStartMenu();
        changeState(startMenuState);
    }

    public void initShop() {
        ShopLoader.loadShop(this);
        if (this.merchantInventory == null || this.merchantInventory.size == 0) {
            if (this.merchantInventory == null) this.merchantInventory = new Array<>();
            merchantInventory.add(new ShopEntry(new HealthPotion("Potion Secours", "Default", 10), 1));
        }
    }

    private void loadMapInitial(String mapName) {
        // Appelle la méthode générique sans changer la position du joueur (car pas encore créé)
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

    // --- CHANGEMENT DE CARTE AVEC POSITION (TRANSITION) ---
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
            // Attention au chemin relatif : assurez-vous que vos maps sont dans assets/tiled/map/
            // Si mapName contient déjà le chemin complet (ex: "tiled/map/interieur.tmx"), on l'utilise tel quel.
            String fullPath = mapName.contains("/") ? mapName : "tiled/map/" + mapName;

            map = new TmxMapLoader().load(fullPath);
        } catch(Exception e) {
            Gdx.app.error("Main", "Impossible de charger la map: " + mapName + " -> " + e.getMessage());
            // Fallback pour ne pas crasher
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

        // Chargement des entités via le MapLoader (qui gère Ennemis, PNJs et Exits)
        this.entities = MapLoader.loadEntities(map, this);

        // Tri des listes spécifiques
        for(Entity e : entities) {
            if(e instanceof NPC) npcs.add((NPC)e);
            if(e instanceof Enemy) enemies.add((Enemy)e);
        }
        if(collisionSystem != null) collisionSystem.setNpcs(npcs);

        // Repositionnement du joueur
        if (player != null) {
            if (startX != -1 && startY != -1) {
                // Position spécifique (transition)
                player.set_position(startX + 0.5f, startY);
            } else {
                // Position par défaut (Spawn Point de la map)
                Vector2 spawn = MapLoader.getPlayerSpawn(map);
                player.set_position(spawn.x, spawn.y);
            }
            player.snapToGrid();

            // Mise à jour caméra
            camera.position.set(player.get_positionX(), player.get_positionY(), 0);
            camera.update();
        }

        changeState(explorationState);
    }

    public void changeState(IGameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        if (currentState != null) currentState.enter();
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1/30f);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        if (currentState != null) {
            currentState.handleInput();
            currentState.update(delta);
            currentState.draw(batch);
        }

        // Gestion UI simplifiée
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

    @Override
    public void resize(int width, int height) {
        Stage[] stages = {combatStage, startStage, classStage, shopStage, menuStage, uiStage};
        for (Stage s : stages) if (s != null) s.getViewport().update(width, height, true);
        camera.setToOrtho(false, 16f, 16f * height / width);
        camera.update();
    }

    public void launchGame(boolean isWizard) {
        if (isWizard) player = Wizard.create(0, 0);
        else player = SwordMan.create(0, 0);

        player.addMoney(500);
        player.addItem(new HealthPotion("Potion de Vie", "Rend 20 PV", 20));
        player.addItem(new ManaPotion("Ether", "Rend 10 PM", 10));

        deadEnemyIds.clear();
        currentMapName = "map.tmx";
        loadMap(currentMapName); // Charge la map et place le joueur au spawn
    }

    // --- SETUP UI ET SKIN (Inchangé mais inclus pour complétude) ---
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

    private void initGameUIs() {
        initCombatUI(); initMenuUI(); initDialogUI(); initShopUI();
    }
    private void initCombatUI() { combatStage = new Stage(new ScreenViewport()); combatStage.addActor(new Table()); }
    private void initMenuUI() { menuStage = new Stage(new ScreenViewport()); menuTable = new Table(); menuTable.setFillParent(true); menuTable.center(); menuTable.setBackground(skin.getDrawable("default-rect")); menuStage.addActor(menuTable); }
    private void initDialogUI() { uiStage = new Stage(new ScreenViewport()); dialogTable = new Table(); dialogTable.setFillParent(true); dialogTable.bottom(); dialogTable.setVisible(false); dialogTable.setBackground(skin.getDrawable("default-rect")); dialogNameLabel = new Label("", skin); dialogTextLabel = new Label("", skin); dialogTable.add(dialogNameLabel).left().pad(20).row(); dialogTable.add(dialogTextLabel).width(Gdx.graphics.getWidth() - 100).left().pad(20); uiStage.addActor(dialogTable); }
    private void initShopUI() { shopStage = new Stage(new ScreenViewport()); shopTable = new Table(); shopTable.setFillParent(true); shopTable.center(); shopTable.setBackground(skin.getDrawable("default-rect")); shopStage.addActor(shopTable); }
    private void initStartMenuUI() { startStage = new Stage(new ScreenViewport()); }
    private void initClassSelectionUI() { classStage = new Stage(new ScreenViewport()); classTable = new Table(); classTable.setFillParent(true); classTable.center(); TextButton btnWarrior = new TextButton("Guerrier", skin); btnWarrior.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { launchGame(false); } }); TextButton btnWizard = new TextButton("Mage", skin); btnWizard.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { launchGame(true); } }); classTable.add(btnWarrior).pad(20); classTable.add(btnWizard).pad(20); classStage.addActor(classTable); }

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

    public void goToClassSelection() { changeState(classSelectionState); }

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
