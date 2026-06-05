package com.example.glimmerseed.app.data.repository

import android.content.Context
import com.example.glimmerseed.data.database.AppDatabase
import com.example.glimmerseed.data.entity.CommandHistoryEntity
import com.example.glimmerseed.editorcore.command.HistoryManager
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 历史记录管理器实现
 * 使用 Room 数据库持久化历史记录
 */
class HistoryManagerImpl(context: Context) : HistoryManager {

    private val database = AppDatabase.getInstance(context)
    private val commandHistoryDao = database.commandHistoryDao()
    private val gson = Gson()

    private var currentSessionId: String = ""

    override fun startSession(sessionId: String) {
        currentSessionId = sessionId
    }

    override fun getCurrentSessionId(): String = currentSessionId

    override suspend fun saveCommand(action: EditorAction) = withContext(Dispatchers.IO) {
        if (currentSessionId.isEmpty()) return@withContext

        val actionType = action::class.java.simpleName
        val actionData = gson.toJson(action)

        val entity = CommandHistoryEntity(
            sessionId = currentSessionId,
            actionType = actionType,
            actionData = actionData,
            timestamp = System.currentTimeMillis(),
            isUndone = false
        )

        commandHistoryDao.insert(entity)
        commandHistoryDao.clearRedoStack(currentSessionId)
    }

    override suspend fun getLastCommand(): EditorAction? = withContext(Dispatchers.IO) {
        if (currentSessionId.isEmpty()) return@withContext null

        val entity = commandHistoryDao.getLastCommand(currentSessionId)
        entity?.let { parseAction(it) }
    }

    override suspend fun getLastUndoneCommand(): EditorAction? = withContext(Dispatchers.IO) {
        if (currentSessionId.isEmpty()) return@withContext null

        val entity = commandHistoryDao.getLastUndoneCommand(currentSessionId)
        entity?.let { parseAction(it) }
    }

    override suspend fun markAsUndone(actionType: String, timestamp: Long): Unit = withContext(Dispatchers.IO) {
        if (currentSessionId.isEmpty()) return@withContext Unit

        val entity = commandHistoryDao.getHistoryBySessionSync(currentSessionId)
            .firstOrNull { it.actionType == actionType && it.timestamp == timestamp && !it.isUndone }

        entity?.let {
            commandHistoryDao.updateUndoneStatus(it.id, true)
        }
        Unit
    }

    override suspend fun markAsRedone(actionType: String, timestamp: Long): Unit = withContext(Dispatchers.IO) {
        if (currentSessionId.isEmpty()) return@withContext Unit

        val entity = commandHistoryDao.getHistoryBySessionSync(currentSessionId)
            .firstOrNull { it.actionType == actionType && it.timestamp == timestamp && it.isUndone }

        entity?.let {
            commandHistoryDao.updateUndoneStatus(it.id, false)
        }
        Unit
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        if (currentSessionId.isNotEmpty()) {
            commandHistoryDao.clearSessionHistory(currentSessionId)
        }
    }

    override suspend fun getHistoryCount(): Int = withContext(Dispatchers.IO) {
        if (currentSessionId.isEmpty()) return@withContext 0
        commandHistoryDao.getHistoryCount(currentSessionId)
    }

    private fun parseAction(entity: CommandHistoryEntity): EditorAction? {
        return try {
            val className = "com.example.glimmerseed.editorcore.editor.EditorAction\$${entity.actionType}"
            val clazz = Class.forName(className)
            gson.fromJson(entity.actionData, clazz) as? EditorAction
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HistoryManagerImpl? = null

        fun getInstance(context: Context): HistoryManagerImpl {
            return INSTANCE ?: synchronized(this) {
                val instance = HistoryManagerImpl(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
