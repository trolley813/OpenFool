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
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter

import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

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
    private val picker: ColorPicker
    private val deckSelectBox: VisSelectBox<String>
    private val languageSelectBox: VisSelectBox<String>
    private val sortingSelectBox: VisSelectBox<String>
    private var back: Sprite? = null
    private var ace: Sprite? = null
    private var queen: Sprite? = null
    private var ten: Sprite? = null
    private var deuce: Sprite? = null


    init {
        // Initialise DECKS
        DECKS = HashMap<String, String>()
        DECKS.put(game.getLocaleBundle().get("CardsRussian"), "rus")
        DECKS.put(game.getLocaleBundle().get("CardsInternational"), "int")
        DECKS.put(game.getLocaleBundle().get("CardsFrench"), "fra")
        // Initialise LANGUAGES
        LANGUAGES = HashMap<String, String>()
        LANGUAGES.put(game.getLocaleBundle().get("LanguageRussian"), "ru")
        LANGUAGES.put(game.getLocaleBundle().get("LanguageEnglish"), "en")
        // Initialise SORTING_MODES
        val SORTING_MODES = ArrayList<String>()
        SORTING_MODES.add(game.getLocaleBundle().get("SortingUnsorted"))
        SORTING_MODES.add(game.getLocaleBundle().get("SortingSuitAscending"))
        SORTING_MODES.add(game.getLocaleBundle().get("SortingSuitDescending"))
        SORTING_MODES.add(game.getLocaleBundle().get("SortingRankAscending"))
        SORTING_MODES.add(game.getLocaleBundle().get("SortingRankDescending"))
        // Initialise the stage
        stage = Stage(FitViewport(800f, 480f))
        Gdx.input.inputProcessor = stage

        backgroundColor = Color(game.getPreferences().getInteger(BACKGROUND_COLOR, 0x33cc4dff))
        deck = game.getPreferences().getString(DECK, "rus")
        language = game.getPreferences().getString(LANGUAGE, "ru")
        sortingMode = Player.SortingMode.fromInt(game.getPreferences().getInteger(SORTING_MODE, 0))

        picker = ColorPicker("Choose background color", object : ColorPickerAdapter() {
            override fun finished(newColor: Color?) {
                backgroundColor = newColor
            }

        })

        val changeColorButton = VisTextButton(game.getLocaleBundle().get("ChangeBackgroundColor"))
        changeColorButton.setBounds(40f, 300f, 250f, 80f)
        changeColorButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                picker.color = backgroundColor
                stage.addActor(picker.fadeIn())
            }
        })
        stage.addActor(changeColorButton)
        val saveButton = VisTextButton(game.getLocaleBundle().get("SaveSettings"))
        saveButton.setBounds(40f, 200f, 250f, 80f)
        saveButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                // super.clicked(event, x, y);
                saveAndQuit()

            }
        })
        stage.addActor(saveButton)
        val deckSelectLabel = VisLabel(game.getLocaleBundle().get("Cards"))
        deckSelectLabel.setBounds(420f, 300f, 60f, 40f)
        stage.addActor(deckSelectLabel)
        val languageSelectLabel = VisLabel(game.getLocaleBundle().get("Language"))
        languageSelectLabel.setBounds(420f, 350f, 60f, 40f)
        stage.addActor(languageSelectLabel)
        val sortingSelectLabel = VisLabel(game.getLocaleBundle().get("Sorting"))
        sortingSelectLabel.setBounds(420f, 400f, 60f, 40f)
        stage.addActor(sortingSelectLabel)
        deckSelectBox = VisSelectBox<String>()
        deckSelectBox.setBounds(580f, 300f, 180f, 40f)
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
        languageSelectBox.setBounds(580f, 350f, 180f, 40f)
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
        sortingSelectBox.setBounds(580f, 400f, 180f, 40f)
        sortingSelectBox.setItems(*SORTING_MODES.toTypedArray())
        sortingSelectBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeListener.ChangeEvent, actor: Actor) {
                sortingMode = Player.SortingMode.fromInt(sortingSelectBox.selectedIndex)
            }


        })
        sortingSelectBox.selectedIndex = sortingMode!!.getValue()
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
        game.getBatch().begin()
        if (!stage.actors.contains(picker, true)) {
            back!!.draw(game.getBatch())
            ace!!.draw(game.getBatch())
            queen!!.draw(game.getBatch())
            ten!!.draw(game.getBatch())
            deuce!!.draw(game.getBatch())
        }
        game.getBatch().end()
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
        game.getPreferences().putInteger(BACKGROUND_COLOR, Color.rgba8888(backgroundColor!!))
        game.getPreferences().putString(DECK, deck)
        game.getPreferences().putString(LANGUAGE, language)
        game.getPreferences().putInteger(SORTING_MODE, sortingMode!!.getValue())
        game.getPreferences().flush()
        game.screen = MainMenuScreen(game)
        dispose()
    }

    private fun updateSprites() {
        back = Sprite(game.getAssetManager().get(
                String.format("decks/%s/back.png", deck), Texture::class.java))
        ace = Sprite(game.getAssetManager().get(
                String.format("decks/%s/1s.png", deck), Texture::class.java))
        queen = Sprite(game.getAssetManager().get(
                String.format("decks/%s/12d.png", deck), Texture::class.java))
        ten = Sprite(game.getAssetManager().get(
                String.format("decks/%s/10h.png", deck), Texture::class.java))
        deuce = Sprite(game.getAssetManager().get(
                String.format("decks/%s/2c.png", deck), Texture::class.java))
        var index = 0
        for (sprite in arrayOf<Sprite>(back, ace, queen, ten, deuce)) {
            sprite.setScale(CARD_SCALE)
            sprite.setCenter((360 + 90 * index++).toFloat(), 200f)
        }
    }

    private fun reloadLocale() {
        game.getAssetManager().unload("i18n/OpenFool")
        game.getAssetManager().load("i18n/OpenFool", I18NBundle::class.java,
                I18NBundleLoader.I18NBundleParameter(Locale(language)))
        game.getAssetManager().finishLoadingAsset("i18n/OpenFool")
        game.setLocaleBundle(game.getAssetManager().get("i18n/OpenFool", I18NBundle::class.java))
    }

    companion object {
        val BACKGROUND_COLOR = "BackgroundColor"
        val DECK = "Deck"
        private val LANGUAGE = "Language"
        val SORTING_MODE = "SortingMode"
        private val CARD_SCALE = 0.25f
    }
}
