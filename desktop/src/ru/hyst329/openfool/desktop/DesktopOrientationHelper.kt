package ru.hyst329.openfool.desktop

import OrientationHelper
import com.badlogic.gdx.Gdx

class DesktopOrientationHelper : OrientationHelper {


    override fun requestOrientation(orientation: OrientationHelper.Orientation?): Boolean {
        this.orientation = orientation
        return when (orientation) {
            OrientationHelper.Orientation.LANDSCAPE -> {
                if (!Gdx.graphics.isFullscreen)
                    Gdx.graphics.setWindowedMode(800, 480)
                true
            }
            OrientationHelper.Orientation.PORTRAIT -> {
                if (!Gdx.graphics.isFullscreen)
                    Gdx.graphics.setWindowedMode(480, 800)
                true
            }
            null -> {
                false
            }
        }
    }

    override var orientation: OrientationHelper.Orientation? = OrientationHelper.Orientation.LANDSCAPE
}