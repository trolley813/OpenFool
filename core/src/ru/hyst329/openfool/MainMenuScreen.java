package ru.hyst329.openfool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTextButton;


/**
 * Created by hyst329 on 13.03.2017.
 * Licensed under MIT License.
 */

class MainMenuScreen implements Screen {
    private final OpenFoolGame game;
    private final Stage stage;
    private Sprite king, queen, jack;
    private boolean canStart;
    private final VisTextButton newGameButton, settingsButton, quitButton;
    private final VisProgressBar progressBar;

    MainMenuScreen(OpenFoolGame game) {
        this.game = game;
        // Initialise the stage
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);


        newGameButton = new VisTextButton(game.localeBundle.get("NewGame"));
        newGameButton.setBounds(40, 300, 250, 80);
        newGameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // super.clicked(event, x, y);
                if(canStart)
                    newGame();
            }
        });
        stage.addActor(newGameButton);
        settingsButton = new VisTextButton(game.localeBundle.get("Settings"));
        settingsButton.setBounds(40, 200, 250, 80);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // super.clicked(event, x, y);
                showSettings();
            }
        });
        stage.addActor(settingsButton);
        quitButton = new VisTextButton(game.localeBundle.get("Quit"));
        quitButton.setBounds(40, 100, 250, 80);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // super.clicked(event, x, y);
                quit();
            }
        });
        stage.addActor(quitButton);
        progressBar = new VisProgressBar(0, 1, 1e-3f, false);
        progressBar.setBounds(40, 40, 720, 60);
        progressBar.setAnimateDuration(0.2f);
        stage.addActor(progressBar);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.8f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
        game.batch.begin();
        if (game.assetManager.update()) {
            canStart = true;
            if (king == null) {
                king = new Sprite(game.assetManager.get("decks/rus/13h.png", Texture.class));
                king.setScale(0.4f);
                king.setCenter(520, 240);
                king.setRotation(20);
            }
            if (queen == null) {
                queen = new Sprite(game.assetManager.get("decks/rus/12c.png", Texture.class));
                queen.setScale(0.4f);
                queen.setCenter(600, 270);
                queen.setRotation(0);
            }
            if (jack == null) {
                jack = new Sprite(game.assetManager.get("decks/rus/11d.png", Texture.class));
                jack.setScale(0.4f);
                jack.setCenter(680, 240);
                jack.setRotation(-20);
            }
            king.draw(game.batch);
            queen.draw(game.batch);
            jack.draw(game.batch);
            game.font.draw(game.batch, "OpenFool", 520, 80);
        } else {
            float progress = game.assetManager.getProgress();
            game.font.draw(game.batch, game.localeBundle.format("LoadingAssets",
                    Math.round(progress * 100)), 320, 100);
            progressBar.setValue(progress);
        }
        game.batch.end();
        newGameButton.setVisible(canStart);
        settingsButton.setVisible(canStart);
        quitButton.setVisible(canStart);
        progressBar.setVisible(!canStart);

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

    }

    private void newGame() {
        game.setScreen(new GameScreen(game));
        dispose();
    }

    private void showSettings() {
        game.setScreen(new SettingsScreen(game));
        dispose();
    }

    private void quit() {
        dispose();
        Gdx.app.exit();
    }
}
