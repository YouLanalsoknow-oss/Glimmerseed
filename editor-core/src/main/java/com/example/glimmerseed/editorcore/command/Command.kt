package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorState

/**
 * 命令接口
 * 定义所有可撤销操作的规范
 */
interface Command {
    /**
     * 执行命令
     * @param state 当前编辑器状态
     * @return 执行后的新状态
     */
    fun execute(state: EditorState): EditorState
    
    /**
     * 撤销命令
     * @param state 当前编辑器状态
     * @return 撤销后的新状态
     */
    fun undo(state: EditorState): EditorState
}
