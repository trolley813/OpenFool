package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout

/**
 * Created by main on 18.03.2017.
 * Licensed under MIT License.
 */
internal class ResultScreen(private val game: OpenFoolGame, private val result: ResultScreen.Result) : Screen {

    internal enum class Result {
        TEAM_WON,
        TEAM_LOST,
        TEAM_PARTNER_LOST,
        TEAM_DRAW
    }

    override fun show() {

    }

    override fun render(delta: Float) {
        var color = Color.BLACK
        var header = ""
        var text = ""
        when (result) {
            ResultScreen.Result.TEAM_WON -> {
                color = Color(0.2f, 0.6f, 0.125f, 1f)
                header = game.localeBundle.get("VictoryHeader")
                text = game.localeBundle.get("VictoryText")
            }
            ResultScreen.Result.TEAM_LOST -> {
                color = Color(0.6f, 0.2f, 0.125f, 1f)
                header = game.localeBundle.get("DefeatHeader")
                text = game.localeBundle.get("DefeatText")
            }
            ResultScreen.Result.TEAM_PARTNER_LOST -> {
                color = Color(0.6f, 0.4f, 0.125f, 1f)
                header = game.localeBundle.get("PartnerDefeatHeader")
                text = game.localeBundle.get("PartnerDefeatText")
            }
            ResultScreen.Result.TEAM_DRAW -> {
                color = Color(0.6f, 0.6f, 0.125f, 1f)
                header = game.localeBundle.get("DrawHeader")
                text = game.localeBundle.get("DrawText")
            }
        }
        val headerLayout = GlyphLayout(game.font, header)
        val textLayout = GlyphLayout(game.font, text)
        Gdx.gl.glClearColor(color.r, color.g, color.b, color.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.batch.begin()
        game.font.draw(game.batch, headerLayout,
                400 - headerLayout.width / 2,
                400 - headerLayout.height / 2)
        game.font.draw(game.batch, textLayout,
                400 - textLayout.width / 2,
                280 - textLayout.height / 2)
        game.batch.end()
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.screen = MainMenuScreen(game)
            dispose()
        }
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {

    }

}
