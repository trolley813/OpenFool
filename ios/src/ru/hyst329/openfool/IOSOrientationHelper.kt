package ru.hyst329.openfool

import OrientationHelper

class IOSOrientationHelper : OrientationHelper {

    // Dummy
    override fun requestOrientation(orientation: OrientationHelper.Orientation?): Boolean {
        this.orientation = orientation
        return when (orientation) {
            OrientationHelper.Orientation.LANDSCAPE -> {
                true
            }
            OrientationHelper.Orientation.PORTRAIT -> {
                true
            }
            null -> {
                false
            }
        }
    }

    override var orientation: OrientationHelper.Orientation? = OrientationHelper.Orientation.LANDSCAPE
}