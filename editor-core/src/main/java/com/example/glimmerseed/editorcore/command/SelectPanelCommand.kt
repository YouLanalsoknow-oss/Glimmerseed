package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState

class SelectPanelCommand(
    private val panelId: String?
) : Command {

    private var oldSelectedId: String? = null

    override fun execute(state: EditorState): EditorState {
        oldSelectedId = state.panelEditing.selectedPanelId
        return state.copy(
            panelEditing = state.panelEditing.copy(selectedPanelId = panelId)
        )
    }

    override fun undo(state: EditorState): EditorState {
        return state.copy(
            panelEditing = state.panelEditing.copy(selectedPanelId = oldSelectedId)
        )
    }
}