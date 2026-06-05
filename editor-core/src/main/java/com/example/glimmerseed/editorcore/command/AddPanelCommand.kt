package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.stage.PanelSlot

class AddPanelCommand(
    private val data: PanelData,
    private val slot: PanelSlot
) : Command {

    override fun execute(state: EditorState): EditorState {
        val current = state.panelEditing
        val newPanels = current.panels + data
        val newSlots = current.slots + (data.id to slot)
        return state.copy(
            panelEditing = current.copy(
                panels = newPanels,
                slots = newSlots,
                selectedPanelId = data.id
            )
        )
    }

    override fun undo(state: EditorState): EditorState {
        val current = state.panelEditing
        val newPanels = current.panels.filter { it.id != data.id }
        val newSlots = current.slots - data.id
        return state.copy(
            panelEditing = current.copy(
                panels = newPanels,
                slots = newSlots,
                selectedPanelId = if (current.selectedPanelId == data.id) null else current.selectedPanelId
            )
        )
    }
}