package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IGameState {
    void enter();            // Initialisation quand on entre dans l'état
    void update(float delta); // Logique (Mouvements, calculs)
    void draw(SpriteBatch batch); // Rendu
    void exit();             // Nettoyage quand on quitte
    void handleInput();      // Gestion des entrées
}
