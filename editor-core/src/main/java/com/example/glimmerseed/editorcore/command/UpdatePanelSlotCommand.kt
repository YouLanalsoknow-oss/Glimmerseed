package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.stage.PanelSlot

class UpdatePanelSlotCommand(
    private val panelId: String,
    private val newSlot: PanelSlot
) : Command {

    private var oldSlot: PanelSlot? = null

    override fun execute(state: EditorState): EditorState {
        val current = state.panelEditing
        val existing = current.slots[panelId] ?: return state

        oldSlot = existing

        return state.copy(
            panelEditing = current.copy(slots = current.slots + (panelId to newSlot))
        )
    }

    override fun undo(state: EditorState): EditorState {
        val slot = oldSlot ?: return state
        val current = state.panelEditing
        return state.copy(
            panelEditing = current.copy(slots = current.slots + (panelId to slot))
        )
    }
}