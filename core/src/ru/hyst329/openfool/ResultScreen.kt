package ru.hyst329.openfool

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport

/**
 * Created by main on 18.03.2017.
 * Licensed under MIT License.
 */
internal class ResultScreen(private val game: OpenFoolGame,
                            private val result: ResultScreen.Result,
                            private val playersPlaces: Map<Int, String>) : Screen {

    private val stage: Stage

    internal enum class Result {
        TEAM_WON,
        TEAM_LOST,
        TEAM_PARTNER_LOST,
        TEAM_DRAW,
        WON,
        LOST,
        DRAW
    }

    init {
        game.orientationHelper.requestOrientation(OrientationHelper.Orientation.LANDSCAPE)
        stage = Stage(FitViewport(800f, 480f))
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
                header = game.localeBundle.get("TeamVictoryHeader")
                text = game.localeBundle.get("TeamVictoryText")
            }
            ResultScreen.Result.TEAM_LOST -> {
                color = Color(0.6f, 0.2f, 0.125f, 1f)
                header = game.localeBundle.get("TeamDefeatHeader")
                text = game.localeBundle.get("TeamDefeatText")
            }
            ResultScreen.Result.TEAM_PARTNER_LOST -> {
                color = Color(0.6f, 0.4f, 0.125f, 1f)
                header = game.localeBundle.get("TeamPartnerDefeatHeader")
                text = game.localeBundle.get("TeamPartnerDefeatText")
            }
            ResultScreen.Result.TEAM_DRAW -> {
                color = Color(0.6f, 0.6f, 0.125f, 1f)
                header = game.localeBundle.get("TeamDrawHeader")
                text = game.localeBundle.get("TeamDrawText")
            }
            ResultScreen.Result.WON -> {
                color = Color(0.2f, 0.6f, 0.125f, 1f)
                header = game.localeBundle.get("VictoryHeader")
                text = game.localeBundle.get("VictoryText")
            }
            ResultScreen.Result.LOST -> {
                color = Color(0.6f, 0.2f, 0.125f, 1f)
                header = game.localeBundle.get("DefeatHeader")
                text = game.localeBundle.get("DefeatText")
            }
            ResultScreen.Result.DRAW -> {
                color = Color(0.6f, 0.6f, 0.125f, 1f)
                header = game.localeBundle.get("DrawHeader")
                text = game.localeBundle.get("DrawText")
            }
        }
        var places = ""
        for ((p, n) in playersPlaces.entries.toList().sortedBy { it.key }) {
            places += game.localeBundle.format("PlayerPlace", n, p) + "\n"
        }
        val headerLayout = GlyphLayout(game.font, header)
        val textLayout = GlyphLayout(game.font, text)
        val placesLayout = GlyphLayout(game.font, places)
        Gdx.gl.glClearColor(color.r, color.g, color.b, color.a)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.batch.transformMatrix = stage.viewport.camera.view
        game.batch.projectionMatrix = stage.viewport.camera.projection
        game.batch.begin()
        game.font.draw(game.batch, headerLayout,
                stage.viewport.worldWidth * 0.5f - headerLayout.width / 2,
                stage.viewport.worldHeight * 0.833f - headerLayout.height / 2)
        game.font.draw(game.batch, textLayout,
                stage.viewport.worldWidth * 0.5f - textLayout.width / 2,
                stage.viewport.worldHeight * 0.58f - textLayout.height / 2)
        game.font.draw(game.batch, placesLayout,
                stage.viewport.worldWidth * 0.5f - textLayout.width / 2,
                stage.viewport.worldHeight * 0.4f - textLayout.height / 2)
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
