package ru.hyst329.openfool;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.kotcrab.vis.ui.VisUI;

import java.util.Locale;

/**
 * Created by hyst329 on 12.03.2017.
 * Licensed under MIT License.
 */

public class OpenFoolGame extends Game {
    SpriteBatch batch;
    AssetManager assetManager;
    BitmapFont font;
    Preferences preferences;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        VisUI.load();
        font = VisUI.getSkin().getFont("default-font");
        preferences = Gdx.app.getPreferences("OpenFool");
        TextureLoader.TextureParameter param;
        param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.MipMap;
        param.genMipMaps = true;
        String[] decks = {"fra", "int", "rus"};
        String suits = "cdhs";
        for (String d : decks) {
            for (int i = 1; i <= 13; i++) {
                for (char s : suits.toCharArray()) {
                    assetManager.load(String.format(Locale.ENGLISH, "decks/%s/%d%s.png", d, i, s),
                            Texture.class, param);
                }
            }
            assetManager.load(String.format(Locale.ENGLISH, "decks/%s/back.png", d),
                    Texture.class, param);
        }
        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        VisUI.dispose();
    }
}
