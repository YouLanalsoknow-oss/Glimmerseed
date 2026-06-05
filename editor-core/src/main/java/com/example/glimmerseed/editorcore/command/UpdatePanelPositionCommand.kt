package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.stage.PanelSlot

class UpdatePanelPositionCommand(
    private val panelId: String,
    private val newLandscapeRect: NormalizedRect,
    private val newPortraitRect: NormalizedRect
) : Command {

    private var oldSlot: PanelSlot? = null

    override fun execute(state: EditorState): EditorState {
        val current = state.panelEditing
        val existing = current.slots[panelId] ?: return state

        oldSlot = existing

        val updated = existing.copy(
            landscapeRect = newLandscapeRect,
            portraitRect = newPortraitRect
        )
        return state.copy(
            panelEditing = current.copy(slots = current.slots + (panelId to updated))
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