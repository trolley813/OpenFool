package ru.hyst329.openfool;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Locale;

public class OpenFoolGame extends ApplicationAdapter {
    SpriteBatch batch;
    AssetManager assetManager;
    BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        font = new BitmapFont();
        font.setColor(Color.CHARTREUSE);
        String[] decks = {"fra", "int", "rus"};
        String suits = "cdhs";
        for (String d : decks) {
            for (int i = 1; i <= 13; i++) {
                for (char s : suits.toCharArray()) {
                    assetManager.load(String.format(Locale.ENGLISH, "%s/%d%s.png", d, i, s),
                            Texture.class);
                }
            }
        }

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        if (assetManager.update()) {
            batch.draw(assetManager.get("rus/12d.png", Texture.class), 0, 0);
        } else {
            font.draw(batch, String.format("Loading %s%%...",
                    assetManager.getProgress() * 100), 20, 20);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
