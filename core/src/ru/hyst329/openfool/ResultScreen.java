package ru.hyst329.openfool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

/**
 * Created by main on 18.03.2017.
 * Licensed under MIT License.
 */
class ResultScreen implements Screen {
    private final OpenFoolGame game;
    private final Result result;

    enum Result {
        WON,
        LOST,
        PARTNER_LOST,
        DRAW
    }

    ResultScreen(OpenFoolGame game, Result result) {
        this.game = game;
        this.result = result;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Color color = Color.BLACK;
        String header = "", text = "";
        switch (result) {
            case WON:
                color = new Color(0.2f, 0.6f, 0.125f, 1);
                header = game.localeBundle.get("VictoryHeader");
                text = game.localeBundle.get("VictoryText");
                break;
            case LOST:
                color = new Color(0.6f, 0.2f, 0.125f, 1);
                header = game.localeBundle.get("DefeatHeader");
                text = game.localeBundle.get("DefeatText");
                break;
            case PARTNER_LOST:
                color = new Color(0.6f, 0.4f, 0.125f, 1);
                header = game.localeBundle.get("PartnerDefeatHeader");
                text = game.localeBundle.get("PartnerDefeatText");
                break;
            case DRAW:
                color = new Color(0.6f, 0.6f, 0.125f, 1);
                header = game.localeBundle.get("DrawHeader");
                text = game.localeBundle.get("DrawText");
                break;
        }
        GlyphLayout headerLayout = new GlyphLayout(game.font, header);
        GlyphLayout textLayout = new GlyphLayout(game.font, text);
        Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.font.draw(game.batch, headerLayout,
                400 - headerLayout.width / 2,
                400 - headerLayout.height / 2);
        game.font.draw(game.batch, textLayout,
                400 - textLayout.width / 2,
                280 - textLayout.height / 2);
        game.batch.end();
        if (Gdx.input.isTouched()) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {

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

}
