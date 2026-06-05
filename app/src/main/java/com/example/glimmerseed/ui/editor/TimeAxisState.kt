package com.example.glimmerseed.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlin.math.floor

class TimeAxisState(
    initialDuration: Float = 10f,
    initialCurrentTime: Float = 0f,
    initialPixelsPerSecond: Float = 100f
) {
    var duration by mutableFloatStateOf(initialDuration)
    var currentTime by mutableFloatStateOf(initialCurrentTime)
    var pixelsPerSecond by mutableFloatStateOf(initialPixelsPerSecond.coerceAtLeast(0.1f))
    var scrollOffset by mutableFloatStateOf(0f)
    var isDraggingPlayhead by mutableStateOf(false)

    val currentTimeUnit: TimeUnit
        get() = when {
            pixelsPerSecond >= 300 -> TimeUnit.FRAME
            pixelsPerSecond >= 10 -> TimeUnit.SECOND
            pixelsPerSecond >= 0.33f -> TimeUnit.THIRTY_SECONDS
            else -> TimeUnit.MINUTE
        }

    enum class TimeUnit(val value: Float, val label: String) {
        FRAME(1 / 30f, "帧"),
        SECOND(1f, "秒"),
        THIRTY_SECONDS(30f, "30秒"),
        MINUTE(60f, "分钟")
    }

    fun timeToPixel(time: Float): Float {
        val safePixelsPerSecond = pixelsPerSecond.coerceAtLeast(0.1f)
        return ((time * safePixelsPerSecond) - scrollOffset).coerceIn(-10000f, 10000f)
    }

    fun pixelToTime(pixel: Float): Float {
        val safePixelsPerSecond = pixelsPerSecond.coerceAtLeast(0.1f)
        return (pixel + scrollOffset) / safePixelsPerSecond
    }

    val visibleTimeRange: ClosedFloatingPointRange<Float>
        get() {
            val start = pixelToTime(0f)
            val end = pixelToTime(10000f)
            return start..end
        }

    fun zoom(zoomFactor: Float, centerPixel: Float) {
        val centerTime = pixelToTime(centerPixel)
        pixelsPerSecond = (pixelsPerSecond * zoomFactor).coerceIn(0.1f, 1000f)
        scrollOffset = (centerTime * pixelsPerSecond) - centerPixel
    }

    fun scroll(delta: Float) {
        scrollOffset = (scrollOffset + delta).coerceAtLeast(0f)
    }

    fun setCurrentTimeAndEnsureVisible(time: Float, viewWidth: Float) {
        currentTime = time.coerceIn(0f, duration)
        val pixel = timeToPixel(currentTime)
        if (pixel < 0 || pixel > viewWidth) {
            scrollOffset = (currentTime * pixelsPerSecond) - viewWidth / 2
        }
    }
}

@Composable
fun rememberTimeAxisState(
    initialDuration: Float = 10f,
    initialCurrentTime: Float = 0f,
    initialPixelsPerSecond: Float = 100f
): TimeAxisState {
    return androidx.compose.runtime.remember {
        TimeAxisState(initialDuration, initialCurrentTime, initialPixelsPerSecond)
    }
}
