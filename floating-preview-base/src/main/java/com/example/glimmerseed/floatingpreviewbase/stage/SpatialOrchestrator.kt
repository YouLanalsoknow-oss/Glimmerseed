package com.example.glimmerseed.floatingpreviewbase.stage

import android.content.res.Configuration
import com.example.glimmerseed.editorcore.coord.CoordConverter
import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.stage.PanelSlot
import com.example.glimmerseed.floatingpreviewbase.panel.PanelView

internal class SpatialOrchestrator {

    private data class PanelInfo(
        val view: PanelView,
        val slot: PanelSlot
    )

    private val panels = mutableMapOf<String, PanelInfo>()
    private val sortedIds = mutableListOf<String>()

    val sortedPanelIds: List<String> get() = sortedIds.toList()

    fun addPanel(panelView: PanelView, slot: PanelSlot) {
        panels[slot.panelId] = PanelInfo(panelView, slot)
        updateSort()
    }

    fun removePanel(panelId: String) {
        panels.remove(panelId)
        sortedIds.remove(panelId)
    }

    fun updateSlot(panelId: String, slot: PanelSlot) {
        val info = panels[panelId] ?: return
        panels[panelId] = info.copy(slot = slot)
        updateSort()
    }

    fun getPanelView(panelId: String): PanelView? = panels[panelId]?.view

    fun getSlot(panelId: String): PanelSlot? = panels[panelId]?.slot

    fun layoutPanels(isLandscape: Boolean, rootWidth: Int, rootHeight: Int) {
        for (info in panels.values) {
            val rect = if (isLandscape) info.slot.landscapeRect else info.slot.portraitRect
            val pixelRect = CoordConverter.normalizedToPixel(rect, rootWidth, rootHeight)
            val view = info.view
            view.pixelLeft = pixelRect.x
            view.pixelTop = pixelRect.y
            view.pixelWidth = pixelRect.width
            view.pixelHeight = pixelRect.height
            view.layout(
                pixelRect.x, pixelRect.y,
                pixelRect.x + pixelRect.width,
                pixelRect.y + pixelRect.height
            )
        }
    }

    fun hitTest(x: Float, y: Float): PanelView? {
        for (id in sortedIds) {
            val info = panels[id] ?: continue
            val view = info.view
            if (!view.isPanelActive) continue
            val localX = x - view.pixelLeft
            val localY = y - view.pixelTop
            if (view.isInTouchRegion(localX, localY)) {
                return view
            }
        }
        return null
    }

    private fun updateSort() {
        sortedIds.clear()
        sortedIds.addAll(panels.entries
            .sortedByDescending { it.value.slot.zOrder }
            .map { it.key })
    }
}