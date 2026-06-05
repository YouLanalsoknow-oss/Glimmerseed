package com.example.glimmerseed.floatingpreviewbase.panel

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import com.example.glimmerseed.editorcore.event.GestureEvent
import com.example.glimmerseed.editorcore.event.SwipeDirection
import com.example.glimmerseed.editorcore.panel.GestureType
import com.example.glimmerseed.editorcore.panel.InteractionLayerData
import com.example.glimmerseed.editorcore.panel.TouchMode

class InteractionLayer(private val data: InteractionLayerData) {

    private val pointerStates = mutableMapOf<Int, PointerState>()

    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressRunnables = mutableMapOf<Int, Runnable>()

    private companion object {
        private const val LONG_PRESS_MS = 500L
        private const val SWIPE_THRESHOLD = 50f
        private const val TAP_MAX_DISTANCE = 20f
    }

    private data class PointerState(
        var downX: Float = 0f,
        var downY: Float = 0f,
        var downTime: Long = 0L,
        var isLongPress: Boolean = false,
        var longPressTriggered: Boolean = false
    )

    fun isInTouchRegion(localX: Float, localY: Float, panelWidth: Int, panelHeight: Int): Boolean {
        when (data.touchMode) {
            TouchMode.PASSTHROUGH -> return false
            TouchMode.BLOCKING -> return true
            TouchMode.REGION_BASED -> {
                val normX = localX / panelWidth.coerceAtLeast(1)
                val normY = localY / panelHeight.coerceAtLeast(1)
                return data.hitRegions.any { region ->
                    val r = region.normalizedRect
                    normX >= r.x && normX <= r.x + r.width &&
                            normY >= r.y && normY <= r.y + r.height &&
                            region.mode != TouchMode.PASSTHROUGH
                }
            }
        }
    }

    fun handleTouchEvent(event: MotionEvent): GestureEvent? {
        val pointerId = event.getPointerId(event.actionIndex)
        val maskedAction = event.actionMasked

        when (maskedAction) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val state = PointerState(
                    downX = event.getX(event.actionIndex),
                    downY = event.getY(event.actionIndex),
                    downTime = System.currentTimeMillis()
                )
                pointerStates[pointerId] = state

                val runnable = Runnable {
                    state.longPressTriggered = true
                    state.isLongPress = true
                }
                longPressRunnables[pointerId] = runnable
                longPressHandler.postDelayed(runnable, LONG_PRESS_MS)
                return null
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val state = pointerStates[pid] ?: continue
                    val dx = event.getX(i) - state.downX
                    val dy = event.getY(i) - state.downY
                    if (!state.longPressTriggered && (dx * dx + dy * dy) > (SWIPE_THRESHOLD * SWIPE_THRESHOLD)) {
                        longPressRunnables[pid]?.let { longPressHandler.removeCallbacks(it) }
                        state.isLongPress = false
                    }
                }
                return null
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val state = pointerStates.remove(pointerId)
                longPressRunnables.remove(pointerId)?.let { longPressHandler.removeCallbacks(it) }
                if (state == null) return null

                val dx = event.getX(event.actionIndex) - state.downX
                val dy = event.getY(event.actionIndex) - state.downY
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (state.longPressTriggered) {
                    return GestureEvent.LongPress(state.downX, state.downY)
                }

                if (distance > SWIPE_THRESHOLD) {
                    val direction = if (Math.abs(dx) > Math.abs(dy)) {
                        if (dx > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT
                    } else {
                        if (dy > 0) SwipeDirection.DOWN else SwipeDirection.UP
                    }
                    return GestureEvent.Swipe(direction, distance)
                }

                if (distance < TAP_MAX_DISTANCE) {
                    return GestureEvent.Tap(event.getX(event.actionIndex), event.getY(event.actionIndex))
                }

                return null
            }

            MotionEvent.ACTION_CANCEL -> {
                pointerStates.clear()
                longPressRunnables.values.forEach { longPressHandler.removeCallbacks(it) }
                longPressRunnables.clear()
                return null
            }
        }
        return null
    }

    fun isRegionBased(): Boolean = data.touchMode == TouchMode.REGION_BASED

    fun getRecognizedGestureTypes(): List<GestureType> {
        return data.hitRegions.flatMap { it.gestureTypes }.distinct()
    }

    fun destroy() {
        pointerStates.clear()
        longPressRunnables.values.forEach { longPressHandler.removeCallbacks(it) }
        longPressRunnables.clear()
    }
}