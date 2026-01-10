package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch; // Import correct
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.Player;
import com.github.LoiseauMael.RPG.items.Equipment;
import com.github.LoiseauMael.RPG.items.Item;
import com.github.LoiseauMael.RPG.save.SaveManager;

public class MenuState implements IGameState {
    private Main game;

    public MenuState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Gdx.input.setInputProcessor(game.menuStage);
        rebuildMenu();
    }

    @Override
    public void exit() {
        game.menuTable.clear();
    }

    private void rebuildMenu() {
        game.menuTable.clear();
        game.menuTable.defaults().pad(5);
        Player p = game.player;

        // --- PARTIE GAUCHE : STATS & EXP ---
        Table leftTable = new Table(game.skin);
        // Utilisation du background enregistré dans Main.initSkin()
        leftTable.setBackground(game.skin.getDrawable("default-rect"));

        leftTable.add(new Label("--- STATISTIQUES (Niveau " + p.getLevel() + ") ---", game.skin)).colspan(2).row();

        // Grille des stats
        addStatRow(leftTable, "PV", p.getPV() + "/" + p.getMaxPV(), "PM", p.getPM() + "/" + p.getMaxPM());
        addStatRow(leftTable, "PA", p.getPA() + "/" + p.getMaxPA(), "VIT",String.valueOf(p.getVIT()));
        addStatRow(leftTable, "FOR", String.valueOf(p.getFOR()), "DEF", String.valueOf(p.getDEF()));
        addStatRow(leftTable, "M.FOR", String.valueOf(p.getFORM()), "M.DEF", String.valueOf(p.getDEFM()));
        addStatRow(leftTable, "DEP", String.valueOf(p.getDEP()), "Or", p.getMoney() + " G");

        // Barre d'expérience
        leftTable.add(new Label("Expérience:", game.skin)).left().padTop(10);

        // Calcul du pourcentage d'XP
        float progress = (p.getExpForNextLevel() > 0) ? (float) p.getExp() / p.getExpForNextLevel() : 0;
        ProgressBar.ProgressBarStyle progressStyle = new ProgressBar.ProgressBarStyle(game.skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class));

        // Si le style n'a pas de knob, on en crée un simple (sécurité)
        if (progressStyle.knob == null && progressStyle.knobBefore == null) {
            progressStyle.knobBefore = getColoredDrawable(10, 10, Color.CYAN);
        }

        ProgressBar expBar = new ProgressBar(0, 1, 0.01f, false, progressStyle);
        expBar.setValue(progress);

        leftTable.add(expBar).width(150).padTop(10).row();
        leftTable.add(new Label(p.getExp() + " / " + p.getExpForNextLevel() + " XP", game.skin)).colspan(2).center().row();


        // --- PARTIE CENTRE : SLOTS D'EQUIPEMENT ---
        Table equipmentTable = new Table(game.skin);
        equipmentTable.add(new Label("--- EQUIPEMENT ---", game.skin)).padBottom(10).row();

        // Slot Arme
        equipmentTable.add(new Label("Arme:", game.skin)).left();
        equipmentTable.add(createEquipmentSlot(p.getEquippedWeapon())).width(200).row();

        // Slot Armure
        equipmentTable.add(new Label("Armure:", game.skin)).left();
        equipmentTable.add(createEquipmentSlot(p.getEquippedArmor())).width(200).row();

        // Slot Relique
        equipmentTable.add(new Label("Relique:", game.skin)).left();
        equipmentTable.add(createEquipmentSlot(p.getEquippedRelic())).width(200).row();

        Label helpLabel = new Label("(Cliquez pour retirer)", game.skin);
        helpLabel.setFontScale(0.8f);
        equipmentTable.add(helpLabel).padTop(5).colspan(2).row();


        // --- PARTIE DROITE : INVENTAIRE ---
        Table rightTable = new Table(game.skin);
        rightTable.add(new Label("--- SAC ---", game.skin)).row();

        Table invList = new Table();
        invList.top();

        if (p.getInventory().isEmpty()) {
            invList.add(new Label("Vide", game.skin)).pad(10);
        } else {
            for (Item item : p.getInventory()) {
                TextButton itemBtn = new TextButton(item.getName() + " (x" + item.getCount() + ")", game.skin);
                // Si c'est un équipement, on change la couleur pour le distinguer
                if (item instanceof Equipment) {
                    itemBtn.setColor(Color.GOLD);
                }

                itemBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        game.player.consumeItem(item);
                        rebuildMenu(); // Rafraîchir tout l'affichage
                    }
                });
                invList.add(itemBtn).width(180).pad(2).row();
            }
        }

        ScrollPane scroll = new ScrollPane(invList, game.skin);
        scroll.setFadeScrollBars(false);
        rightTable.add(scroll).width(200).height(300).row();


        // --- BAS : BOUTONS D'ACTION ---
        Table actionTable = new Table();
        TextButton btnResume = new TextButton("Retour Jeu", game.skin);
        btnResume.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { game.changeState(game.explorationState); }
        });

        TextButton btnSave = new TextButton("Sauvegarder", game.skin);
        btnSave.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SaveManager.saveGame(game);
                btnSave.setText("Sauvegardé !");
            }
        });

        TextButton btnQuit = new TextButton("Quitter", game.skin);
        btnQuit.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { game.changeState(game.startMenuState); }
        });

        actionTable.add(btnResume).width(120).pad(5);
        actionTable.add(btnSave).width(120).pad(5);
        actionTable.add(btnQuit).width(120).pad(5);

        // --- ASSEMBLAGE ---
        Table mainContent = new Table();
        mainContent.add(leftTable).top().pad(10);
        mainContent.add(equipmentTable).top().pad(10);
        mainContent.add(rightTable).top().pad(10);

        game.menuTable.add(mainContent).row();
        game.menuTable.add(actionTable).padTop(20);
    }

    private void addStatRow(Table t, String label1, String val1, String label2, String val2) {
        t.add(new Label(label1 + ": " + val1, game.skin)).left().padRight(10);
        t.add(new Label(label2 + ": " + val2, game.skin)).left().row();
    }

    private TextButton createEquipmentSlot(Equipment equip) {
        String text = (equip != null) ? equip.getName() : "<Vide>";
        TextButton btn = new TextButton(text, game.skin);

        if (equip != null) {
            btn.setColor(Color.LIME);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.player.unequip(equip);
                    rebuildMenu();
                }
            });
        } else {
            btn.setDisabled(true);
            btn.setColor(Color.DARK_GRAY);
        }
        return btn;
    }

    private TextureRegionDrawable getColoredDrawable(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

    @Override public void update(float delta) {}

    // --- CORRECTION DU BUG DE SPRITEBATCH ---
    @Override public void draw(SpriteBatch batch) {
        // On n'appelle PAS batch.begin() ici car explorationState.draw() gère déjà son propre batch/renderer
        // ou s'il ne le fait pas, c'est à lui de le faire.
        // MAIS attention : si explorationState.draw() utilise TiledMapRenderer, celui-ci a son propre begin/end.
        // Si explorationState.draw() dessine aussi des entités avec batch, il fait ses propres begin/end.

        // 1. Dessiner le jeu en arrière-plan
        game.explorationState.draw(batch);

        // 2. Dessiner l'UI par dessus
        // Stage gère son propre batch.begin/end, donc on est bon.
        game.menuStage.act();
        game.menuStage.draw();
    }

    @Override public void handleInput() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) game.changeState(game.explorationState);
    }
}
