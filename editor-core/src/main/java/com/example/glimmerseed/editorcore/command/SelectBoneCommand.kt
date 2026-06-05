package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState

/**
 * 选中骨骼命令
 */
class SelectBoneCommand(
    private val newBoneId: Int?,
    private val oldBoneId: Int?
) : Command {
    
    override fun execute(state: EditorState): EditorState {
        return state.copy(selectedBoneId = newBoneId)
    }
    
    override fun undo(state: EditorState): EditorState {
        return state.copy(selectedBoneId = oldBoneId)
    }
}
