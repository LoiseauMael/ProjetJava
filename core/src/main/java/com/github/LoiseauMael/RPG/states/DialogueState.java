package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.npcs.HealerNPC;
import com.github.LoiseauMael.RPG.npcs.MerchantNPC;
import com.github.LoiseauMael.RPG.npcs.NPC;

public class DialogueState implements IGameState {
    private Main game;
    private NPC currentNPC;

    public DialogueState(Main game) {
        this.game = game;
    }

    public void setNPC(NPC npc) {
        this.currentNPC = npc;
    }

    @Override
    public void enter() {
        if (currentNPC != null) {
            game.dialogTable.setVisible(true);
            game.dialogNameLabel.setText(currentNPC.getName());
            game.dialogTextLabel.setText(currentNPC.getCurrentDialogue());
        }
    }

    @Override
    public void handleInput() {
        // Appuyer sur F ou Entrée pour passer à la suite
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (currentNPC != null) {
                if (currentNPC.advanceDialogue()) {
                    // Le dialogue continue
                    game.dialogTextLabel.setText(currentNPC.getCurrentDialogue());
                } else {
                    // --- FIN DU DIALOGUE : ACTIONS SPÉCIFIQUES ---

                    if (currentNPC instanceof HealerNPC) {
                        // 1. Si c'est un Guérisseur : Soin complet (PV + PM + PA)
                        if (game.player != null) {
                            game.player.setPV(game.player.getMaxPV()); // PV max
                            game.player.setPM(game.player.getMaxPM()); // PM max

                            // --- AJOUT ICI ---
                            game.player.setPA(game.player.getMaxPA()); // PA max
                            // -----------------

                            System.out.println("Le joueur a été soigné (PV, PM, PA) !");
                        }
                        game.changeState(game.explorationState);
                    }
                    else if (currentNPC instanceof MerchantNPC) {
                        // 2. Si c'est un Marchand : Ouvrir le magasin
                        game.changeState(game.shopState);
                    }
                    else {
                        // 3. NPC normal : Retourner simplement à l'exploration
                        game.changeState(game.explorationState);
                    }
                }
            }
        }
    }

    @Override
    public void update(float delta) {}

    @Override
    public void draw(SpriteBatch batch) {
        game.explorationState.draw(batch); // Fond
        game.uiStage.act();
        game.uiStage.draw(); // UI dialogue
    }

    @Override
    public void exit() {
        game.dialogTable.setVisible(false);
    }
}
