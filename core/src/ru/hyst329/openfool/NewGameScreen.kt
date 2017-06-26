package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.kotcrab.vis.ui.widget.VisCheckBox
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton

internal class NewGameScreen(private val game: OpenFoolGame) : Screen {
    private val stage: Stage = Stage(FitViewport(800f, 480f))
    private val singlePlayerButton: VisTextButton
    private val gameplayLabel: VisLabel
    private val deuceCheckBox: VisCheckBox
    private val limitCheckBox: VisCheckBox
    private val ruleSet: RuleSet = RuleSet(game.preferences)

    init {
        Gdx.input.inputProcessor = stage
        singlePlayerButton = VisTextButton(game.localeBundle.get("SinglePlayer"))
        singlePlayerButton.setBounds(40f, 300f, 250f, 80f)
        singlePlayerButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                startGame()
            }
        })
        stage.addActor(singlePlayerButton)
        gameplayLabel = VisLabel(game.localeBundle.get("GameplaySettings"))
        gameplayLabel.setBounds(440f, 350f, 260f, 40f)
        stage.addActor(gameplayLabel)
        deuceCheckBox = VisCheckBox(game.localeBundle.get("DeuceBeatsAce"))
        deuceCheckBox.setBounds(440f, 300f, 260f, 40f)
        deuceCheckBox.isChecked = ruleSet.deuceBeatsAce
        stage.addActor(deuceCheckBox)
        limitCheckBox = VisCheckBox(game.localeBundle.get("LimitTo5Cards"))
        limitCheckBox.setBounds(440f, 250f, 260f, 40f)
        stage.addActor(limitCheckBox)
        deuceCheckBox.isChecked = ruleSet.loweredFirstDiscardLimit
    }


    override fun hide() {
    }

    override fun show() {
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.2f, 0.8f, 0.3f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {

    }

    private fun startGame() {
        game.screen = GameScreen(game)
        dispose()
    }
}