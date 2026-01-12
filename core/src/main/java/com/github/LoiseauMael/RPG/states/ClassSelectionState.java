package com.github.LoiseauMael.RPG.states;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.LoiseauMael.RPG.Main;

/**
 * État de transition permettant au joueur de choisir sa classe (Épéiste, Magicien, etc.)
 * au début d'une nouvelle aventure.
 * <p>
 * Cet état délègue principalement l'affichage et la logique d'interaction
 * au {@code Stage} défini dans la classe principale {@link Main} (game.classStage).
 */
public class ClassSelectionState implements IGameState {
    private Main game;

    public ClassSelectionState(Main game) { this.game = game; }

    /** Active le processeur d'entrée pour la sélection de classe. */
    @Override public void enter() { Gdx.input.setInputProcessor(game.classStage); }

    /** Met à jour les animations de l'interface. */
    @Override public void update(float delta) { game.classStage.act(delta); }

    /** Dessine l'interface de sélection. */
    @Override public void draw(SpriteBatch batch) { game.classStage.draw(); }

    @Override public void handleInput() {}

    @Override public void exit() {}
}
