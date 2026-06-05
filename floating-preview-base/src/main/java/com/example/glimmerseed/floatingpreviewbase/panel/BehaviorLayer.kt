package com.example.glimmerseed.floatingpreviewbase.panel

import com.example.glimmerseed.editorcore.event.GestureEvent
import com.example.glimmerseed.editorcore.event.PanelEvent
import com.example.glimmerseed.editorcore.event.SystemEvent
import com.example.glimmerseed.editorcore.panel.BehaviorAction
import com.example.glimmerseed.editorcore.panel.BehaviorLayerData

class BehaviorLayer(
    private val data: BehaviorLayerData,
    private val actionExecutor: (BehaviorAction) -> Unit
) {

    fun handleGestureEvent(event: GestureEvent): Boolean {
        val gestureType = when (event) {
            is GestureEvent.Tap -> com.example.glimmerseed.editorcore.panel.GestureType.TAP
            is GestureEvent.LongPress -> com.example.glimmerseed.editorcore.panel.GestureType.LONG_PRESS
            is GestureEvent.Swipe -> when (event.direction) {
                com.example.glimmerseed.editorcore.event.SwipeDirection.LEFT ->
                    com.example.glimmerseed.editorcore.panel.GestureType.SWIPE_LEFT
                com.example.glimmerseed.editorcore.event.SwipeDirection.RIGHT ->
                    com.example.glimmerseed.editorcore.panel.GestureType.SWIPE_RIGHT
                com.example.glimmerseed.editorcore.event.SwipeDirection.UP ->
                    com.example.glimmerseed.editorcore.panel.GestureType.SWIPE_UP
                com.example.glimmerseed.editorcore.event.SwipeDirection.DOWN ->
                    com.example.glimmerseed.editorcore.panel.GestureType.SWIPE_DOWN
            }
        }

        val handler = data.gestureHandlers.find { it.gestureType == gestureType }
        handler?.let { actionExecutor(it.action) }
        return handler != null
    }

    fun handleSystemEvent(event: SystemEvent): Boolean {
        val eventType = when (event) {
            is SystemEvent.ScreenOff -> com.example.glimmerseed.editorcore.panel.SystemEventType.SCREEN_OFF
            is SystemEvent.ScreenOn -> com.example.glimmerseed.editorcore.panel.SystemEventType.SCREEN_ON
            is SystemEvent.VolumeChanged -> when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_VOLUME_UP ->
                    com.example.glimmerseed.editorcore.panel.SystemEventType.VOLUME_UP
                android.view.KeyEvent.KEYCODE_VOLUME_DOWN ->
                    com.example.glimmerseed.editorcore.panel.SystemEventType.VOLUME_DOWN
                else -> return false
            }
            is SystemEvent.NotificationReceived ->
                com.example.glimmerseed.editorcore.panel.SystemEventType.NOTIFICATION
        }

        val handler = data.systemEventHandlers.find { it.eventType == eventType }
        handler?.let { actionExecutor(it.action) }
        return handler != null
    }

    fun handlePanelEvent(event: PanelEvent): Boolean {
        val handler = data.panelEventHandlers.find { it.eventName == event.eventType }
        handler?.let { actionExecutor(it.action) }
        return handler != null
    }
}