package com.example.glimmerseed.floatingpreviewbase.stage

internal class StateOrchestrator {

    private data class PanelState(
        var active: Boolean = true,
        var paused: Boolean = false
    )

    private val panelStates = mutableMapOf<String, PanelState>()
    private var isPaused = false

    fun registerPanel(panelId: String, active: Boolean) {
        panelStates[panelId] = PanelState(active = active)
    }

    fun unregisterPanel(panelId: String) {
        panelStates.remove(panelId)
    }

    fun isActive(panelId: String): Boolean {
        return panelStates[panelId]?.active ?: false
    }

    fun setActive(panelId: String, active: Boolean) {
        panelStates[panelId]?.active = active
    }

    fun pause() {
        isPaused = true
        for (state in panelStates.values) {
            state.paused = true
        }
    }

    fun resume() {
        isPaused = false
        for (state in panelStates.values) {
            state.paused = false
        }
    }

    val isStagePaused: Boolean get() = isPaused

    fun hasActivePanels(): Boolean {
        return panelStates.values.any { it.active && !it.paused }
    }

    fun reset() {
        panelStates.clear()
        isPaused = false
    }
}