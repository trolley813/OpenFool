package ru.hyst329.openfool;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;
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
    I18NBundle localeBundle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        VisUI.load();
        font = VisUI.getSkin().getFont("default-font");
        preferences = Gdx.app.getPreferences("OpenFool");
        // Deal with localisation
        String localeString = preferences.getString("Language", null);
        Locale locale = localeString == null ? Locale.getDefault() : new Locale(localeString);
        assetManager.load("i18n/OpenFool", I18NBundle.class,
                new I18NBundleLoader.I18NBundleParameter(locale));
        if (localeString == null) {
            localeString = locale.getLanguage();
            preferences.putString("Language", localeString);
            preferences.flush();
        }
        assetManager.finishLoadingAsset("i18n/OpenFool");
        localeBundle = assetManager.get("i18n/OpenFool", I18NBundle.class);
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
        for (int i = 0; i < 1; i++) {
            assetManager.load(String.format(Locale.ENGLISH, "backgrounds/background%d.png", i + 1),
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
