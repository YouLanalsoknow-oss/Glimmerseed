package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.editor.EditorAction

/**
 * 历史记录管理器接口
 * 定义历史记录的持久化操作
 */
interface HistoryManager {
    /**
     * 启动新的会话
     */
    fun startSession(sessionId: String)

    /**
     * 获取当前会话ID
     */
    fun getCurrentSessionId(): String

    /**
     * 保存命令到历史记录
     */
    suspend fun saveCommand(action: EditorAction)

    /**
     * 获取最后一个命令
     */
    suspend fun getLastCommand(): EditorAction?

    /**
     * 获取最后一个被撤销的命令
     */
    suspend fun getLastUndoneCommand(): EditorAction?

    /**
     * 标记命令为已撤销
     */
    suspend fun markAsUndone(actionType: String, timestamp: Long)

    /**
     * 标记命令为已重做
     */
    suspend fun markAsRedone(actionType: String, timestamp: Long)

    /**
     * 清空历史记录
     */
    suspend fun clearHistory()

    /**
     * 获取历史记录数量
     */
    suspend fun getHistoryCount(): Int
}
