package com.github.LoiseauMael.RPG.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Interface définissant le contrat que doivent respecter tous les états du jeu.
 * <p>
 * Le jeu fonctionne comme une machine à états finis. Un seul état est actif à la fois
 * (géré par {@link com.github.LoiseauMael.RPG.Main}).
 */
public interface IGameState {

    /**
     * Appelé une seule fois lors de la transition vers cet état.
     * Sert à initialiser les variables, lancer les musiques ou prendre le focus des entrées (InputProcessor).
     */
    void enter();

    /**
     * Boucle logique principale de l'état.
     * @param delta Temps écoulé depuis la dernière frame (en secondes).
     */
    void update(float delta);

    /**
     * Boucle de rendu graphique.
     * @param batch Le SpriteBatch utilisé pour dessiner les textures.
     */
    void draw(SpriteBatch batch);

    /**
     * Appelé juste avant de quitter cet état pour passer à un autre.
     * Sert à nettoyer la mémoire, cacher l'UI ou sauvegarder des données temporaires.
     */
    void exit();

    /**
     * Méthode dédiée à la gestion des touches clavier (Polling).
     * Souvent appelée à l'intérieur de {@code update()}.
     */
    void handleInput();
}
