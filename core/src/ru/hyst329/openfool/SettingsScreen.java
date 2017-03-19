package ru.hyst329.openfool;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.I18NBundleLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by main on 18.03.2017.
 */
public class SettingsScreen implements Screen {
    public static final String BACKGROUND_COLOR = "BackgroundColor";
    public static final String DECK = "Deck";
    public static final String LANGUAGE = "Language";
    private final Map<String, String> DECKS, LANGUAGES;
    private static final float CARD_SCALE = 0.25f;

    private OpenFoolGame game;
    private final Stage stage;
    private Color backgroundColor;
    private String deck, language;
    private ColorPicker picker;
    private VisTextButton changeColorButton, saveButton;
    private VisSelectBox<String> deckSelectBox, languageSelectBox;
    private VisLabel deckSelectLabel, languageSelectLabel;
    private Sprite back, ace, queen, ten, deuce;


    public SettingsScreen(OpenFoolGame game) {
        this.game = game;
        // Initialise DECKS
        DECKS = new HashMap<String, String>();
        DECKS.put(game.localeBundle.get("CardsRussian"), "rus");
        DECKS.put(game.localeBundle.get("CardsInternational"), "int");
        DECKS.put(game.localeBundle.get("CardsFrench"), "fra");
        // Initialise LANGUAGES
        LANGUAGES = new HashMap<String, String>();
        LANGUAGES.put(game.localeBundle.get("LanguageRussian"), "ru");
        LANGUAGES.put(game.localeBundle.get("LanguageEnglish"), "en");
        // Initialise the stage
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        backgroundColor = new Color(game.preferences.getInteger(BACKGROUND_COLOR, 0x33cc4dff));
        deck = game.preferences.getString(DECK, "rus");
        language = game.preferences.getString(LANGUAGE, "ru");

        picker = new ColorPicker("Choose background color", new ColorPickerAdapter() {
            @Override
            public void finished(Color newColor) {
                backgroundColor = newColor;
            }

        });

        changeColorButton = new VisTextButton(game.localeBundle.get("ChangeBackgroundColor"));
        changeColorButton.setBounds(40, 300, 250, 80);
        changeColorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // super.clicked(event, x, y);
                picker.setColor(backgroundColor);
                stage.addActor(picker.fadeIn());
            }
        });
        stage.addActor(changeColorButton);
        saveButton = new VisTextButton(game.localeBundle.get("SaveSettings"));
        saveButton.setBounds(40, 200, 250, 80);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // super.clicked(event, x, y);
                saveAndQuit();

            }
        });
        stage.addActor(saveButton);
        deckSelectLabel = new VisLabel(game.localeBundle.get("Cards"));
        deckSelectLabel.setBounds(520, 400, 60, 40);
        stage.addActor(deckSelectLabel);
        languageSelectLabel = new VisLabel(game.localeBundle.get("Language"));
        languageSelectLabel.setBounds(520, 350, 60, 40);
        stage.addActor(languageSelectLabel);

        deckSelectBox = new VisSelectBox<String>();
        deckSelectBox.setBounds(600, 400, 120, 40);
        deckSelectBox.setItems(DECKS.keySet().toArray(new String[0]));
        deckSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                deck = DECKS.get(deckSelectBox.getSelected());
                updateSprites();
            }


        });
        String deckName = "";
        for (Map.Entry<String, String> entry : DECKS.entrySet()) {
            if (entry.getValue().equals(deck)) {
                deckName = entry.getKey();
            }
        }
        deckSelectBox.setSelected(deckName);
        stage.addActor(deckSelectBox);
        languageSelectBox = new VisSelectBox<String>();
        languageSelectBox.setBounds(600, 350, 120, 40);
        languageSelectBox.setItems(LANGUAGES.keySet().toArray(new String[0]));
        languageSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                language = LANGUAGES.get(languageSelectBox.getSelected());
                reloadLocale();
            }


        });
        String languageName = "";
        for (Map.Entry<String, String> entry : LANGUAGES.entrySet()) {
            if (entry.getValue().equals(language)) {
                languageName = entry.getKey();
            }
        }
        languageSelectBox.setSelected(languageName);
        stage.addActor(languageSelectBox);
        updateSprites();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        game.batch.begin();
        if (!stage.getActors().contains(picker, true)) {
            back.draw(game.batch);
            ace.draw(game.batch);
            queen.draw(game.batch);
            ten.draw(game.batch);
            deuce.draw(game.batch);
        }
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        picker.dispose();
    }


    private void saveAndQuit() {
        game.preferences.putInteger(BACKGROUND_COLOR, Color.rgba8888(backgroundColor));
        game.preferences.putString(DECK, deck);
        game.preferences.putString(LANGUAGE, language);
        game.preferences.flush();
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    private void updateSprites() {
        back = new Sprite(game.assetManager.get(
                String.format("decks/%s/back.png", deck), Texture.class));
        ace = new Sprite(game.assetManager.get(
                String.format("decks/%s/1s.png", deck), Texture.class));
        queen = new Sprite(game.assetManager.get(
                String.format("decks/%s/12d.png", deck), Texture.class));
        ten = new Sprite(game.assetManager.get(
                String.format("decks/%s/10h.png", deck), Texture.class));
        deuce = new Sprite(game.assetManager.get(
                String.format("decks/%s/2c.png", deck), Texture.class));
        int index = 0;
        for (Sprite sprite : new Sprite[] {back, ace, queen, ten, deuce}) {
            sprite.setScale(CARD_SCALE);
            sprite.setCenter(360 + 90 * index++, 200);
        }
    }

    private void reloadLocale() {
        game.assetManager.unload("i18n/OpenFool");
        game.assetManager.load("i18n/OpenFool", I18NBundle.class,
                new I18NBundleLoader.I18NBundleParameter(new Locale(language)));
        game.assetManager.finishLoadingAsset("i18n/OpenFool");
        game.localeBundle = game.assetManager.get("i18n/OpenFool", I18NBundle.class);
    }
}
