package ru.hyst329.openfool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by main on 18.03.2017.
 * Licensed under MIT License.
 */
class SettingsScreen implements Screen {
    static final String BACKGROUND_COLOR = "BackgroundColor";
    static final String DECK = "Deck";
    private static final String LANGUAGE = "Language";
    static final String SORTING_MODE = "SortingMode";
    private final Map<String, String> DECKS, LANGUAGES;
    private static final float CARD_SCALE = 0.25f;

    private final OpenFoolGame game;
    private final Stage stage;
    private Color backgroundColor;
    private String deck, language;
    private Player.SortingMode sortingMode;
    private final ColorPicker picker;
    private final VisSelectBox<String> deckSelectBox, languageSelectBox, sortingSelectBox;
    private Sprite back, ace, queen, ten, deuce;


    SettingsScreen(OpenFoolGame game) {
        this.game = game;
        // Initialise DECKS
        DECKS = new HashMap<>();
        DECKS.put(game.localeBundle.get("CardsRussian"), "rus");
        DECKS.put(game.localeBundle.get("CardsInternational"), "int");
        DECKS.put(game.localeBundle.get("CardsFrench"), "fra");
        // Initialise LANGUAGES
        LANGUAGES = new HashMap<>();
        LANGUAGES.put(game.localeBundle.get("LanguageRussian"), "ru");
        LANGUAGES.put(game.localeBundle.get("LanguageEnglish"), "en");
        // Initialise SORTING_MODES
        ArrayList<String> SORTING_MODES = new ArrayList<>();
        SORTING_MODES.add(game.localeBundle.get("SortingUnsorted"));
        SORTING_MODES.add(game.localeBundle.get("SortingSuitAscending"));
        SORTING_MODES.add(game.localeBundle.get("SortingSuitDescending"));
        SORTING_MODES.add(game.localeBundle.get("SortingRankAscending"));
        SORTING_MODES.add(game.localeBundle.get("SortingRankDescending"));
        // Initialise the stage
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        backgroundColor = new Color(game.preferences.getInteger(BACKGROUND_COLOR, 0x33cc4dff));
        deck = game.preferences.getString(DECK, "rus");
        language = game.preferences.getString(LANGUAGE, "ru");
        sortingMode = Player.SortingMode.fromInt(game.preferences.getInteger(SORTING_MODE, 0));

        picker = new ColorPicker("Choose background color", new ColorPickerAdapter() {
            @Override
            public void finished(Color newColor) {
                backgroundColor = newColor;
            }

        });

        VisTextButton changeColorButton = new VisTextButton(game.localeBundle.get("ChangeBackgroundColor"));
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
        VisTextButton saveButton = new VisTextButton(game.localeBundle.get("SaveSettings"));
        saveButton.setBounds(40, 200, 250, 80);
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // super.clicked(event, x, y);
                saveAndQuit();

            }
        });
        stage.addActor(saveButton);
        VisLabel deckSelectLabel = new VisLabel(game.localeBundle.get("Cards"));
        deckSelectLabel.setBounds(420, 300, 60, 40);
        stage.addActor(deckSelectLabel);
        VisLabel languageSelectLabel = new VisLabel(game.localeBundle.get("Language"));
        languageSelectLabel.setBounds(420, 350, 60, 40);
        stage.addActor(languageSelectLabel);
        VisLabel sortingSelectLabel = new VisLabel(game.localeBundle.get("Sorting"));
        sortingSelectLabel.setBounds(420, 400, 60, 40);
        stage.addActor(sortingSelectLabel);
        deckSelectBox = new VisSelectBox<>();
        deckSelectBox.setBounds(580, 300, 180, 40);
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
        languageSelectBox = new VisSelectBox<>();
        languageSelectBox.setBounds(580, 350, 180, 40);
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
        sortingSelectBox = new VisSelectBox<>();
        sortingSelectBox.setBounds(580, 400, 180, 40);
        sortingSelectBox.setItems(SORTING_MODES.toArray(new String[0]));
        sortingSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            sortingMode = Player.SortingMode.fromInt(sortingSelectBox.getSelectedIndex());
            }


        });
        sortingSelectBox.setSelectedIndex(sortingMode.getValue());
        stage.addActor(sortingSelectBox);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveAndQuit();
        }
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
        game.preferences.putInteger(SORTING_MODE, sortingMode.getValue());
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
