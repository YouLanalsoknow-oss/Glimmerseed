package com.example.glimmerseed.floatingpreviewbase.service

import android.content.res.Configuration
import android.graphics.Point
import com.example.glimmerseed.editorcore.panel.BehaviorAction
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.stage.PanelSlot
import com.example.glimmerseed.floatingpreviewbase.render.PanelRenderer
import com.example.glimmerseed.floatingpreviewbase.stage.StageEngine

internal class PanelManager(private val service: StageService) {

    val engine: StageEngine = StageEngine(service)

    /** 面板渲染器（含动画播放控制） */
    val panelRenderer = PanelRenderer()

    private var screenWidthPx = 0
    private var screenHeightPx = 0

    init {
        updateScreenSize()
        setupCallbacks()
    }

    private fun setupCallbacks() {
        engine.setOnPanelAdded { panelView ->
            service.rootView?.addView(panelView)
            service.rootView?.requestLayout()
        }
        engine.setOnPanelRemoved { panelView ->
            service.rootView?.removeView(panelView)
        }
        engine.setOnNeedsTouchChanged { needsTouch ->
            service.onTouchModeChanged(needsTouch)
        }
    }

    fun addPanel(data: PanelData, slot: PanelSlot) {
        val view = engine.addPanel(data, slot)
        view.setActionExecutor { action ->
            executeAction(action)
        }
        engine.layoutPanels(isLandscape, screenWidthPx, screenHeightPx)
    }

    fun removePanel(panelId: String) {
        engine.removePanel(panelId)
    }

    fun setPanelActive(panelId: String, active: Boolean) {
        engine.setPanelActive(panelId, active)
        engine.layoutPanels(isLandscape, screenWidthPx, screenHeightPx)
    }

    fun layoutPanels(rootWidth: Int, rootHeight: Int) {
        screenWidthPx = rootWidth
        screenHeightPx = rootHeight
        engine.layoutPanels(isLandscape, rootWidth, rootHeight)
    }

    fun onConfigurationChanged() {
        updateScreenSize()
        service.rootView?.requestLayout()
    }

    fun onScreenOff() {
        engine.pause()
    }

    fun onScreenOn() {
        engine.resume()
    }

    fun destroy() {
        panelRenderer.destroy()
        engine.destroy()
    }

    private fun executeAction(action: BehaviorAction) {
        when (action) {
            is BehaviorAction.ToggleVisibility -> {
                val newActive = !engine.stateOrchestrator.isActive(action.panelId)
                setPanelActive(action.panelId, newActive)
            }
            is BehaviorAction.PlayAnimation -> {
            }
            is BehaviorAction.SendEvent -> {
                engine.sendPanelEvent(
                    com.example.glimmerseed.editorcore.event.PanelEvent(
                        eventType = action.eventName,
                        sourcePanelId = "",
                        targetPanelId = null,
                        payload = action.payload
                    )
                )
            }
            is BehaviorAction.SetTouchMode -> {
                // Phase 3: update panel touch mode via editor
            }
            is BehaviorAction.NoOp -> {
            }
        }
    }

    private fun updateScreenSize() {
        service.windowManager?.let { wm ->
            val display = wm.defaultDisplay
            val point = Point()
            display.getRealSize(point)
            screenWidthPx = point.x
            screenHeightPx = point.y
        }
    }

    private val isLandscape: Boolean
        get() = service.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}