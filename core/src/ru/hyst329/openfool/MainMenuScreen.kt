package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.kotcrab.vis.ui.widget.VisProgressBar
import com.kotcrab.vis.ui.widget.VisTextButton


/**
 * Created by hyst329 on 13.03.2017.
 * Licensed under MIT License.
 */

internal class MainMenuScreen(private val game: OpenFoolGame) : Screen {
    private val stage: Stage
    private var king: Sprite? = null
    private var queen: Sprite? = null
    private var jack: Sprite? = null
    private var canStart: Boolean = false
    private val newGameButton: VisTextButton
    private val settingsButton: VisTextButton
    private val quitButton: VisTextButton
    private val progressBar: VisProgressBar

    init {
        // Initialise the stage
        stage = Stage(FitViewport(800f, 480f))
        Gdx.input.inputProcessor = stage


        newGameButton = VisTextButton(game.localeBundle.get("NewGame"))
        newGameButton.setBounds(40f, 300f, 250f, 80f)
        newGameButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                if (canStart)
                    newGame()
            }
        })
        stage.addActor(newGameButton)
        settingsButton = VisTextButton(game.localeBundle.get("Settings"))
        settingsButton.setBounds(40f, 200f, 250f, 80f)
        settingsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                showSettings()
            }
        })
        stage.addActor(settingsButton)
        quitButton = VisTextButton(game.localeBundle.get("Quit"))
        quitButton.setBounds(40f, 100f, 250f, 80f)
        quitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                quit()
            }
        })
        stage.addActor(quitButton)
        progressBar = VisProgressBar(0f, 1f, 1e-3f, false)
        progressBar.setBounds(40f, 40f, 720f, 60f)
        progressBar.setAnimateDuration(0.2f)
        stage.addActor(progressBar)
    }

    override fun show() {

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.2f, 0.8f, 0.3f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
        game.batch.begin()
        if (game.assetManager.update()) {
            canStart = true
            if (king == null) {
                king = Sprite(game.assetManager.get("decks/rus/13h.png", Texture::class.java))
                king!!.setScale(0.4f)
                king!!.setCenter(520f, 240f)
                king!!.rotation = 20f
            }
            if (queen == null) {
                queen = Sprite(game.assetManager.get("decks/rus/12c.png", Texture::class.java))
                queen!!.setScale(0.4f)
                queen!!.setCenter(600f, 270f)
                queen!!.rotation = 0f
            }
            if (jack == null) {
                jack = Sprite(game.assetManager.get("decks/rus/11d.png", Texture::class.java))
                jack!!.setScale(0.4f)
                jack!!.setCenter(680f, 240f)
                jack!!.rotation = -20f
            }
            king!!.draw(game.batch)
            queen!!.draw(game.batch)
            jack!!.draw(game.batch)
            game.font.draw(game.batch, "OpenFool", 520f, 80f)
        } else {
            val progress = game.assetManager.progress
            game.font.draw(game.batch, game.localeBundle.format("LoadingAssets",
                    Math.round(progress * 100)), 320f, 100f)
            progressBar.value = progress
        }
        game.batch.end()
        newGameButton.isVisible = canStart
        settingsButton.isVisible = canStart
        quitButton.isVisible = canStart
        progressBar.isVisible = !canStart
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit()
        }

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {

    }

    private fun newGame() {
        game.screen = GameScreen(game)
        dispose()
    }

    private fun showSettings() {
        game.screen = SettingsScreen(game)
        dispose()
    }

    private fun quit() {
        dispose()
        Gdx.app.exit()
    }
}
