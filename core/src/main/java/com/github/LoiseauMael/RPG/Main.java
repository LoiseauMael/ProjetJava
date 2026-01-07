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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
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

// Imports NPCs
import com.github.LoiseauMael.RPG.npcs.NPC;
import com.github.LoiseauMael.RPG.npcs.HealerNPC;
import com.github.LoiseauMael.RPG.npcs.MerchantNPC;

// Imports Items & Equipement
import com.github.LoiseauMael.RPG.items.*;

// Imports Sauvegarde
import com.github.LoiseauMael.RPG.save.SaveManager;

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
    private Array<NPC> npcs;
    private NPC activeNPC = null;
    private CollisionSystem collisionSystem;

    // --- LOGIQUE DE JEU ---
    private enum GameState { START_MENU, CLASS_SELECTION, EXPLORATION, COMBAT, MENU, SHOP }
    private GameState currentGameState = GameState.START_MENU;
    private static final float UNIT_SCALE = 1/16f;

    // --- UI GLOBALE ---
    private Skin skin;
    private BitmapFont font;

    // --- UI COMBAT ---
    private BattleSystem battleSystem;
    private Stage combatStage;

    // --- UI MENU (PAUSE) ---
    private Stage menuStage;
    private Table menuTable;
    private boolean isMenuOpen = false;

    // --- UI DIALOGUE (PNJ) ---
    private Stage uiStage;
    private Table dialogTable;
    private Label dialogTextLabel;
    private Label dialogNameLabel;

    // --- UI MAGASIN (SHOP) ---
    private Stage shopStage;
    private Table shopTable;

    private static class ShopEntry {
        Item item;
        int price;
        public ShopEntry(Item item, int price) { this.item = item; this.price = price; }
    }
    private Array<ShopEntry> merchantInventory;

    // --- UI START MENU ---
    private Stage startStage;
    private Table startTable;
    private Stage classStage;
    private Table classTable;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        initSkin();

        map = new TmxMapLoader().load("tiled/map/map.tmx");
        this.collisionSystem = new CollisionSystem(map);
        Entity.setCollisionSystem(this.collisionSystem);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE, batch);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 16f, 16f * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
        camera.update();

        // Initialisation des UIs de démarrage
        initStartMenuUI();
        initClassSelectionUI();

        enemies = new Array<>();
        npcs = new Array<>();

        Gdx.input.setInputProcessor(startStage);
    }

    // ==========================================
    // SELECTION DE CLASSE & DEMARRAGE
    // ==========================================

    private void goToClassSelection() {
        currentGameState = GameState.CLASS_SELECTION;
        Gdx.input.setInputProcessor(classStage);
    }

    private void launchGame(boolean isWizard) {
        if (isWizard) {
            player = Wizard.create(17, 80);
        } else {
            player = SwordMan.create(17, 80);
        }

        player.addMoney(150);
        player.addItem(new HealthPotion("Potion de Vie", "Rend 20 PV", 20));
        player.addItem(new ManaPotion("Ether", "Rend 10 PM", 10));

        resetWorldEntities();
        initGameUIs();

        currentGameState = GameState.EXPLORATION;
        Gdx.input.setInputProcessor(null);
    }

    private void loadSavedGame() {
        player = SaveManager.loadGame();
        if (player == null) return;

        resetWorldEntities();
        initGameUIs();

        currentGameState = GameState.EXPLORATION;
        Gdx.input.setInputProcessor(null);
    }

    private void initGameUIs() {
        initCombatUI();
        initMenuUI();
        initDialogUI();
        initShopUI();
    }

    private void resetWorldEntities() {
        enemies.clear();
        spawnEnemies();
        npcs.clear();
        spawnNPCs();

        merchantInventory = new Array<>();
        merchantInventory.add(new ShopEntry(new HealthPotion("Potion Vie", "Rend 20 PV", 20), 10));
        merchantInventory.add(new ShopEntry(new ManaPotion("Ether", "Rend 10 PM", 10), 15));
        merchantInventory.add(new ShopEntry(new Weapon("Epee Rouillee", "FOR +5", SwordMan.class, 5, 0), 50));
        merchantInventory.add(new ShopEntry(new Weapon("Epee Mithril", "FOR +25", SwordMan.class, 25, 0), 300));
        merchantInventory.add(new ShopEntry(new Weapon("Baton Bois", "FORM +5", Wizard.class, 1, 5), 50));
        merchantInventory.add(new ShopEntry(new Weapon("Sceptre Arcanique", "FORM +25", Wizard.class, 2, 25), 300));
        merchantInventory.add(new ShopEntry(new Armor("Armure Cuir", "DEF +5", null, 5, 2), 80));
        merchantInventory.add(new ShopEntry(new Relic("Amulette", "Defense +10% (Passif)", 1.0f, 0.9f), 200));
    }

    private void spawnEnemies() {
        // --- SPAWN AVEC NIVEAUX ---

        // Un Gobelin faible (Niveau 1)
        enemies.add(new Goblin(10f, 8f, 1));

        // Un Gobelin plus entraîné (Niveau 3) - Il aura ~33% de stats en plus
        enemies.add(new Goblin(12f, 9f, 3));

        // Le ROI GOBELIN (Boss Niveau 5)
        // Il sera très puissant (Stats de base + 50% environ)
        enemies.add(new KingGoblin(25f, 30f, 5));
    }

    private void spawnNPCs() {
        npcs.add(new HealerNPC(12, 10, "HealerSpriteSheet.png", "Pretre",
            "Bonjour voyageur.", "Laisse-moi soigner tes blessures."));
        npcs.add(new MerchantNPC(15, 12, "MerchantSpriteSheet.png", "Vendeur",
            "J'ai des armes pour Guerriers et Mages !"));
    }

    // ==========================================
    // INITIALISATION UIs
    // ==========================================

    private void initSkin() {
        skin = new Skin();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("default", new BitmapFont());

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.BLACK);
        textButtonStyle.over = skin.newDrawable("white", Color.GRAY);
        textButtonStyle.disabled = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        skin.add("default", labelStyle);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollStyle);

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = skin.getFont("default");
        windowStyle.background = skin.newDrawable("white", Color.DARK_GRAY);
        skin.add("default", windowStyle);
    }

    private void initClassSelectionUI() {
        classStage = new Stage(new ScreenViewport());
        classTable = new Table();
        classTable.setFillParent(true);
        classTable.center();

        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0.05f, 0.05f, 0.05f, 1);
        bg.fill();
        classTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));

        Label title = new Label("CHOISISSEZ VOTRE CLASSE", skin);
        title.setFontScale(3.0f);
        classTable.add(title).colspan(2).padBottom(50).row();

        TextButton btnWarrior = new TextButton("GUERRIER\n(Epee & Force)", skin);
        btnWarrior.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { launchGame(false); }
        });

        TextButton btnWizard = new TextButton("MAGE\n(Baton & Magie)", skin);
        btnWizard.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { launchGame(true); }
        });

        classTable.add(btnWarrior).width(300).height(200).pad(20);
        classTable.add(btnWizard).width(300).height(200).pad(20);
        classTable.row();

        TextButton btnBack = new TextButton("Retour", skin);
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                currentGameState = GameState.START_MENU;
                Gdx.input.setInputProcessor(startStage);
            }
        });
        classTable.add(btnBack).colspan(2).width(200).height(50).padTop(30);
        classStage.addActor(classTable);
    }

    private void initStartMenuUI() {
        startStage = new Stage(new ScreenViewport());
        startTable = new Table();
        startTable.setFillParent(true);
        startTable.center();

        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0.1f, 0.1f, 0.3f, 1);
        bg.fill();
        startTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));

        Label title = new Label("MON SUPER RPG", skin);
        title.setFontScale(4.0f);
        startTable.add(title).padBottom(50).row();

        final TextButton btnContinue = new TextButton("Continuer", skin);
        if (!SaveManager.saveExists()) { btnContinue.setDisabled(true); btnContinue.setColor(Color.GRAY); }
        btnContinue.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { if (!btnContinue.isDisabled()) loadSavedGame(); }
        });
        startTable.add(btnContinue).width(400).height(80).padBottom(20).row();

        TextButton btnNewGame = new TextButton("Nouvelle Partie", skin);
        btnNewGame.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (SaveManager.saveExists()) showOverwriteDialog(); else goToClassSelection();
            }
        });
        startTable.add(btnNewGame).width(400).height(80).padBottom(20).row();

        TextButton btnDelete = new TextButton("Supprimer Sauvegarde", skin);
        btnDelete.setColor(Color.FIREBRICK);
        btnDelete.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (SaveManager.saveExists()) {
                    SaveManager.deleteSave();
                    btnContinue.setDisabled(true);
                    btnContinue.setColor(Color.GRAY);
                    btnDelete.setText("Sauvegarde Supprimee");
                }
            }
        });
        startTable.add(btnDelete).width(400).height(60).padBottom(20).row();

        TextButton btnQuit = new TextButton("Quitter", skin);
        btnQuit.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { Gdx.app.exit(); }});
        startTable.add(btnQuit).width(400).height(80).row();
        startStage.addActor(startTable);
    }

    private void showOverwriteDialog() {
        Dialog dialog = new Dialog("Attention", skin) {
            @Override protected void result(Object object) { if ((Boolean) object) goToClassSelection(); }
        };
        dialog.text("Une sauvegarde existe deja.\nL'ecraser ?");
        dialog.button("Oui", true);
        dialog.button("Non", false);
        dialog.show(startStage);
    }

    private void initCombatUI() {
        combatStage = new Stage(new ScreenViewport());
        Table table = new Table();
        table.setFillParent(true);
        table.bottom().left();

        TextButton btnMove = new TextButton("Deplacement", skin);
        btnMove.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn() && !battleSystem.hasMoved()) battleSystem.startMoveSelection();
            }
        });
        TextButton btnAttack = new TextButton("Attaque", skin);
        btnAttack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.startTargetSelection(new AttackAction());
            }
        });
        TextButton btnMagic = new TextButton("Magie", skin);
        btnMagic.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.startTargetSelection(new SpellAction("Feu", 10, 20));
            }
        });
        TextButton btnArt = new TextButton("Arts", skin);
        btnArt.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.startTargetSelection(new ArtAction("Coup Fort", 5, 1.5f));
            }
        });
        TextButton btnPass = new TextButton("Passer", skin);
        btnPass.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) battleSystem.passTurn();
            }
        });

        // --- BOUTON FUIR MODIFIÉ (REGENERATION) ---
        TextButton btnFlee = new TextButton("Fuir", skin);
        btnFlee.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (isPlayerTurn()) {
                    Enemy enemy = battleSystem.getEnemy();
                    if (enemy != null) {
                        enemy.heal(enemy.getMaxPV()); // Régénération
                        BattleSystem.addLog("Fuite ! L'ennemi s'est regenere.");
                    }
                    endBattle();
                }
            }
        });

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
        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0, 0, 0, 0.9f);
        bg.fill();
        menuTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));
        menuStage.addActor(menuTable);
    }

    private void initDialogUI() {
        uiStage = new Stage(new ScreenViewport());
        dialogTable = new Table();
        dialogTable.setFillParent(true);
        dialogTable.bottom();
        dialogTable.setVisible(false);
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.8f);
        p.fill();
        dialogTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(p))));
        dialogNameLabel = new Label("", skin);
        dialogNameLabel.setColor(Color.YELLOW);
        dialogNameLabel.setFontScale(1.5f);
        dialogTextLabel = new Label("", skin);
        dialogTextLabel.setWrap(true);
        dialogTextLabel.setFontScale(1.2f);
        dialogTable.add(dialogNameLabel).left().pad(20).row();
        dialogTable.add(dialogTextLabel).width(Gdx.graphics.getWidth() - 100).left().pad(20).padBottom(30);
        uiStage.addActor(dialogTable);
    }

    private void initShopUI() {
        shopStage = new Stage(new ScreenViewport());
        shopTable = new Table();
        shopTable.setFillParent(true);
        shopTable.center();
        Pixmap bg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bg.setColor(0.1f, 0.1f, 0.2f, 0.95f);
        bg.fill();
        shopTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(bg))));
        shopStage.addActor(shopTable);
    }

    private void openShop() {
        isMenuOpen = true;
        currentGameState = GameState.SHOP;
        Gdx.input.setInputProcessor(shopStage);
        rebuildShopLayout();
    }

    private void closeShop() {
        isMenuOpen = false;
        currentGameState = GameState.EXPLORATION;
        Gdx.input.setInputProcessor(null);
    }

    private void rebuildShopLayout() {
        shopTable.clear();
        Label title = new Label("MAGASIN", skin);
        title.setFontScale(3.0f);
        title.setColor(Color.GOLD);
        shopTable.add(title).padBottom(10).row();

        Label goldLabel = new Label("Votre Or: " + player.getMoney(), skin);
        goldLabel.setFontScale(2.0f);
        goldLabel.setColor(Color.YELLOW);
        shopTable.add(goldLabel).padBottom(30).row();

        Table contentTable = new Table();
        contentTable.top();

        for (final ShopEntry entry : merchantInventory) {
            Table itemRow = new Table();
            String itemText = entry.item.getName() + " (" + entry.item.getDescription() + ")";
            Label itemLabel = new Label(itemText, skin);
            itemLabel.setFontScale(1.5f);

            String priceText = entry.price + " Or";
            TextButton buyBtn = new TextButton("Acheter (" + priceText + ")", skin);

            if (player.getMoney() >= entry.price) {
                buyBtn.setColor(Color.GREEN);
                buyBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (player.getMoney() >= entry.price) {
                            player.addMoney(-entry.price);
                            player.addItem(entry.item);
                            rebuildShopLayout();
                        }
                    }
                });
            } else {
                buyBtn.setColor(Color.RED);
                buyBtn.setDisabled(true);
            }

            itemRow.add(itemLabel).width(500).left().padRight(20);
            itemRow.add(buyBtn).width(250).height(50);
            contentTable.add(itemRow).padBottom(15).row();
        }

        ScrollPane scroll = new ScrollPane(contentTable, skin);
        scroll.setScrollingDisabled(true, false);
        shopTable.add(scroll).grow().pad(20).row();

        TextButton closeBtn = new TextButton("Quitter le magasin", skin);
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { closeShop(); }
        });
        shopTable.add(closeBtn).width(300).height(70).padBottom(20);
        shopStage.setScrollFocus(scroll);
    }

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
        float textScale = 2.0f;
        float titleScale = 2.5f;

        Table contentTable = new Table();
        contentTable.top();

        // === STATISTIQUES ===
        Label titleStats = new Label("--- STATISTIQUES ---", skin);
        titleStats.setFontScale(titleScale);
        titleStats.setColor(Color.GOLD);
        contentTable.add(titleStats).padTop(20).padBottom(20).row();

        Table statsGroup = new Table();

        // Infos de base
        addStatRow(statsGroup, "Niveau: " + player.getLevel(), textScale);
        addStatRow(statsGroup, "EXP: " + player.getExp() + " / " + player.getMaxExp(), textScale);

        // Barres de vie / mana / action
        addStatRow(statsGroup, "PV: " + player.getPV() + " / " + player.getMaxPV(), textScale);
        addStatRow(statsGroup, "PM: " + player.getPM() + " / " + player.getMaxPM(), textScale);
        addStatRow(statsGroup, "PA: " + player.getPA() + " / " + player.getMaxPA(), textScale);

        // --- STATS MANQUANTES AJOUTEES ---
        addStatRow(statsGroup, "FOR: " + player.getFOR(), textScale);
        addStatRow(statsGroup, "DEF: " + player.getDEF(), textScale);
        addStatRow(statsGroup, "FORM: " + player.getFORM(), textScale);
        addStatRow(statsGroup, "DEFM: " + player.getDEFM(), textScale);
        addStatRow(statsGroup, "VIT: " + player.getVIT(), textScale);
        addStatRow(statsGroup, "DEP: " + player.getDEP(), textScale);

        // Or
        addStatRow(statsGroup, "Or: " + player.getMoney(), textScale);

        contentTable.add(statsGroup).padBottom(40).row();

        // === EQUIPEMENT ===
        Label titleEquip = new Label("--- EQUIPEMENT ACTUEL ---", skin);
        titleEquip.setFontScale(titleScale);
        titleEquip.setColor(Color.CYAN);
        contentTable.add(titleEquip).padBottom(20).row();

        Table equippedGroup = new Table();
        addEquippedItemSlot(equippedGroup, "Arme", player.getEquippedWeapon(), textScale);
        addEquippedItemSlot(equippedGroup, "Armure", player.getEquippedArmor(), textScale);
        addEquippedItemSlot(equippedGroup, "Relique", player.getEquippedRelic(), textScale);
        contentTable.add(equippedGroup).padBottom(40).row();

        // === SAC ===
        Label titleInv = new Label("--- SAC ---", skin);
        titleInv.setFontScale(titleScale);
        titleInv.setColor(Color.ORANGE);
        contentTable.add(titleInv).padBottom(20).row();

        if (player.getInventory().size == 0) {
            Label empty = new Label("Vide", skin);
            empty.setFontScale(textScale);
            contentTable.add(empty).padBottom(20).row();
        } else {
            for (final Item item : player.getInventory()) {
                String txt = item.getName() + " x" + item.getCount();
                TextButton itemBtn = new TextButton(txt, skin);
                itemBtn.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) {
                        if (item instanceof Equipment) {
                            player.equip((Equipment)item);
                            rebuildMenuLayout();
                        } else {
                            player.consumeItem(item);
                            rebuildMenuLayout();
                        }
                    }
                });
                contentTable.add(itemBtn).width(450).height(70).padBottom(5).row();
                Label desc = new Label(item.getDescription(), skin);
                desc.setColor(Color.LIGHT_GRAY);
                contentTable.add(desc).padBottom(25).row();
            }
        }

        // --- BOUTONS CONTROLE ---
        TextButton btnSave = new TextButton("Sauvegarder", skin);
        btnSave.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SaveManager.saveGame(player);
                btnSave.setText("Partie Sauvegardee !");
            }
        });
        contentTable.add(btnSave).padTop(40).width(300).height(80).row();

        TextButton btnResume = new TextButton("Reprendre", skin);
        btnResume.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { closeMenu(); }});
        contentTable.add(btnResume).padTop(20).width(300).height(80).row();

        TextButton btnQuit = new TextButton("Quitter", skin);
        btnQuit.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { Gdx.app.exit(); }});
        contentTable.add(btnQuit).padTop(20).width(300).height(80).padBottom(50).row();

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setScrollingDisabled(true, false);
        menuTable.add(scrollPane).grow().pad(20);
        menuStage.setScrollFocus(scrollPane);
    }

    private void addEquippedItemSlot(Table t, final String slotName, final Equipment item, float scale) {
        String txt = slotName + ": " + (item != null ? item.getName() : "(Vide)");
        TextButton btn = new TextButton(txt, skin);
        if (item != null) {
            btn.setColor(Color.GREEN);
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    player.unequip(item);
                    rebuildMenuLayout();
                }
            });
        } else {
            btn.setColor(Color.DARK_GRAY);
        }
        t.add(btn).width(500).height(60).pad(5).row();
    }

    private void addStatRow(Table t, String text, float scale) {
        Label l = new Label(text, skin);
        l.setFontScale(scale);
        t.add(l).center().padBottom(10).row();
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.M) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (currentGameState == GameState.EXPLORATION) openMenu();
            else if (currentGameState == GameState.MENU) closeMenu();
            else if (currentGameState == GameState.SHOP) closeShop();
        }

        if (currentGameState == GameState.START_MENU) {
            startStage.act(delta);
            startStage.draw();
        }
        else if (currentGameState == GameState.CLASS_SELECTION) {
            classStage.act(delta);
            classStage.draw();
        }
        else if (currentGameState == GameState.EXPLORATION) {
            updateExploration(delta);
            drawExploration();
        }
        else if (currentGameState == GameState.COMBAT) {
            updateCombat(delta);
            drawCombat();
        }
        else if (currentGameState == GameState.MENU) {
            drawExploration();
            menuStage.act(delta);
            menuStage.draw();
        }
        else if (currentGameState == GameState.SHOP) {
            drawExploration();
            shopStage.act(delta);
            shopStage.draw();
        }
    }

    private void updateExploration(float delta) {
        player.handleInput();
        player.update(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            if (activeNPC != null) {
                activeNPC = null;
                dialogTable.setVisible(false);
                if (currentGameState == GameState.SHOP) closeShop();
            }
            else {
                for (NPC npc : npcs) {
                    if (Vector2.dst(player.get_positionX(), player.get_positionY(),
                        npc.get_positionX(), npc.get_positionY()) < 2.5f) {

                        activeNPC = npc;
                        npc.onInteract(player);

                        if (npc instanceof MerchantNPC) {
                            openShop();
                        } else {
                            dialogNameLabel.setText(npc.getName());
                            dialogTextLabel.setText(npc.getDialogues()[0]);
                            dialogTable.setVisible(true);
                        }
                        break;
                    }
                }
            }
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);

            float distance = Vector2.dst(player.get_positionX(), player.get_positionY(),
                enemy.get_positionX(), enemy.get_positionY());

            if (distance < 1.5f) {
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
        for (NPC npc : npcs) npc.draw(batch);
        for (Enemy e : enemies) e.draw(batch);
        player.draw(batch);
        batch.end();

        if (activeNPC != null && currentGameState != GameState.SHOP) {
            uiStage.draw();
        }
    }

    private void startBattle(Enemy enemy) {
        currentGameState = GameState.COMBAT;
        float startDistance = 4.0f;
        Vector2 direction = new Vector2(player.get_positionX() - enemy.get_positionX(),
            player.get_positionY() - enemy.get_positionY());
        if (direction.len() == 0) direction.set(0, -1);
        direction.nor().scl(startDistance);
        int targetTileX = Math.round(enemy.get_positionX() + direction.x);
        int targetTileY = Math.round(enemy.get_positionY() + direction.y);
        float finalX = (targetTileX + 0.5f) - (player.getSprite().getWidth() / 2f);
        float finalY = (targetTileY + 0.5f) - (player.getSprite().getHeight() / 2f);
        player.set_position(finalX, finalY);
        camera.position.set(player.get_positionX(), player.get_positionY(), 0);
        camera.update();
        battleSystem = new BattleSystem(player, enemy);
        Gdx.input.setInputProcessor(combatStage);
        player.set_velocityX(0); player.set_velocityY(0);
    }

    private void endBattle() {
        currentGameState = GameState.EXPLORATION;
        battleSystem = null;
        Gdx.input.setInputProcessor(null);
    }

    private void updateCombat(float delta) {
        if (battleSystem == null) return;
        battleSystem.update(delta);

        if (battleSystem.getState() == BattleState.PLAYER_TURN ||
            battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) player.setDirection(3);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) player.setDirection(0);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) player.setDirection(1);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) player.setDirection(2);
        }

        if (Gdx.input.justTouched()) {
            if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) battleSystem.cancelSelection();
            else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touchPos);
                if (battleSystem.getState() == BattleState.PLAYER_MOVING) battleSystem.tryMovePlayerTo(touchPos.x, touchPos.y);
                else if (battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) battleSystem.tryAttackTarget(touchPos.x, touchPos.y);
            }
        }

        combatStage.act(delta);
        camera.position.set(player.get_positionX(), player.get_positionY(), 0);
        camera.update();

        // --- GESTION VICTOIRE (XP + OR) ---
        if (battleSystem.getState() == BattleState.VICTORY) {
            Enemy defeatedEnemy = battleSystem.getEnemy();

            // Calcul Récompenses
            int xpReward = 50 * defeatedEnemy.getLevel();
            int goldReward = 20 * defeatedEnemy.getLevel();

            // Bonus Boss/Elite
            if (defeatedEnemy instanceof BossEnemy) {
                xpReward *= 5; goldReward *= 5;
            } else if (defeatedEnemy instanceof EliteEnemy) {
                xpReward *= 2; goldReward *= 2;
            }

            // Distribution
            player.gainExp(xpReward);
            player.addMoney(goldReward);

            // Log de victoire
            BattleSystem.addLog("Victoire ! Gain: " + xpReward + " XP, " + goldReward + " Or.");

            // Nettoyage
            enemies.removeValue(defeatedEnemy, true);
            endBattle();
        }
        else if (battleSystem.getState() == BattleState.GAME_OVER) {
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
        float centerX = player.get_positionX() + (player.getSprite().getWidth() / 2f);
        float centerY = player.get_positionY() + (player.getSprite().getHeight() / 2f);
        if (battleSystem != null && battleSystem.getState() == BattleState.PLAYER_MOVING) drawGridZone(centerX, centerY, player.getDEP(), Color.CYAN);
        if (battleSystem != null && battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) {
            BattleAction action = battleSystem.getPendingAction();
            Array<Vector2> tiles = action.getTargetableTiles(player);
            if (tiles != null) drawSpecificTiles(tiles, Color.RED);
            else drawGridZone(centerX, centerY, action.getRange(), Color.RED);
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        if (battleSystem != null && battleSystem.getEnemy() != null) battleSystem.getEnemy().draw(batch);
        batch.end();
        combatStage.draw();

        // --- AFFICHAGE UI & LOGS ---
        batch.setProjectionMatrix(combatStage.getCamera().combined);
        batch.begin();
        if (battleSystem != null) {
            font.getData().setScale(1.5f);
            float currentY = Gdx.graphics.getHeight() - 20;
            float lineHeight = 35f;

            // Info Etat
            String stateTxt = "ETAT: " + battleSystem.getState();
            if (battleSystem.getState() == BattleState.PLAYER_MOVING) stateTxt += " (Clic: Bouger)";
            if (battleSystem.getState() == BattleState.PLAYER_SELECTING_TARGET) stateTxt += " (Clic: Valider)";
            font.draw(batch, stateTxt, 20, currentY);
            currentY -= lineHeight * 1.5f;

            // Info Ennemi
            int enemyATB = (int) battleSystem.getEnemyATB();
            String eInfo = "ENNEMI (" + battleSystem.getEnemy().getClass().getSimpleName() + ") PV: " + battleSystem.getEnemy().getPV();
            if (enemyATB >= 100) font.setColor(Color.RED); else font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "ATB Ennemi: " + enemyATB + "%", 20, currentY);
            currentY -= lineHeight;
            font.setColor(Color.WHITE);
            font.draw(batch, eInfo, 20, currentY);
            currentY -= lineHeight * 1.5f;

            // Info Joueur
            int playerATB = (int) battleSystem.getPlayerATB();
            String pInfo = "JOUEUR PV: " + player.getPV() + "/" + player.getMaxPV() + " | PM: " + player.getPM();
            if (playerATB >= 100) font.setColor(Color.GREEN); else font.setColor(Color.WHITE);
            font.draw(batch, "ATB Joueur: " + playerATB + "%", 20, currentY);
            currentY -= lineHeight;
            font.setColor(Color.WHITE);
            font.draw(batch, pInfo, 20, currentY);
            currentY -= lineHeight * 2.0f;

            // --- JOURNAL DE COMBAT (LOGS) ---
            font.setColor(Color.YELLOW);
            font.draw(batch, "--- JOURNAL ---", 20, currentY);
            currentY -= lineHeight;
            font.getData().setScale(1.2f);
            font.setColor(Color.CYAN);

            Array<String> logs = BattleSystem.getLogs();
            for (int i = 0; i < logs.size; i++) {
                font.draw(batch, logs.get(i), 20, currentY);
                currentY -= 25f;
            }
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
        if (startStage != null) startStage.dispose();
        if (classStage != null) classStage.dispose();
        if (shopStage != null) shopStage.dispose();
        if (skin != null) skin.dispose();
        if (font != null) font.dispose();
    }
}
