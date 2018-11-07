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
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner

internal class NewGameScreen(private val game: OpenFoolGame) : Screen {
    private val stage: Stage = Stage(FitViewport(800f, 480f))
    private val singlePlayerButton: VisTextButton
    private val gameplayLabel: VisLabel
    private val deuceCheckBox: VisCheckBox
    private val limitCheckBox: VisCheckBox
    private val passCheckBox: VisCheckBox
    private val playerCountSpinner: Spinner
    private val teamCheckBox: VisCheckBox
    private val cardCountLabel: VisLabel
    private val cardCountSelectBox: VisSelectBox<Int>
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
        gameplayLabel.setBounds(340f, 400f, 400f, 40f)
        stage.addActor(gameplayLabel)
        deuceCheckBox = VisCheckBox(game.localeBundle.get("DeuceBeatsAce"))
        deuceCheckBox.setBounds(340f, 350f, 400f, 40f)
        deuceCheckBox.isChecked = ruleSet.deuceBeatsAce
        stage.addActor(deuceCheckBox)
        limitCheckBox = VisCheckBox(game.localeBundle.get("LimitTo5Cards"))
        limitCheckBox.setBounds(340f, 300f, 400f, 40f)
        stage.addActor(limitCheckBox)
        limitCheckBox.isChecked = ruleSet.loweredFirstDiscardLimit
        passCheckBox = VisCheckBox(game.localeBundle.get("PassingGame"))
        passCheckBox.setBounds(340f, 250f, 400f, 40f)
        stage.addActor(passCheckBox)
        passCheckBox.isChecked = ruleSet.allowPass
        val intSpinnerModel = IntSpinnerModel(ruleSet.playerCount, 2, 5, 1)
        playerCountSpinner = Spinner(game.localeBundle.get("PlayerCount"), intSpinnerModel)
        playerCountSpinner.setBounds(340f, 200f, 400f, 40f)
        playerCountSpinner.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                ruleSet.playerCount = intSpinnerModel.value
            }
        })
        stage.addActor(playerCountSpinner)
        teamCheckBox = VisCheckBox(game.localeBundle.get("TeamPlay"))
        teamCheckBox.setBounds(340f, 150f, 400f, 40f)
        stage.addActor(teamCheckBox)
        teamCheckBox.isChecked = ruleSet.teamPlay
        cardCountLabel = VisLabel(game.localeBundle.get("DeckCardsCount"))
        cardCountLabel.setBounds(340f, 100f, 180f, 40f)
        stage.addActor(cardCountLabel)
        cardCountSelectBox = VisSelectBox()
        cardCountSelectBox.setItems(24, 32, 36, 52)
        println("card count is ${ruleSet.cardCount}")
        cardCountSelectBox.selected = ruleSet.cardCount
        println("selected is ${cardCountSelectBox.selected}")
        cardCountSelectBox.setBounds(540f, 100f, 200f, 40f)
        stage.addActor(cardCountSelectBox)
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
        ruleSet.allowPass = passCheckBox.isChecked
        ruleSet.playerCount = (playerCountSpinner.model as IntSpinnerModel).value
        ruleSet.cardCount = cardCountSelectBox.selected
        ruleSet.save(game.preferences)
        game.screen = GameScreen(game)
        dispose()
    }
}