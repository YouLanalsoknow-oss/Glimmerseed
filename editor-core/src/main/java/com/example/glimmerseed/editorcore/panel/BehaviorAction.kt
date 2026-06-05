package com.example.glimmerseed.editorcore.panel

import kotlinx.serialization.Serializable

@Serializable
sealed class BehaviorAction {

    @Serializable
    data class ToggleVisibility(val panelId: String) : BehaviorAction()

    @Serializable
    data class PlayAnimation(val animationId: String) : BehaviorAction()

    @Serializable
    data class SendEvent(
        val eventName: String,
        val payload: Map<String, String> = emptyMap()
    ) : BehaviorAction()

    @Serializable
    data class SetTouchMode(val mode: TouchMode) : BehaviorAction()

    @Serializable
    object NoOp : BehaviorAction()
}

@Serializable
data class GestureHandler(
    val gestureType: GestureType,
    val action: BehaviorAction
)

@Serializable
data class SystemEventHandler(
    val eventType: SystemEventType,
    val action: BehaviorAction
)

@Serializable
enum class SystemEventType {
    SCREEN_OFF,
    SCREEN_ON,
    VOLUME_UP,
    VOLUME_DOWN,
    NOTIFICATION
}

@Serializable
data class PanelEventHandler(
    val eventName: String,
    val action: BehaviorAction
)