package com.github.LoiseauMael.RPG;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {

    SpriteBatch batch;
    Player player;


    @Override
    public void create() {
        batch = new SpriteBatch();
        // Le constructeur Player nécessite velocityX et velocityY
        player = new Player(50, 50, 0, 0, 100, 50, 5, 10, 5, 10, 5, 1, 3, new Sprite(new Texture(Gdx.files.internal("SwordmanIdle.png"))));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        player.handleInput();
        player.update(delta);

        batch.begin();
        // player.draw(batch) utilise le sprite hérité
        player.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (player != null) {
            player.dispose();
        }
    }
}
