package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.loaders.I18NBundleLoader
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.viewport.FitViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisSelectBox
import com.kotcrab.vis.ui.widget.VisTextButton
import com.kotcrab.vis.ui.widget.VisDialog
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner

import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import kotlin.properties.Delegates

/**
 * Created by main on 18.03.2017.
 * Licensed under MIT License.
 */
internal class SettingsScreen(private val game: OpenFoolGame) : Screen {
    private val DECKS: MutableMap<String, String>
    private val LANGUAGES: MutableMap<String, String>
    private val stage: Stage
    private var backgroundColor: Color? = null
    private var deck: String? = null
    private var language: String? = null
    private var sortingMode: Player.SortingMode? = null
    private var background: Int
    private val picker: ColorPicker
    private val deckSelectBox: VisSelectBox<String>
    private val languageSelectBox: VisSelectBox<String>
    private val sortingSelectBox: VisSelectBox<String>
    private val backgroundSpinner: Spinner
    private var back: Sprite by Delegates.notNull()
    private var ace: Sprite by Delegates.notNull()
    private var queen: Sprite by Delegates.notNull()
    private var ten: Sprite by Delegates.notNull()
    private var deuce: Sprite by Delegates.notNull()


    init {
        // Initialise DECKS
        DECKS = HashMap<String, String>()
        DECKS.put(game.localeBundle.get("CardsRussian"), "rus")
        DECKS.put(game.localeBundle.get("CardsInternational"), "int")
        DECKS.put(game.localeBundle.get("CardsFrench"), "fra")
        // Initialise LANGUAGES
        LANGUAGES = HashMap<String, String>()
        LANGUAGES.put(game.localeBundle.get("LanguageRussian"), "ru")
        LANGUAGES.put(game.localeBundle.get("LanguageEnglish"), "en")
        LANGUAGES.put(game.localeBundle.get("LanguageCzech"), "cs")
        // Initialise SORTING_MODES
        val SORTING_MODES = ArrayList<String>()
        SORTING_MODES.add(game.localeBundle.get("SortingUnsorted"))
        SORTING_MODES.add(game.localeBundle.get("SortingSuitAscending"))
        SORTING_MODES.add(game.localeBundle.get("SortingSuitDescending"))
        SORTING_MODES.add(game.localeBundle.get("SortingRankAscending"))
        SORTING_MODES.add(game.localeBundle.get("SortingRankDescending"))
        // Initialise the stage
        stage = Stage(FitViewport(800f, 480f))
        Gdx.input.inputProcessor = stage

        backgroundColor = Color(game.preferences.getInteger(BACKGROUND_COLOR, 0x33cc4dff))
        deck = game.preferences.getString(DECK, "rus")
        language = game.preferences.getString(LANGUAGE, "ru")
        sortingMode = Player.SortingMode.fromInt(game.preferences.getInteger(SORTING_MODE, 0))
        background = game.preferences.getInteger(BACKGROUND, 1)
        picker = ColorPicker("Choose background color", object : ColorPickerAdapter() {
            override fun finished(newColor: Color?) {
                backgroundColor = newColor
            }

        })

        val changeColorButton = VisTextButton(game.localeBundle.get("ChangeBackgroundColor"))
        changeColorButton.setBounds(40f, 350f, 250f, 80f)
        changeColorButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                picker.color = backgroundColor
                stage.addActor(picker.fadeIn())
            }
        })
        stage.addActor(changeColorButton)
        val saveButton = VisTextButton(game.localeBundle.get("SaveSettings"))
        saveButton.setBounds(40f, 250f, 250f, 80f)
        saveButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                saveAndQuit()

            }
        })
        stage.addActor(saveButton)
        val gameplaySettingsButton = VisTextButton(game.localeBundle.get("GameplaySettings"))
        gameplaySettingsButton.setBounds(40f, 150f, 250f, 80f)
        gameplaySettingsButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                // TODO: Add real gameplay settings
                val win = VisDialog(game.localeBundle.get("GameplaySettings"))
                win.button(game.localeBundle.get("OK"), true)
                win.button(game.localeBundle.get("Cancel"), false)
                win.key(Input.Keys.ENTER, true).key(Input.Keys.ESCAPE, false)
                // Adding checkboxes for rules
                win.show(stage)
            }
        })
        stage.addActor(gameplaySettingsButton)
        val backgroundSelectLabel = VisLabel(game.localeBundle.get("Background"))
        backgroundSelectLabel.setBounds(340f, 250f, 100f, 40f)
        stage.addActor(backgroundSelectLabel)
        val deckSelectLabel = VisLabel(game.localeBundle.get("Cards"))
        deckSelectLabel.setBounds(340f, 300f, 100f, 40f)
        stage.addActor(deckSelectLabel)
        val languageSelectLabel = VisLabel(game.localeBundle.get("Language"))
        languageSelectLabel.setBounds(340f, 350f, 100f, 40f)
        stage.addActor(languageSelectLabel)
        val sortingSelectLabel = VisLabel(game.localeBundle.get("Sorting"))
        sortingSelectLabel.setBounds(340f, 400f, 100f, 40f)
        stage.addActor(sortingSelectLabel)
        val intSpinnerModel = IntSpinnerModel(background, 1, 2, 1)
        backgroundSpinner = Spinner("", intSpinnerModel)
        backgroundSpinner.setBounds(530f, 250f, 230f, 40f)
        backgroundSpinner.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                background = intSpinnerModel.value
                // updateSprites()
            }


        })
        stage.addActor(backgroundSpinner)
        deckSelectBox = VisSelectBox()
        deckSelectBox.setBounds(530f, 300f, 230f, 40f)
        deckSelectBox.setItems(*DECKS.keys.toTypedArray())
        deckSelectBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                deck = DECKS[deckSelectBox.selected]
                updateSprites()
            }


        })
        var deckName = ""
        for ((key, value) in DECKS) {
            if (value == deck) {
                deckName = key
            }
        }
        deckSelectBox.setSelected(deckName)
        stage.addActor(deckSelectBox)
        languageSelectBox = VisSelectBox<String>()
        languageSelectBox.setBounds(530f, 350f, 230f, 40f)
        languageSelectBox.setItems(*LANGUAGES.keys.toTypedArray())
        languageSelectBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                language = LANGUAGES[languageSelectBox.selected]
                reloadLocale()
            }


        })
        var languageName = ""
        for ((key, value) in LANGUAGES) {
            if (value == language) {
                languageName = key
            }
        }
        languageSelectBox.setSelected(languageName)
        stage.addActor(languageSelectBox)
        sortingSelectBox = VisSelectBox<String>()
        sortingSelectBox.setBounds(530f, 400f, 230f, 40f)
        sortingSelectBox.setItems(*SORTING_MODES.toTypedArray())
        sortingSelectBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                sortingMode = Player.SortingMode.fromInt(sortingSelectBox.selectedIndex)
            }


        })
        sortingSelectBox.selectedIndex = sortingMode!!.value
        stage.addActor(sortingSelectBox)
        updateSprites()

    }

    override fun show() {

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(backgroundColor!!.r, backgroundColor!!.g, backgroundColor!!.b, backgroundColor!!.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
        game.batch.begin()
        if (!stage.actors.contains(picker, true)) {
            back.draw(game.batch)
            ace.draw(game.batch)
            queen.draw(game.batch)
            ten.draw(game.batch)
            deuce.draw(game.batch)
        }
        game.batch.end()
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveAndQuit()
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
        picker.dispose()
    }


    private fun saveAndQuit() {
        game.preferences.putInteger(BACKGROUND_COLOR, Color.rgba8888(backgroundColor!!))
        game.preferences.putString(DECK, deck)
        game.preferences.putString(LANGUAGE, language)
        game.preferences.putInteger(SORTING_MODE, sortingMode!!.value)
        game.preferences.putInteger(BACKGROUND, background)
        game.preferences.flush()
        game.screen = MainMenuScreen(game)
        dispose()
    }

    private fun updateSprites() {
        back = Sprite(game.assetManager.get(
                String.format("decks/%s/back.png", deck), Texture::class.java))
        ace = Sprite(game.assetManager.get(
                String.format("decks/%s/1s.png", deck), Texture::class.java))
        queen = Sprite(game.assetManager.get(
                String.format("decks/%s/12d.png", deck), Texture::class.java))
        ten = Sprite(game.assetManager.get(
                String.format("decks/%s/10h.png", deck), Texture::class.java))
        deuce = Sprite(game.assetManager.get(
                String.format("decks/%s/2c.png", deck), Texture::class.java))
        for ((index, sprite) in arrayOf(back, ace, queen, ten, deuce).withIndex()) {
            sprite.setScale(CARD_SCALE)
            sprite.setCenter((360f + 90f * index), 100f)
        }
    }

    private fun reloadLocale() {
        game.assetManager.unload("i18n/OpenFool")
        game.assetManager.load("i18n/OpenFool", I18NBundle::class.java,
                I18NBundleLoader.I18NBundleParameter(Locale(language)))
        game.assetManager.finishLoadingAsset<I18NBundle>("i18n/OpenFool")
        game.localeBundle = game.assetManager.get("i18n/OpenFool", I18NBundle::class.java)
    }

    companion object {
        val BACKGROUND_COLOR = "BackgroundColor"
        val DECK = "Deck"
        private val LANGUAGE = "Language"
        val SORTING_MODE = "SortingMode"
        val BACKGROUND = "Background"
        private val CARD_SCALE = 0.25f
    }
}
