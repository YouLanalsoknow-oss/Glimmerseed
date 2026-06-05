package com.example.glimmerseed.floatingpreviewbase.stage

import android.content.Context
import android.view.MotionEvent
import com.example.glimmerseed.editorcore.event.PanelEvent
import com.example.glimmerseed.editorcore.event.SystemEvent
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.panel.TouchMode
import com.example.glimmerseed.editorcore.stage.PanelSlot
import com.example.glimmerseed.editorcore.stage.StageData
import com.example.glimmerseed.floatingpreviewbase.panel.PanelView

class StageEngine(private val context: Context) {

    internal val spatialOrchestrator = SpatialOrchestrator()
    internal val interactionOrchestrator = InteractionOrchestrator(spatialOrchestrator)
    internal val stateOrchestrator = StateOrchestrator()

    private val panelViews = mutableMapOf<String, PanelView>()
    private var onNeedsTouchChanged: ((Boolean) -> Unit)? = null
    private var onPanelAdded: ((PanelView) -> Unit)? = null
    private var onPanelRemoved: ((PanelView) -> Unit)? = null

    fun setOnNeedsTouchChanged(listener: (Boolean) -> Unit) {
        onNeedsTouchChanged = listener
    }

    fun setOnPanelAdded(listener: (PanelView) -> Unit) {
        onPanelAdded = listener
    }

    fun setOnPanelRemoved(listener: (PanelView) -> Unit) {
        onPanelRemoved = listener
    }

    fun loadStage(stageData: StageData) {
        clearAll()
        for (slot in stageData.panels) {
            if (slot.active) {
                stateOrchestrator.registerPanel(slot.panelId, true)
            }
        }
    }

    fun addPanel(data: PanelData, slot: PanelSlot): PanelView {
        val view = PanelView(context)
        view.bindPanelData(data, slot.landscapeRect, slot.portraitRect)
        view.zOrder = slot.zOrder
        view.isPanelActive = slot.active
        view.touchMode = data.interaction.touchMode

        panelViews[data.id] = view
        spatialOrchestrator.addPanel(view, slot)
        stateOrchestrator.registerPanel(data.id, slot.active)
        interactionOrchestrator.registerPanelEventHandler(data.id) { event ->
            view.behaviorLayer?.handlePanelEvent(event)
        }

        onPanelAdded?.invoke(view)
        updateTouchFlags()
        return view
    }

    fun removePanel(panelId: String) {
        val view = spatialOrchestrator.getPanelView(panelId) ?: return
        spatialOrchestrator.removePanel(panelId)
        stateOrchestrator.unregisterPanel(panelId)
        interactionOrchestrator.unregisterPanelEventHandler(panelId)
        panelViews.remove(panelId)

        onPanelRemoved?.invoke(view)
        view.destroy()
        updateTouchFlags()
    }

    fun setPanelActive(panelId: String, active: Boolean) {
        val view = spatialOrchestrator.getPanelView(panelId) ?: return
        view.isPanelActive = active
        view.visibility = if (active) android.view.View.VISIBLE else android.view.View.GONE
        stateOrchestrator.setActive(panelId, active)
        updateTouchFlags()
    }

    fun handleTouchEvent(event: MotionEvent): Boolean {
        return interactionOrchestrator.dispatchTouchEvent(event)
    }

    fun handleSystemEvent(event: SystemEvent) {
        for (id in spatialOrchestrator.sortedPanelIds) {
            val view = spatialOrchestrator.getPanelView(id) ?: continue
            view.behaviorLayer?.handleSystemEvent(event)
        }
    }

    fun sendPanelEvent(event: PanelEvent) {
        interactionOrchestrator.sendPanelEvent(event)
    }

    fun layoutPanels(isLandscape: Boolean, rootWidth: Int, rootHeight: Int) {
        spatialOrchestrator.layoutPanels(isLandscape, rootWidth, rootHeight)
    }

    fun pause() {
        stateOrchestrator.pause()
    }

    fun resume() {
        stateOrchestrator.resume()
    }

    fun getPanelView(panelId: String): PanelView? = spatialOrchestrator.getPanelView(panelId)

    fun clearAll() {
        for (id in spatialOrchestrator.sortedPanelIds.toList()) {
            removePanel(id)
        }
        interactionOrchestrator.reset()
        stateOrchestrator.reset()
    }

    fun destroy() {
        clearAll()
    }

    private fun updateTouchFlags() {
        val needsTouch = spatialOrchestrator.sortedPanelIds.any { id ->
            val view = spatialOrchestrator.getPanelView(id)
            view != null && view.isPanelActive && view.touchMode != TouchMode.PASSTHROUGH
        }
        onNeedsTouchChanged?.invoke(needsTouch)
    }
}