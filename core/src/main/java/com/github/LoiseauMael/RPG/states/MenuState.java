package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.model.entities.Player;
import com.github.LoiseauMael.RPG.items.Equipment;
import com.github.LoiseauMael.RPG.items.Item;
import com.github.LoiseauMael.RPG.save.SaveManager;

/**
 * État représentant le menu de pause (In-Game Menu).
 * <p>
 * Permet au joueur de :
 * <ul>
 * <li>Consulter ses statistiques (PV, PM, PA, XP, Or, etc.).</li>
 * <li>Gérer son équipement (Arme, Armure, Relique).</li>
 * <li>Utiliser des objets depuis l'inventaire.</li>
 * <li>Sauvegarder la partie ou quitter le jeu.</li>
 * </ul>
 */
public class MenuState implements IGameState {
    private Main game;

    public MenuState(Main game) {
        this.game = game;
    }

    /**
     * Active le processeur d'entrée pour l'interface du menu (Stage)
     * et reconstruit l'interface pour afficher les données à jour.
     */
    @Override
    public void enter() {
        Gdx.input.setInputProcessor(game.menuStage);
        rebuildMenu();
    }

    /**
     * Nettoie le tableau du menu à la sortie.
     */
    @Override
    public void exit() {
        game.menuTable.clear();
    }

    /**
     * Construit dynamiquement l'interface du menu.
     * Crée les sections Stats, Équipement et Inventaire basées sur l'état actuel du joueur.
     */
    private void rebuildMenu() {
        game.menuTable.clear();
        game.menuTable.defaults().pad(5);
        Player p = game.player;

        // --- PARTIE GAUCHE : STATS & EXP ---
        Table leftTable = new Table(game.skin);
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

        float progress = (p.getExpForNextLevel() > 0) ? (float) p.getExp() / p.getExpForNextLevel() : 0;
        ProgressBar.ProgressBarStyle progressStyle = new ProgressBar.ProgressBarStyle(game.skin.get("default-horizontal", ProgressBar.ProgressBarStyle.class));

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

        equipmentTable.add(new Label("Arme:", game.skin)).left();
        equipmentTable.add(createEquipmentSlot(p.getEquippedWeapon())).width(200).row();

        equipmentTable.add(new Label("Armure:", game.skin)).left();
        equipmentTable.add(createEquipmentSlot(p.getEquippedArmor())).width(200).row();

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

    /** Utilitaire pour ajouter une ligne de statistiques au tableau. */
    private void addStatRow(Table t, String label1, String val1, String label2, String val2) {
        t.add(new Label(label1 + ": " + val1, game.skin)).left().padRight(10);
        t.add(new Label(label2 + ": " + val2, game.skin)).left().row();
    }

    /** Crée un bouton pour un slot d'équipement (permet de déséquiper au clic). */
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

    /** Crée une texture unie de couleur donnée (pour la barre de progression). */
    private TextureRegionDrawable getColoredDrawable(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

    @Override public void update(float delta) {}

    /**
     * Dessine le menu.
     * Affiche l'état d'exploration en fond (figé) puis le Stage du menu par-dessus.
     */
    @Override public void draw(SpriteBatch batch) {
        // 1. Dessiner le jeu en arrière-plan
        game.explorationState.draw(batch);

        // 2. Dessiner l'UI par dessus
        game.menuStage.act();
        game.menuStage.draw();
    }

    /** Permet de quitter le menu avec la touche Echap. */
    @Override public void handleInput() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) game.changeState(game.explorationState);
    }
}
