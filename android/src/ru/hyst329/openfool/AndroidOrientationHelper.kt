package ru.hyst329.openfool

import OrientationHelper
import android.app.Activity
import android.content.pm.ActivityInfo

class AndroidOrientationHelper(val activity: Activity) : OrientationHelper {
    override fun requestOrientation(orientation: OrientationHelper.Orientation?): Boolean {
        this.orientation = orientation
        //return when (orientation) {
            if (orientation == OrientationHelper.Orientation.LANDSCAPE) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                return true
            }
            if (orientation == OrientationHelper.Orientation.PORTRAIT)  {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                return true
            }
            else {
                return false
            }
        //}
    }

    override var orientation: OrientationHelper.Orientation? = OrientationHelper.Orientation.LANDSCAPE
}