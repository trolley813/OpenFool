package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import com.kotcrab.vis.ui.widget.VisCheckBox
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner

internal class NewGameScreen(private val game: OpenFoolGame) : Screen {
    private val stage: Stage = Stage(FitViewport(800f, 480f))
    private val singlePlayerButton: VisTextButton
    private val gameplayLabel: VisLabel
    private val deuceCheckBox: VisCheckBox
    private val limitCheckBox: VisCheckBox
    private val playerCountSpinner: Spinner
    private val teamCheckBox: VisCheckBox
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
        gameplayLabel.setBounds(390f, 350f, 320f, 40f)
        stage.addActor(gameplayLabel)
        deuceCheckBox = VisCheckBox(game.localeBundle.get("DeuceBeatsAce"))
        deuceCheckBox.setBounds(390f, 300f, 320f, 40f)
        deuceCheckBox.isChecked = ruleSet.deuceBeatsAce
        stage.addActor(deuceCheckBox)
        limitCheckBox = VisCheckBox(game.localeBundle.get("LimitTo5Cards"))
        limitCheckBox.setBounds(390f, 250f, 320f, 40f)
        stage.addActor(limitCheckBox)
        limitCheckBox.isChecked = ruleSet.loweredFirstDiscardLimit
        val intSpinnerModel = IntSpinnerModel(ruleSet.playerCount, 2, 4, 1)
        playerCountSpinner = Spinner(game.localeBundle.get("PlayerCount"), intSpinnerModel)
        playerCountSpinner.setBounds(390f, 200f, 320f, 40f)
        playerCountSpinner.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                ruleSet.playerCount = intSpinnerModel.value
            }
        })
        stage.addActor(playerCountSpinner)
        teamCheckBox = VisCheckBox(game.localeBundle.get("TeamPlay"))
        teamCheckBox.setBounds(390f, 150f, 320f, 40f)
        stage.addActor(teamCheckBox)
        teamCheckBox.isChecked = ruleSet.teamPlay
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
        ruleSet.deuceBeatsAce = deuceCheckBox.isChecked
        ruleSet.loweredFirstDiscardLimit = limitCheckBox.isChecked
        ruleSet.teamPlay = teamCheckBox.isChecked
        ruleSet.playerCount = (playerCountSpinner.model as IntSpinnerModel).value
        ruleSet.save(game.preferences)
        game.screen = GameScreen(game)
        dispose()
    }
}