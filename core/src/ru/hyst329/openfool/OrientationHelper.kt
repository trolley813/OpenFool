interface OrientationHelper {
    enum class Orientation {
        LANDSCAPE, PORTRAIT
    }

    fun requestOrientation(orientation: Orientation?): Boolean
    var orientation: Orientation?
}