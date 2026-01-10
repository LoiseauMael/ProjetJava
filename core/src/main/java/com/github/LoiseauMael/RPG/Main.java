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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
import com.github.LoiseauMael.RPG.utils.MapLoader;
import com.github.LoiseauMael.RPG.quests.QuestManager;
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

    public QuestManager questManager;
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
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        entities = new Array<>();
        npcs = new Array<>();
        enemies = new Array<>();
        deadEnemyIds = new Array<>();

        initSkin();
        questManager = new QuestManager();

        loadMapInitial(currentMapName);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 16f, 16f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
        camera.update();

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

    private void loadMapInitial(String mapName) {
        if (map != null) map.dispose();
        try {
            map = new TmxMapLoader().load("tiled/map/" + mapName);
            this.collisionSystem = new CollisionSystem(map);
            Entity.setCollisionSystem(this.collisionSystem);
            tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);
        } catch (Exception e) {
            Gdx.app.error("Main", "Erreur chargement carte initiale (" + mapName + "): " + e.getMessage());
            try {
                map = new TmxMapLoader().load("map.tmx");
                tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);
            } catch (Exception ex) {
                Gdx.app.error("Main", "Echec total chargement map fallback.");
            }
        }
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
    }

    @Override
    public void resize(int width, int height) {
        Stage[] stages = {combatStage, startStage, classStage, shopStage, menuStage, uiStage};
        for (Stage s : stages) {
            if (s != null) s.getViewport().update(width, height, true);
        }
        camera.setToOrtho(false, 16f, 16f * height / width);
        camera.update();
    }

    public void launchGame(boolean isWizard) {
        if (isWizard) player = Wizard.create(0, 0);
        else player = SwordMan.create(0, 0);

        player.addMoney(500);
        player.addItem(new HealthPotion("Potion de Vie", "Rend 20 PV", 20));
        player.addItem(new ManaPotion("Ether", "Rend 10 PM", 10));

        merchantInventory = new Array<>();
        merchantInventory.add(new ShopEntry(new HealthPotion("Potion Vie", "Rend 20 PV", 20), 20));
        merchantInventory.add(new ShopEntry(new ManaPotion("Ether", "Rend 10 PM", 10), 30));

        merchantInventory.add(new ShopEntry(new Weapon("Epée de Fer", "Lame standard.", SwordMan.class, 5, 0), 100));
        merchantInventory.add(new ShopEntry(new Weapon("Hache de Guerre", "Lourde.", SwordMan.class, 12, 0), 800));
        merchantInventory.add(new ShopEntry(new Weapon("Vieux Bâton", "Basique.", Wizard.class, 1, 5), 100));
        merchantInventory.add(new ShopEntry(new Weapon("Sceptre Magique", "Magique.", Wizard.class, 2, 15), 800));

        merchantInventory.add(new ShopEntry(new Armor("Veste en Cuir", "Légère.", null, 5, 2), 80));
        merchantInventory.add(new ShopEntry(new Armor("Cotte de Mailles", "Lourd.", SwordMan.class, 15, 0), 800));
        merchantInventory.add(new ShopEntry(new Armor("Petite Toge", "Tissu.", Wizard.class, 2, 10), 80));
        merchantInventory.add(new ShopEntry(new Armor("Robe de Mage", "Tissu magique.", Wizard.class, 5, 20), 800));

        deadEnemyIds.clear();
        currentMapName = "map.tmx";

        // 1. Charger la map (ne change PAS la position)
        loadMap(currentMapName);

        // 2. Appliquer le spawn par défaut uniquement pour une NOUVELLE partie
        Vector2 spawn = MapLoader.getPlayerSpawn(map);
        player.set_position(spawn.x, spawn.y);
        camera.position.set(spawn.x, spawn.y, 0);
        camera.update();
    }

    // --- CHARGEMENT DE CARTE (CORRIGÉ : NE TOUCHE PLUS AU JOUEUR) ---

    public void loadMap(String mapName) {
        if (map != null) map.dispose();
        this.currentMapName = mapName;
        try {
            map = new TmxMapLoader().load("tiled/map/" + mapName);
        } catch(Exception e) {
            Gdx.app.error("Main", "Impossible de charger la map: " + mapName);
            return;
        }

        if (tiledMapRenderer != null) tiledMapRenderer.setMap(map);
        else tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);

        this.collisionSystem = new CollisionSystem(map);
        Entity.setCollisionSystem(this.collisionSystem);

        this.entities = MapLoader.loadEntities(map, this);
        npcs.clear();
        enemies.clear();
        for(Entity e : entities) {
            if(e instanceof NPC) npcs.add((NPC)e);
            if(e instanceof Enemy) enemies.add((Enemy)e);
        }
        if(collisionSystem != null) collisionSystem.setNpcs(npcs);

        // MODIFICATION MAJEURE : On a supprimé le bloc qui réinitialisait la position du joueur ici.
        // loadMap ne s'occupe plus que du décor.

        changeState(explorationState);
    }

    private void initSkin() {
        skin = new Skin();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture whiteTexture = new Texture(pixmap);
        TextureRegion whiteRegion = new TextureRegion(whiteTexture);

        skin.add("white", whiteRegion);
        skin.add("default", new BitmapFont());

        TextureRegionDrawable whiteDrawable = new TextureRegionDrawable(whiteRegion);
        Drawable darkBackground = whiteDrawable.tint(new Color(0.1f, 0.1f, 0.1f, 0.9f));

        skin.add("default-rect", darkBackground, Drawable.class);

        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = whiteDrawable.tint(Color.DARK_GRAY);
        tbs.down = whiteDrawable.tint(Color.BLACK);
        tbs.over = whiteDrawable.tint(Color.GRAY);
        tbs.font = skin.getFont("default");
        skin.add("default", tbs);

        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = skin.getFont("default");
        skin.add("default", ls);
        skin.add("default", new ScrollPane.ScrollPaneStyle());

        ProgressBar.ProgressBarStyle pbs = new ProgressBar.ProgressBarStyle();
        pbs.background = whiteDrawable.tint(Color.DARK_GRAY);
        pbs.knobBefore = whiteDrawable.tint(Color.CYAN);
        skin.add("default-horizontal", pbs);
    }

    private void initGameUIs() {
        initCombatUI();
        initMenuUI();
        initDialogUI();
        initShopUI();
    }

    private void initCombatUI() {
        combatStage = new Stage(new ScreenViewport());
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();
        combatStage.addActor(table);
    }

    private void initMenuUI() {
        menuStage = new Stage(new ScreenViewport());
        menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.center();
        menuTable.setBackground(skin.getDrawable("default-rect"));
        menuStage.addActor(menuTable);
    }

    private void initDialogUI() {
        uiStage = new Stage(new ScreenViewport());
        dialogTable = new Table();
        dialogTable.setFillParent(true);
        dialogTable.bottom();
        dialogTable.setVisible(false);
        dialogTable.setBackground(skin.getDrawable("default-rect"));
        dialogNameLabel = new Label("", skin);
        dialogTextLabel = new Label("", skin);
        dialogTable.add(dialogNameLabel).left().pad(20).row();
        dialogTable.add(dialogTextLabel).width(Gdx.graphics.getWidth() - 100).left().pad(20);
        uiStage.addActor(dialogTable);
    }

    private void initShopUI() {
        shopStage = new Stage(new ScreenViewport());
        shopTable = new Table();
        shopTable.setFillParent(true);
        shopTable.center();
        shopTable.setBackground(skin.getDrawable("default-rect"));
        shopStage.addActor(shopTable);
    }

    public void rebuildShopLayout() {}

    private void initStartMenuUI() {
        startStage = new Stage(new ScreenViewport());
    }

    public void rebuildStartMenu() {
        startStage.clear();
        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(10).width(250).height(50);

        if (SaveManager.saveExists()) {
            TextButton btnContinue = new TextButton("Continuer", skin);
            btnContinue.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // 1. Charger la sauvegarde : Le joueur est créé et placé aux coordonnées du fichier JSON
                    SaveManager.loadGame(Main.this);

                    // 2. Charger la map : Le décor est construit autour, SANS toucher au joueur
                    loadMap(currentMapName);

                    // Sécurité : Recaler la caméra sur le joueur chargé
                    if(player != null) {
                        camera.position.set(player.get_positionX(), player.get_positionY(), 0);
                        camera.update();
                    }

                    System.out.println("Partie chargée. Joueur en : " + player.get_positionX() + "," + player.get_positionY());
                }
            });
            table.add(btnContinue).row();

            TextButton btnDelete = new TextButton("Supprimer Sauvegarde", skin);
            btnDelete.setColor(Color.FIREBRICK);
            btnDelete.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager.deleteSave();
                    rebuildStartMenu();
                }
            });
            table.add(btnDelete).row();
        } else {
            TextButton btnNew = new TextButton("Nouvelle Partie", skin);
            btnNew.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) { goToClassSelection(); }
            });
            table.add(btnNew).row();
        }

        TextButton btnExit = new TextButton("Quitter", skin);
        btnExit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { Gdx.app.exit(); }
        });
        table.add(btnExit).row();
        startStage.addActor(table);
    }

    private void initClassSelectionUI() {
        classStage = new Stage(new ScreenViewport());
        classTable = new Table();
        classTable.setFillParent(true);
        classTable.center();
        TextButton btnWarrior = new TextButton("Guerrier", skin);
        btnWarrior.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { launchGame(false); }
        });
        TextButton btnWizard = new TextButton("Mage", skin);
        btnWizard.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { launchGame(true); }
        });
        classTable.add(btnWarrior).pad(20);
        classTable.add(btnWizard).pad(20);
        classStage.addActor(classTable);
    }

    public void goToClassSelection() { changeState(classSelectionState); }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (map != null) map.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
        if (player != null) player.dispose();
        Stage[] stages = {combatStage, menuStage, startStage, classStage, shopStage, uiStage};
        for (Stage s : stages) if (s != null) s.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
    }
}
