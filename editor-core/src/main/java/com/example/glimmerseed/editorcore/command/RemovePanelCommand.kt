package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.stage.PanelSlot

class RemovePanelCommand(
    private val panelId: String
) : Command {

    private var removedData: PanelData? = null
    private var removedSlot: PanelSlot? = null

    override fun execute(state: EditorState): EditorState {
        val current = state.panelEditing
        val panel = current.panels.find { it.id == panelId } ?: return state
        val slot = current.slots[panelId] ?: return state

        removedData = panel
        removedSlot = slot

        val newPanels = current.panels.filter { it.id != panelId }
        val newSlots = current.slots - panelId
        return state.copy(
            panelEditing = current.copy(
                panels = newPanels,
                slots = newSlots,
                selectedPanelId = if (current.selectedPanelId == panelId) null else current.selectedPanelId
            )
        )
    }

    override fun undo(state: EditorState): EditorState {
        val data = removedData ?: return state
        val slot = removedSlot ?: return state
        val current = state.panelEditing
        val newPanels = current.panels + data
        val newSlots = current.slots + (panelId to slot)
        return state.copy(
            panelEditing = current.copy(panels = newPanels, slots = newSlots)
        )
    }
}