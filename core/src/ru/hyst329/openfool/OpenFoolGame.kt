package ru.hyst329.openfool

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.I18NBundleLoader
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.I18NBundle
import com.kotcrab.vis.ui.VisUI

import java.util.Locale
import kotlin.properties.Delegates

/**
 * Created by hyst329 on 12.03.2017.
 * Licensed under MIT License.
 */

class OpenFoolGame : Game() {
    internal var batch: SpriteBatch by Delegates.notNull()
    internal var assetManager: AssetManager by Delegates.notNull()
    internal var font: BitmapFont by Delegates.notNull()
    internal var preferences: Preferences by Delegates.notNull()
    internal var localeBundle: I18NBundle by Delegates.notNull()

    override fun create() {
        batch = SpriteBatch()
        assetManager = AssetManager()
        VisUI.load()
        font = VisUI.getSkin().getFont("default-font")
        preferences = Gdx.app.getPreferences("OpenFool")
        Gdx.input.isCatchBackKey = true
        // Deal with localisation
        var localeString: String? = preferences.getString("Language", null)
        val locale = if (localeString == null) Locale.getDefault() else Locale(localeString)
        assetManager.load("i18n/OpenFool", I18NBundle::class.java,
                I18NBundleLoader.I18NBundleParameter(locale))
        if (localeString == null) {
            localeString = locale.language
            preferences.putString("Language", localeString)
            preferences.flush()
        }
        assetManager.finishLoadingAsset("i18n/OpenFool")
        localeBundle = assetManager.get("i18n/OpenFool", I18NBundle::class.java)
        val param: TextureLoader.TextureParameter = TextureLoader.TextureParameter()
        param.minFilter = Texture.TextureFilter.MipMap
        param.genMipMaps = true
        val decks = arrayOf("fra", "int", "rus")
        val suits = "cdhs"
        for (d in decks) {
            for (i in 1..13) {
                for (s in suits.toCharArray()) {
                    assetManager.load(String.format(Locale.ENGLISH, "decks/%s/%d%s.png", d, i, s),
                            Texture::class.java, param)
                }
            }
            assetManager.load(String.format(Locale.ENGLISH, "decks/%s/back.png", d),
                    Texture::class.java, param)
        }
        for (i in 1..2) {
            assetManager.load(String.format(Locale.ENGLISH, "backgrounds/background%d.png", i),
                    Texture::class.java, param)
        }
        this.setScreen(MainMenuScreen(this))
    }

    //    @Override
    //    public void render() {
    //        super.render();
    //    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        VisUI.dispose()
    }
}
