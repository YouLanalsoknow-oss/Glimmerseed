package com.example.glimmerseed.floatingpreviewbase.stage

import android.view.MotionEvent
import com.example.glimmerseed.editorcore.event.PanelEvent
import com.example.glimmerseed.floatingpreviewbase.panel.PanelView
import com.example.glimmerseed.floatingpreviewbase.panel.TouchDispatchResult

internal class InteractionOrchestrator(private val spatialOrchestrator: SpatialOrchestrator) {

    private val pointerLocks = mutableMapOf<Int, String>()
    private val panelEventHandlers = mutableMapOf<String, ((PanelEvent) -> Unit)>()

    fun registerPanelEventHandler(panelId: String, handler: (PanelEvent) -> Unit) {
        panelEventHandlers[panelId] = handler
    }

    fun unregisterPanelEventHandler(panelId: String) {
        panelEventHandlers.remove(panelId)
    }

    fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val pointerId = event.getPointerId(event.actionIndex)
        val maskedAction = event.actionMasked

        when (maskedAction) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val x = event.getX(event.actionIndex)
                val y = event.getY(event.actionIndex)

                val target = spatialOrchestrator.hitTest(x, y)
                if (target != null) {
                    pointerLocks[pointerId] = target.panelId
                    return target.handleTouchEvent(event) == TouchDispatchResult.CONSUMED
                }
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                var consumed = false
                val relevantMoves = mutableMapOf<String, MutableList<Int>>()
                for (i in 0 until event.pointerCount) {
                    val pid = event.getPointerId(i)
                    val lockedId = pointerLocks[pid] ?: continue
                    relevantMoves.getOrPut(lockedId) { mutableListOf() }.add(i)
                }

                for ((panelId, pointerIndices) in relevantMoves) {
                    val panel = spatialOrchestrator.getPanelView(panelId)
                    if (panel?.handleTouchEvent(event) == TouchDispatchResult.CONSUMED) {
                        consumed = true
                    }
                }
                return consumed
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val lockedPanelId = pointerLocks.remove(pointerId)
                if (lockedPanelId != null) {
                    val panel = spatialOrchestrator.getPanelView(lockedPanelId)
                    return panel?.handleTouchEvent(event) == TouchDispatchResult.CONSUMED
                }
                return false
            }

            MotionEvent.ACTION_CANCEL -> {
                pointerLocks.clear()
                return false
            }
        }
        return false
    }

    fun sendPanelEvent(event: PanelEvent) {
        if (event.targetPanelId != null) {
            panelEventHandlers[event.targetPanelId]?.invoke(event)
        } else {
            for (handler in panelEventHandlers.values) {
                handler(event)
            }
        }
    }

    fun releaseLock() {
        pointerLocks.clear()
    }

    fun reset() {
        pointerLocks.clear()
        panelEventHandlers.clear()
    }
}