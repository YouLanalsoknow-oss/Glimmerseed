package com.example.glimmerseed.data.repository

import android.content.Context
import com.example.glimmerseed.data.database.AppDatabase
import com.example.glimmerseed.data.entity.CommandHistoryEntity
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 历史记录仓库
 * 负责编辑器历史记录的持久化
 */
class HistoryRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val commandHistoryDao = database.commandHistoryDao()
    private val gson = Gson()

    private var currentSessionId: String = ""

    fun startSession(sessionId: String) {
        currentSessionId = sessionId
    }

    fun getCurrentSessionId(): String = currentSessionId

    suspend fun saveCommand(action: EditorAction) = withContext(Dispatchers.IO) {
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

    suspend fun getHistoryFlow(): Flow<List<CommandHistoryEntity>> = withContext(Dispatchers.IO) {
        commandHistoryDao.getHistoryBySession(currentSessionId)
    }

    suspend fun getHistory(): List<CommandHistoryEntity> = withContext(Dispatchers.IO) {
        commandHistoryDao.getHistoryBySessionSync(currentSessionId)
    }

    suspend fun markAsUndone(entityId: Long) = withContext(Dispatchers.IO) {
        commandHistoryDao.updateUndoneStatus(entityId, true)
    }

    suspend fun markAsRedone(entityId: Long) = withContext(Dispatchers.IO) {
        commandHistoryDao.updateUndoneStatus(entityId, false)
    }

    suspend fun getLastCommand(): CommandHistoryEntity? = withContext(Dispatchers.IO) {
        commandHistoryDao.getLastCommand(currentSessionId)
    }

    suspend fun getLastUndoneCommand(): CommandHistoryEntity? = withContext(Dispatchers.IO) {
        commandHistoryDao.getLastUndoneCommand(currentSessionId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        if (currentSessionId.isNotEmpty()) {
            commandHistoryDao.clearSessionHistory(currentSessionId)
        }
    }

    suspend fun getHistoryCount(): Int = withContext(Dispatchers.IO) {
        commandHistoryDao.getHistoryCount(currentSessionId)
    }

    fun parseAction(entity: CommandHistoryEntity): EditorAction? {
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
        private var INSTANCE: HistoryRepository? = null

        fun getInstance(context: Context): HistoryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = HistoryRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
