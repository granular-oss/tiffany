package ag.granular.tiff

/**
 * Coordinates of a window over a portion or the entire image coordinates
 */
data class ImageWindow(
    val minX: Int = 0,
    val minY: Int = 0,
    val maxX: Int = 0,
    val maxY: Int = 0
) {
    companion object {
        fun fromXY(x: Int, y: Int): ImageWindow = ImageWindow(x, y, x + 1, y + 1)
    }
}
