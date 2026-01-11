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
import com.github.LoiseauMael.RPG.items.Relic;
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

        // Titre
        Label title = new Label("--- ECHOPPE ---", game.skin);
        title.setFontScale(1.5f);
        title.setColor(Color.GOLD);
        game.shopTable.add(title).colspan(3).padBottom(20).row();

        // Argent
        Label moneyLabel = new Label("Bourse : " + game.player.getMoney() + " Or", game.skin);
        moneyLabel.setFontScale(1.3f);
        moneyLabel.setColor(Color.YELLOW);
        game.shopTable.add(moneyLabel).colspan(3).padBottom(15).row();

        // Liste
        Table itemsTable = new Table();
        itemsTable.top();

        if (game.merchantInventory != null) {
            for (final ShopEntry entry : game.merchantInventory) {
                String itemName = entry.item.getName();
                String infoText = "";

                // --- AFFICHAGE DES STATS ---
                if (entry.item instanceof Weapon) {
                    Weapon w = (Weapon) entry.item;
                    if (w.bonusFOR > 0) infoText += " [ATK+" + w.bonusFOR + "]";
                    if (w.bonusFORM > 0) infoText += " [MAG+" + w.bonusFORM + "]";
                } else if (entry.item instanceof Armor) {
                    Armor a = (Armor) entry.item;
                    if (a.bonusDEF > 0) infoText += " [DEF+" + a.bonusDEF + "]";
                    if (a.bonusDEFM > 0) infoText += " [M.DEF+" + a.bonusDEFM + "]";
                } else if (entry.item instanceof Relic) {
                    Relic r = (Relic) entry.item;
                    if (r.damageMultiplier > 1.0f) infoText += " [DMG+" + (int)((r.damageMultiplier-1)*100) + "%]";
                    if (r.defenseMultiplier > 1.0f) infoText += " [DEF+" + (int)((r.defenseMultiplier-1)*100) + "%]";
                } else {
                    infoText = " (" + entry.item.getDescription() + ")";
                }

                Label nameLabel = new Label(itemName + infoText, game.skin);
                nameLabel.setFontScale(1.2f);
                if (entry.item instanceof Equipment) nameLabel.setColor(Color.CYAN);

                itemsTable.add(nameLabel).width(500).left().padBottom(10);
                itemsTable.add(new Label(entry.price + " Or", game.skin)).width(80).center().padBottom(10);

                TextButton buyBtn = new TextButton("Acheter", game.skin);

                // Vérification si équipable
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
            }
        }

        ScrollPane scroll = new ScrollPane(itemsTable, game.skin);
        game.shopTable.add(scroll).width(750).height(400).colspan(3).row();

        TextButton closeBtn = new TextButton("Fermer", game.skin);
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.changeState(game.explorationState);
            }
        });
        game.shopTable.add(closeBtn).width(200).height(50).padTop(10).colspan(3);
    }

    @Override public void update(float delta) { game.shopStage.act(delta); }
    @Override public void draw(SpriteBatch batch) { game.explorationState.draw(batch); game.shopStage.draw(); }
    @Override public void handleInput() { if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) game.changeState(game.explorationState); }
    @Override public void exit() { game.shopTable.clear(); }
}
