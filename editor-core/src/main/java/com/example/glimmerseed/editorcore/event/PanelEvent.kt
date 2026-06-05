package com.example.glimmerseed.editorcore.event

import kotlinx.serialization.Serializable

@Serializable
data class PanelEvent(
    val eventType: String,
    val sourcePanelId: String,
    val targetPanelId: String? = null,
    val payload: Map<String, String> = emptyMap()
)

sealed class GestureEvent {
    data class Tap(val x: Float, val y: Float) : GestureEvent()
    data class LongPress(val x: Float, val y: Float) : GestureEvent()
    data class Swipe(
        val direction: SwipeDirection,
        val distance: Float
    ) : GestureEvent()
}

enum class SwipeDirection {
    LEFT,
    RIGHT,
    UP,
    DOWN
}

sealed class SystemEvent {
    object ScreenOff : SystemEvent()
    object ScreenOn : SystemEvent()
    data class VolumeChanged(val keyCode: Int) : SystemEvent()
    data class NotificationReceived(val packageName: String) : SystemEvent()
}