package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.Main.ShopEntry;
import com.github.LoiseauMael.RPG.items.Armor;
import com.github.LoiseauMael.RPG.items.Equipment;
import com.github.LoiseauMael.RPG.items.Weapon;

public class ShopState implements IGameState {
    private Main game;

    public ShopState(Main game) {
        this.game = game;
    }

    @Override
    public void enter() {
        Gdx.input.setInputProcessor(game.shopStage);
        rebuildShopUI();
    }

    private void rebuildShopUI() {
        game.shopTable.clear();
        game.shopTable.defaults().pad(5);

        // --- EN-TÊTE ---
        Label title = new Label("--- ECHOPPE DU VILLAGE ---", game.skin);
        title.setFontScale(1.5f); // Titre un peu plus gros (+50%)
        title.setColor(Color.GOLD);
        game.shopTable.add(title).colspan(3).padBottom(20).row();

        // --- ARGENT DU JOUEUR ---
        Label moneyLabel = new Label("Votre Bourse : " + game.player.getMoney() + " Or", game.skin);
        moneyLabel.setFontScale(1.3f); // Texte agrandi (+30%)
        moneyLabel.setColor(Color.YELLOW);
        game.shopTable.add(moneyLabel).colspan(3).padBottom(15).row();

        // --- LISTE DES ARTICLES (SCROLLABLE) ---
        Table itemsTable = new Table();
        itemsTable.top();

        if (game.merchantInventory != null) {
            for (final ShopEntry entry : game.merchantInventory) {

                // 1. Préparation des infos : Nom + Stats uniquement (Pas de description)
                String itemName = entry.item.getName();
                String statsText = "";

                if (entry.item instanceof Weapon) {
                    Weapon w = (Weapon) entry.item;
                    if (w.bonusFOR > 0) statsText += " [FOR+" + w.bonusFOR + "]";
                    if (w.bonusFORM > 0) statsText += " [MAG+" + w.bonusFORM + "]";
                } else if (entry.item instanceof Armor) {
                    Armor a = (Armor) entry.item;
                    if (a.bonusDEF > 0) statsText += " [DEF+" + a.bonusDEF + "]";
                    if (a.bonusDEFM > 0) statsText += " [M.DEF+" + a.bonusDEFM + "]";
                } else {
                    // Pour les potions, on peut mettre une stat courte si besoin,
                    // ou laisser juste le nom si la description est supprimée.
                    // Ici on laisse vide ou on met un effet simple si vous avez des getters sur les potions.
                }

                // --- MODIFICATION : TEXTE AGRANDI ET SANS DESCRIPTION ---

                // Label Nom + Stats concaténés
                Label nameLabel = new Label(itemName + " " + statsText, game.skin);
                nameLabel.setFontScale(1.3f); // Agrandissement du texte (+30%)

                // Couleur différente pour l'équipement
                if (entry.item instanceof Equipment) nameLabel.setColor(Color.CYAN);

                // On augmente la largeur de la cellule car le texte est plus gros
                itemsTable.add(nameLabel).width(450).left().padBottom(10);

                // Label Prix
                Label priceLabel = new Label(entry.price + " Or", game.skin);
                priceLabel.setFontScale(1.3f); // Agrandissement du prix
                itemsTable.add(priceLabel).width(100).center().padBottom(10);

                // 2. Bouton Acheter
                TextButton buyBtn = new TextButton("Acheter", game.skin);
                // On agrandit aussi un peu le texte du bouton
                buyBtn.getLabel().setFontScale(1.1f);

                // Vérifie la classe
                boolean canEquip = true;
                if (entry.item instanceof Equipment) {
                    canEquip = ((Equipment)entry.item).canEquip(game.player);
                }

                if (!canEquip) {
                    buyBtn.setText("Classe!");
                    buyBtn.setDisabled(true);
                    buyBtn.setColor(Color.GRAY);
                } else if (game.player.getMoney() < entry.price) {
                    buyBtn.setColor(Color.FIREBRICK);
                } else {
                    buyBtn.setColor(Color.WHITE);
                }

                buyBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (buyBtn.isDisabled()) return;

                        if (game.player.getMoney() >= entry.price) {
                            game.player.addMoney(-entry.price);
                            game.player.addItem(entry.item);
                            rebuildShopUI();
                        }
                    }
                });
                itemsTable.add(buyBtn).width(100).padBottom(10).row();

                // Ligne de séparation fine
                Image sep = new Image(game.skin.newDrawable("white", Color.DARK_GRAY));
                itemsTable.add(sep).height(1).colspan(3).fillX().padBottom(5).row();
            }
        }

        // Ajout du tableau d'items dans un ScrollPane
        ScrollPane scroll = new ScrollPane(itemsTable, game.skin);
        scroll.setFadeScrollBars(false);
        game.shopTable.add(scroll).width(700).height(400).colspan(3).row();

        // --- BOUTON QUITTER ---
        TextButton closeBtn = new TextButton("Quitter", game.skin);
        closeBtn.getLabel().setFontScale(1.2f);
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.changeState(game.explorationState);
            }
        });
        game.shopTable.add(closeBtn).width(200).height(50).padTop(15).colspan(3);
    }

    @Override
    public void update(float delta) {
        game.shopStage.act(delta);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Dessine le jeu en arrière-plan
        game.explorationState.draw(batch);
        game.shopStage.draw();
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.changeState(game.explorationState);
        }
    }

    @Override
    public void exit() {
        game.shopTable.clear();
    }
}
