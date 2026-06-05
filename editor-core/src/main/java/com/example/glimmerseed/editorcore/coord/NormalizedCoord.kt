package com.example.glimmerseed.editorcore.coord

import kotlinx.serialization.Serializable

@Serializable
data class NormalizedRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    init {
        require(x in 0f..1f) { "x must be in 0.0..1.0" }
        require(y in 0f..1f) { "y must be in 0.0..1.0" }
        require(width in 0f..1f) { "width must be in 0.0..1.0" }
        require(height in 0f..1f) { "height must be in 0.0..1.0" }
        require(x + width <= 1.0001f) { "x + width must not exceed 1.0" }
        require(y + height <= 1.0001f) { "y + height must not exceed 1.0" }
    }
}

data class PixelRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

object CoordConverter {

    fun normalizedToPixel(
        rect: NormalizedRect,
        screenWidthPx: Int,
        screenHeightPx: Int
    ): PixelRect {
        return PixelRect(
            x = (rect.x * screenWidthPx).toInt(),
            y = (rect.y * screenHeightPx).toInt(),
            width = (rect.width * screenWidthPx).toInt(),
            height = (rect.height * screenHeightPx).toInt()
        )
    }

    fun pixelToNormalized(
        rect: PixelRect,
        screenWidthPx: Int,
        screenHeightPx: Int
    ): NormalizedRect {
        return NormalizedRect(
            x = rect.x.toFloat() / screenWidthPx,
            y = rect.y.toFloat() / screenHeightPx,
            width = rect.width.toFloat() / screenWidthPx,
            height = rect.height.toFloat() / screenHeightPx
        )
    }
}