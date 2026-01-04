package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// Imports Battle
import com.github.LoiseauMael.RPG.battle.AttackAction;
import com.github.LoiseauMael.RPG.battle.BattleSystem;
import com.github.LoiseauMael.RPG.battle.BattleSystem.BattleState;
import com.github.LoiseauMael.RPG.battle.SpellAction;
import com.github.LoiseauMael.RPG.battle.ArtAction;
import com.github.LoiseauMael.RPG.battle.BattleAction;

// Imports Items
import com.github.LoiseauMael.RPG.items.Item;
import com.github.LoiseauMael.RPG.items.HealthPotion;
import com.github.LoiseauMael.RPG.items.ManaPotion;
import com.github.LoiseauMael.RPG.items.EnergyPotion;

public class Main extends ApplicationAdapter {

    // --- RENDU & MAP ---
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private TiledMap map;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    // --- ENTITÉS ---
    private Player player;
    private Array<Enemy> enemies;
    private CollisionSystem collisionSystem;

    // --- LOGIQUE DE JEU ---
    private enum GameState { EXPLORATION, COMBAT, MENU }
    private GameState currentGameState = GameState.EXPLORATION;
    private static final float UNIT_SCALE = 1/16f;

    // --- UI GLOBALE ---
    private Skin skin;
    private BitmapFont font;

    // --- UI COMBAT ---
    private BattleSystem battleSystem;
    private Stage combatStage;

    // --- UI MENU ---
    private Stage menuStage;
    private Table menuTable;
    private boolean isMenuOpen = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        // 1. Map & Collisions
        map = new TmxMapLoader().load("tiled/map/map.tmx");
        this.collisionSystem = new CollisionSystem(map);
        Entity.setCollisionSystem(this.collisionSystem);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);

        // 2. Caméra
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 16f, 16f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
        camera.update();

        // 3. Joueur
        player = SwordMan.create(17, 80);

        // --- INVENTAIRE DE DÉPART ---
        player.addItem(new HealthPotion("Potion de Vie", "Rend 20 PV", 20));
        player.addItem(new ManaPotion("Ether", "Rend 10 PM", 10));
        player.addItem(new EnergyPotion("Boisson Energ.", "Rend 5 PA", 5));

        // Test de l'empilement (stacking)
        player.addItem(new HealthPotion("Potion de Vie", "Rend 20 PV", 20));

        // 4. Ennemis
        enemies = new Array<>();
        spawnEnemies();

        // 5. Initialisation UI
        initSkin(); // Création du style global
        initCombatUI();
        initMenuUI();
    }

    private void spawnEnemies() {
        String texturePath = "EnnemyKingSpriteSheet.png";
        // Signature : create(x, y, radius, level, texturePath)
        enemies.add(Enemy.create(10f, 8f, 3.0f, 1, texturePath));
        enemies.add(Enemy.create(15f, 5f, 2.0f, 3, texturePath));
        enemies.add(Enemy.create(25f, 30f, 4.0f, 5, texturePath));
    }

    // ==========================================
    // INITIALISATION UI
    // ==========================================

    private void initSkin() {
        skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("default", new BitmapFont()); // Police par défaut de LibGDX

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.BLACK);
        textButtonStyle.over = skin.newDrawable("white", Color.GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        skin.add("default", labelStyle);
    }

    private void initCombatUI() {
        combatStage = new Stage(new ScreenViewport());

        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();

        // Boutons
        TextButton btnMove = new TextButton("Deplacement", skin);
        btnMove.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn() && !battleSystem.hasMoved()) battleSystem.startMoveSelection();
            }
        });

        TextButton btnAttack = new TextButton("Attaque", skin);
        btnAttack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.startTargetSelection(new AttackAction());
            }
        });

        TextButton btnMagic = new TextButton("Magie", skin);
        btnMagic.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.startTargetSelection(new SpellAction("Feu", 10, 20));
            }
        });

        TextButton btnArt = new TextButton("Arts", skin);
        btnArt.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.startTargetSelection(new ArtAction("Coup Fort", 5, 1.5f));
            }
        });

        TextButton btnPass = new TextButton("Passer", skin);
        btnPass.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.passTurn();
            }
        });

        TextButton btnFlee = new TextButton("Fuir", skin);
        btnFlee.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) endBattle();
            }
        });

        // Layout Combat
        float w = 120, h = 40, pad = 5;
        table.add(btnMove).width(w).height(h).pad(pad);
        table.add(btnAttack).width(w).height(h).pad(pad);
        table.row();
        table.add(btnMagic).width(w).height(h).pad(pad);
        table.add(btnArt).width(w).height(h).pad(pad);
        table.row();
        table.add(btnPass).width(w).height(h).pad(pad);
        table.add(btnFlee).width(w).height(h).pad(pad);

        combatStage.addActor(table);
    }

    private void initMenuUI() {
        menuStage = new Stage(new ScreenViewport());

        menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.center();

        // Fond semi-transparent
        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0, 0, 0, 0.9f); // Assez sombre
        bg.fill();
        menuTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));

        menuStage.addActor(menuTable);
    }

    // ==========================================
    // GESTION DU MENU (PAUSE)
    // ==========================================

    private void openMenu() {
        isMenuOpen = true;
        currentGameState = GameState.MENU;
        Gdx.input.setInputProcessor(menuStage);
        rebuildMenuLayout();
    }

    private void closeMenu() {
        isMenuOpen = false;
        currentGameState = GameState.EXPLORATION;
        Gdx.input.setInputProcessor(null);
    }

    private void rebuildMenuLayout() {
        menuTable.clear();

        // Paramètres de taille et d'espacement
        float textScale = 2.5f;
        float descScale = 1.8f;
        float titleScale = 3.0f;

        // --- COLONNE GAUCHE : STATS ---
        Table statsTable = new Table();

        Label titleStats = new Label("--- STATISTIQUES ---", skin);
        titleStats.setFontScale(titleScale);
        statsTable.add(titleStats).padBottom(40).row();

        addStatRow(statsTable, "Niveau: " + player.getLevel(), textScale);
        addStatRow(statsTable, "EXP: " + player.getExp() + " / " + player.getMaxExp(), textScale);
        addStatRow(statsTable, "PV: " + player.getPV() + " / " + player.getMaxPV(), textScale);
        addStatRow(statsTable, "PM: " + player.getPM() + " / " + player.getMaxPM(), textScale);
        // Utilisation de getMaxPA() (ajouté dans Player.java)
        addStatRow(statsTable, "PA: " + player.getPA() + " / " + player.getMaxPA(), textScale);
        addStatRow(statsTable, "FOR: " + player.getFOR(), textScale);
        addStatRow(statsTable, "DEF: " + player.getDEF(), textScale);
        addStatRow(statsTable, "Or: " + player.getMoney(), textScale);

        // --- COLONNE DROITE : INVENTAIRE ---
        Table itemsTable = new Table();

        Label titleInv = new Label("--- INVENTAIRE ---", skin);
        titleInv.setFontScale(titleScale);
        itemsTable.add(titleInv).padBottom(40).row();

        if (player.getInventory().size == 0) {
            Label empty = new Label("Vide", skin);
            empty.setFontScale(textScale);
            itemsTable.add(empty);
        } else {
            for (final Item item : player.getInventory()) {
                String txt = item.getName() + " x" + item.getCount();

                TextButton itemBtn = new TextButton(txt, skin);
                itemBtn.getLabel().setFontScale(textScale);

                itemBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        player.consumeItem(item);
                        rebuildMenuLayout(); // Rafraîchir après consommation
                    }
                });

                itemsTable.add(itemBtn).width(400).height(60).padBottom(5).row();

                Label descLabel = new Label(item.getDescription(), skin);
                descLabel.setFontScale(descScale);
                descLabel.setColor(Color.LIGHT_GRAY);
                itemsTable.add(descLabel).padBottom(25).row();
            }
        }

        // --- BOUTONS CONTROLES ---
        TextButton btnResume = new TextButton("Reprendre", skin);
        btnResume.getLabel().setFontScale(textScale);
        btnResume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeMenu();
            }
        });

        TextButton btnQuit = new TextButton("Quitter Jeu", skin);
        btnQuit.getLabel().setFontScale(textScale);
        btnQuit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Assemblage Final
        menuTable.add(statsTable).top().left().pad(50).width(500);
        menuTable.add(itemsTable).top().right().pad(50).width(500);
        menuTable.row();

        menuTable.add(btnResume).padTop(80).width(350).height(80);
        menuTable.add(btnQuit).padTop(80).width(350).height(80);
    }

    private void addStatRow(Table t, String text, float scale) {
        Label l = new Label(text, skin);
        l.setFontScale(scale);
        t.add(l).left().padBottom(15).row();
    }

    private boolean isPlayerTurn() {
        return battleSystem != null && battleSystem.getState() == BattleState.PLAYER_TURN;
    }

    // ==========================================
    // BOUCLE DE JEU (RENDER)
    // ==========================================

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1/30f);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        // --- INPUT MENU ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (currentGameState == GameState.EXPLORATION) {
                openMenu();
            } else if (currentGameState == GameState.MENU) {
                closeMenu();
            }
        }

        // --- GESTION ÉTATS ---
        if (currentGameState == GameState.EXPLORATION) {
            updateExploration(delta);
            drawExploration();
        }
        else if (currentGameState == GameState.COMBAT) {
            updateCombat(delta);
            drawCombat();
        }
        else if (currentGameState == GameState.MENU) {
            // Dessine l'exploration figée en fond
            drawExploration();
            // Dessine le menu par dessus
            menuStage.act(delta);
            menuStage.draw();
        }
    }

    // ==========================================
    // LOGIQUE EXPLORATION
    // ==========================================

    private void updateExploration(float delta) {
        player.handleInput();
        player.update(delta);

        Rectangle weaponBounds = null;
        if (player.isAttacking) {
            weaponBounds = new Rectangle(player.getBounds());
            weaponBounds.x -= 0.5f; weaponBounds.y -= 0.5f;
            weaponBounds.width += 1f; weaponBounds.height += 1f;
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);

            if (player.getBounds().overlaps(enemy.getBounds())) {
                startBattle(enemy);
                break;
            }
            if (weaponBounds != null && weaponBounds.overlaps(enemy.getBounds())) {
                startBattle(enemy);
                break;
            }
        }
        camera.position.set(player.get_positionX(), player.get_positionY(), 0);
        camera.update();
    }

    private void drawExploration() {
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        for (Enemy e : enemies) e.draw(batch);
        batch.end();
    }

    // ==========================================
    // LOGIQUE COMBAT
    // ==========================================

    private void startBattle(Enemy enemy) {
        Gdx.app.log("Main", "COMBAT LANCÉ contre " + enemy.getClass().getSimpleName());
        currentGameState = GameState.COMBAT;

        // Repositionnement tactique
        float startDistance = 4.0f;
        Vector2 direction = new Vector2(player.get_positionX() - enemy.get_positionX(),
            player.get_positionY() - enemy.get_positionY());
        if (direction.len() == 0) direction.set(0, -1);
        direction.nor().scl(startDistance);

        int targetTileX = Math.round(enemy.get_positionX() + direction.x);
        int targetTileY = Math.round(enemy.get_positionY() + direction.y);

        // Centrage
        float spriteWidth = player.getSprite().getWidth();
        float spriteHeight = player.getSprite().getHeight();

        float finalX = (targetTileX + 0.5f) - (spriteWidth / 2f);
        float finalY = (targetTileY + 0.5f) - (spriteHeight / 2f);

        player.set_position(finalX, finalY);
        camera.position.set(player.get_positionX(), player.get_positionY(), 0);
        camera.update();

        battleSystem = new BattleSystem(player, enemy);
        Gdx.input.setInputProcessor(combatStage);

        player.set_velocityX(0);
        player.set_velocityY(0);
    }

    private void endBattle() {
        currentGameState = GameState.EXPLORATION;
        battleSystem = null;
        Gdx.input.setInputProcessor(null);
    }

    private void updateCombat(float delta) {
        if (battleSystem == null) return;
        battleSystem.update(delta);

        // Rotation
        if (battleSystem.getState() == BattleState.PLAYER_TURN ||
            battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                player.setDirection(3);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                player.setDirection(0);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                player.setDirection(1);
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                player.setDirection(2);
            }
        }

        // Clics
        if (Gdx.input.justTouched()) {
            if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                battleSystem.cancelSelection();
            }
            else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);

                if (battleSystem.getState() == BattleState.PLAYER_MOVING) {
                    battleSystem.tryMovePlayerTo(touchPos.x, touchPos.y);
                } else if (battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) {
                    boolean hit = battleSystem.tryAttackTarget(touchPos.x, touchPos.y);
                    if (hit) Gdx.app.log("Combat", "Action validée !");
                }
            }
        }

        combatStage.act(delta);
        camera.position.set(player.get_positionX(), player.get_positionY(), 0);
        camera.update();

        if (battleSystem.getState() == BattleState.VICTORY) {
            enemies.removeValue(battleSystem.getEnemy(), true);
            endBattle();
        } else if (battleSystem.getState() == BattleState.GAME_OVER) {
            player.setPV(100);
            endBattle();
        }
    }

    private void drawCombat() {
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);

        // Zones bleues/rouges centrées
        float centerX = player.get_positionX() + (player.getSprite().getWidth() / 2f);
        float centerY = player.get_positionY() + (player.getSprite().getHeight() / 2f);

        if (battleSystem != null && battleSystem.getState() == BattleState.PLAYER_MOVING) {
            drawGridZone(centerX, centerY, player.getDEP(), Color.CYAN);
        }

        if (battleSystem != null && battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) {
            BattleAction action = battleSystem.getPendingAction();
            Array<Vector2> tiles = action.getTargetableTiles(player);

            if (tiles != null) {
                drawSpecificTiles(tiles, Color.RED);
            } else {
                drawGridZone(centerX, centerY, action.getRange(), Color.RED);
            }
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        if (battleSystem != null && battleSystem.getEnemy() != null) {
            battleSystem.getEnemy().draw(batch);
        }
        batch.end();

        combatStage.draw();

        // HUD Combat
        batch.setProjectionMatrix(combatStage.getCamera().combined);
        batch.begin();
        if (battleSystem != null) {
            String stateTxt = "TOUR: " + battleSystem.getState();
            if (battleSystem.getState() == BattleState.PLAYER_MOVING) stateTxt += " (Clic G: Bouger)";
            if (battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) stateTxt += " (Clic G: Valider)";
            font.draw(batch, stateTxt, 20, Gdx.graphics.getHeight() - 80);

            String pInfo = "Lvl " + player.getLevel() +
                " | XP: " + player.getExp() + "/" + player.getMaxExp() +
                " | Or: " + player.getMoney() +
                " | PV: " + player.getPV();
            font.draw(batch, pInfo, 20, Gdx.graphics.getHeight() - 20);

            String eInfo = "Ennemi Lvl " + battleSystem.getEnemy().getLevel() +
                " | PV: " + battleSystem.getEnemy().getPV();
            font.draw(batch, eInfo, 20, Gdx.graphics.getHeight() - 50);
        }
        batch.end();
    }

    private void drawGridZone(float centerX, float centerY, float radius, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, 0.2f);
        int r = (int) Math.ceil(radius);
        int cx = (int) Math.floor(centerX);
        int cy = (int) Math.floor(centerY);
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (Vector2.dst(cx, cy, x, y) <= radius) shapeRenderer.rect(x, y, 1, 1);
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        for (int x = cx - r; x <= cx + r; x++) {
            for (int y = cy - r; y <= cy + r; y++) {
                if (Vector2.dst(cx, cy, x, y) <= radius) shapeRenderer.rect(x, y, 1, 1);
            }
        }
        shapeRenderer.end();
    }

    private void drawSpecificTiles(Array<Vector2> tiles, Color color) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color.r, color.g, color.b, 0.4f);
        for (Vector2 tile : tiles) shapeRenderer.rect(tile.x, tile.y, 1, 1);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        for (Vector2 tile : tiles) shapeRenderer.rect(tile.x, tile.y, 1, 1);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (map != null) map.dispose();
        if (tiledMapRenderer != null) tiledMapRenderer.dispose();
        if (player != null) player.dispose();
        for (Enemy e : enemies) e.dispose();
        if (combatStage != null) combatStage.dispose();
        if (menuStage != null) menuStage.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
    }
}
