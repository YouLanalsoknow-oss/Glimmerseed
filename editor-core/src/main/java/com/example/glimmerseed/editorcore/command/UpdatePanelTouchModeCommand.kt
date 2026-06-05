package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.panel.TouchMode
import com.example.glimmerseed.editorcore.panel.VisualLayerData

class UpdatePanelTouchModeCommand(
    private val panelId: String,
    private val newTouchMode: TouchMode
) : Command {

    private var oldTouchMode: TouchMode? = null

    override fun execute(state: EditorState): EditorState {
        val current = state.panelEditing
        val panel = current.panels.find { it.id == panelId } ?: return state

        oldTouchMode = panel.interaction.touchMode

        val updatedPanel = panel.copy(
            interaction = panel.interaction.copy(touchMode = newTouchMode)
        )
        val newPanels = current.panels.map { if (it.id == panelId) updatedPanel else it }
        return state.copy(
            panelEditing = current.copy(panels = newPanels)
        )
    }

    override fun undo(state: EditorState): EditorState {
        val mode = oldTouchMode ?: return state
        val current = state.panelEditing
        val panel = current.panels.find { it.id == panelId } ?: return state
        val updatedPanel = panel.copy(
            interaction = panel.interaction.copy(touchMode = mode)
        )
        val newPanels = current.panels.map { if (it.id == panelId) updatedPanel else it }
        return state.copy(
            panelEditing = current.copy(panels = newPanels)
        )
    }
}