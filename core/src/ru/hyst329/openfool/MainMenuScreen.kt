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
    private val stage: Stage = Stage(FitViewport(800f, 480f))
    private var logo: Sprite = Sprite(Texture(Gdx.files.internal("logos/mm_logo.png")))
    private var hammerAndSickle: Sprite = Sprite(Texture(Gdx.files.internal("holidays/hammersickle.png")))
    private var santaHat: Sprite = Sprite(Texture(Gdx.files.internal("holidays/santahat.png")))
    private var canStart: Boolean = false
    private val newGameButton: VisTextButton
    private val settingsButton: VisTextButton
    private val quitButton: VisTextButton
    private val progressBar: VisProgressBar

    init {
        // Initialise the stage
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
        when (getCurrentHoliday()) {
            Holiday.OCTOBER_REVOLUTION -> Gdx.gl.glClearColor(0.8f, 0.0f, 0.0f, 1f)
            Holiday.NEW_YEAR ->  Gdx.gl.glClearColor(0.0f, 0.6f, 0.9f, 1f)
            null -> Gdx.gl.glClearColor(0.2f, 0.8f, 0.3f, 1f)
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
        game.batch.begin()
        if (game.assetManager.update()) {
            canStart = true
            logo.setCenter(560f, 250f)
            logo.draw(game.batch)
            when (getCurrentHoliday()) {
                Holiday.OCTOBER_REVOLUTION -> {
                    hammerAndSickle.setCenter(165f, 430f)
                    hammerAndSickle.setScale(0.35f)
                    hammerAndSickle.draw(game.batch)
                }
                Holiday.NEW_YEAR -> {
                    santaHat.setCenter(165f, 430f)
                    santaHat.setScale(0.3f)
                    santaHat.draw(game.batch)
                }
                null -> {

                }
            }

        } else {
            val progress = game.assetManager.progress
            game.font.draw(game.batch, game.localeBundle.format("LoadingAssets",
                    Math.round(progress * 100)), 280f, 110f)
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
        game.screen = NewGameScreen(game)
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
