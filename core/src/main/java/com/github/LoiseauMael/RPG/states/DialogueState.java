package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.LoiseauMael.RPG.Main;
import com.github.LoiseauMael.RPG.npcs.HealerNPC;
import com.github.LoiseauMael.RPG.npcs.MerchantNPC;
import com.github.LoiseauMael.RPG.npcs.NPC;

/**
 * État gérant l'affichage et l'interaction des dialogues avec les PNJs.
 * <p>
 * Cet état met le jeu en "pause visuelle" (le monde est figé en arrière-plan) et affiche
 * l'interface utilisateur de dialogue. Il gère également les événements post-dialogue,
 * comme le soin par un guérisseur ou l'ouverture d'une boutique.
 */
public class DialogueState implements IGameState {
    private Main game;
    private NPC currentNPC;

    /**
     * Constructeur de l'état de dialogue.
     * @param game Instance principale du jeu pour l'accès aux ressources globales.
     */
    public DialogueState(Main game) {
        this.game = game;
    }

    /**
     * Définit le PNJ avec lequel le joueur est en train de discuter.
     * @param npc Le PNJ cible.
     */
    public void setNPC(NPC npc) {
        this.currentNPC = npc;
    }

    /**
     * Initialise l'affichage du dialogue.
     * Rend la fenêtre de dialogue visible et charge le nom et le premier texte du PNJ.
     */
    @Override
    public void enter() {
        if (currentNPC != null) {
            game.dialogTable.setVisible(true);
            game.dialogNameLabel.setText(currentNPC.getName());
            game.dialogTextLabel.setText(currentNPC.getCurrentDialogue());
        }
    }

    /**
     * Gère les entrées du joueur pour avancer dans le dialogue.
     * <p>
     * Touches :
     * <ul>
     * <li><b>F</b> ou <b>Entrée</b> : Passe au texte suivant ou termine le dialogue.</li>
     * </ul>
     * À la fin du dialogue, déclenche des actions spécifiques selon le type de PNJ (Soin, Boutique, etc.).
     */
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
                            game.player.setPA(game.player.getMaxPA()); // PA max
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

    /**
     * Affiche l'écran de dialogue.
     * Dessine d'abord l'état d'exploration en arrière-plan, puis l'interface utilisateur (UI) par-dessus.
     * @param batch Le SpriteBatch utilisé pour le rendu.
     */
    @Override
    public void draw(SpriteBatch batch) {
        game.explorationState.draw(batch); // Fond
        game.uiStage.act();
        game.uiStage.draw(); // UI dialogue
    }

    /**
     * Nettoie l'état à la sortie.
     * Cache la fenêtre de dialogue.
     */
    @Override
    public void exit() {
        game.dialogTable.setVisible(false);
    }
}
