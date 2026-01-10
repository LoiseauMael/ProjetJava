package com.github.LoiseauMael.RPG.states;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.LoiseauMael.RPG.Main;

public class ClassSelectionState implements IGameState {
    private Main game;
    public ClassSelectionState(Main game) { this.game = game; }
    @Override public void enter() { Gdx.input.setInputProcessor(game.classStage); }
    @Override public void update(float delta) { game.classStage.act(delta); }
    @Override public void draw(SpriteBatch batch) { game.classStage.draw(); }
    @Override public void handleInput() {}
    @Override public void exit() {}
}
