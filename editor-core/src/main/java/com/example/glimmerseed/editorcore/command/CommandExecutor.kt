package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.editorcore.editor.EditorState

/**
 * 命令执行器
 * 管理命令执行和撤销/重做栈
 * 支持可选的持久化历史记录
 */
class CommandExecutor(
    private val maxHistorySize: Int = 100,
    private val historyManager: HistoryManager? = null
) {
    /** 撤销栈 */
    private val undoStack = mutableListOf<Command>()

    /** 重做栈 */
    private val redoStack = mutableListOf<Command>()

    /**
     * 执行命令
     * @param command 要执行的命令
     * @param currentState 当前编辑器状态
     * @return 执行后的新状态
     */
    fun execute(command: Command, currentState: EditorState): EditorState {
        val newState = command.execute(currentState)

        undoStack.add(command)
        redoStack.clear()

        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }

        return newState
    }

    /**
     * 执行命令并持久化
     * @param command 要执行的命令
     * @param action 对应的编辑器操作
     * @param currentState 当前编辑器状态
     * @return 执行后的新状态
     */
    suspend fun executeAndPersist(command: Command, action: EditorAction, currentState: EditorState): EditorState {
        val newState = execute(command, currentState)

        historyManager?.let { manager ->
            try {
                manager.saveCommand(action)
            } catch (e: Exception) {
                // 忽略持久化错误
            }
        }

        return newState
    }

    /**
     * 撤销上一个命令
     * @param currentState 当前编辑器状态
     * @return 撤销后的新状态，若无命令可撤销则返回原状态
     */
    fun undo(currentState: EditorState): EditorState {
        if (undoStack.isEmpty()) return currentState

        val command = undoStack.removeLast()
        val newState = command.undo(currentState)

        redoStack.add(command)

        return newState
    }

    /**
     * 撤销上一个命令并更新持久化状态
     * @param currentState 当前编辑器状态
     * @return 撤销后的新状态，若无命令可撤销则返回原状态
     */
    suspend fun undoAndPersist(currentState: EditorState): EditorState {
        if (undoStack.isEmpty()) return currentState

        val command = undoStack.removeLast()
        val newState = command.undo(currentState)

        redoStack.add(command)

        historyManager?.let { manager ->
            try {
                val lastUndone = manager.getLastUndoneCommand()
                lastUndone?.let {
                    val actionType = it::class.java.simpleName
                    manager.markAsUndone(actionType, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                // 忽略持久化错误
            }
        }

        return newState
    }

    /**
     * 重做上一个撤销的命令
     * @param currentState 当前编辑器状态
     * @return 重做后的新状态，若无命令可重做则返回原状态
     */
    fun redo(currentState: EditorState): EditorState {
        if (redoStack.isEmpty()) return currentState

        val command = redoStack.removeLast()
        val newState = command.execute(currentState)

        undoStack.add(command)

        return newState
    }

    /**
     * 重做上一个撤销的命令并更新持久化状态
     * @param currentState 当前编辑器状态
     * @return 重做后的新状态，若无命令可重做则返回原状态
     */
    suspend fun redoAndPersist(currentState: EditorState): EditorState {
        if (redoStack.isEmpty()) return currentState

        val command = redoStack.removeLast()
        val newState = command.execute(currentState)

        undoStack.add(command)

        historyManager?.let { manager ->
            try {
                val lastRedone = manager.getLastCommand()
                lastRedone?.let {
                    val actionType = it::class.java.simpleName
                    manager.markAsRedone(actionType, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                // 忽略持久化错误
            }
        }

        return newState
    }

    /**
     * 检查是否可以撤销
     */
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    /**
     * 检查是否可以重做
     */
    fun canRedo(): Boolean = redoStack.isNotEmpty()

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    /**
     * 清空历史记录并同步到持久化层
     */
    suspend fun clearHistoryAndPersist() {
        clearHistory()

        historyManager?.let { manager ->
            try {
                manager.clearHistory()
            } catch (e: Exception) {
                // 忽略持久化错误
            }
        }
    }
}
