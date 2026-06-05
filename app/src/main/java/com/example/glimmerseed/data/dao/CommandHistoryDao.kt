package com.example.glimmerseed.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.glimmerseed.data.entity.CommandHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 命令历史记录 DAO
 */
@Dao
interface CommandHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CommandHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CommandHistoryEntity>)

    @Update
    suspend fun update(entity: CommandHistoryEntity)

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getHistoryBySession(sessionId: String): Flow<List<CommandHistoryEntity>>

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getHistoryBySessionSync(sessionId: String): List<CommandHistoryEntity>

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId AND isUndone = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastCommand(sessionId: String): CommandHistoryEntity?

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId AND isUndone = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastUndoneCommand(sessionId: String): CommandHistoryEntity?

    @Query("UPDATE command_history SET isUndone = :isUndone WHERE id = :id")
    suspend fun updateUndoneStatus(id: Long, isUndone: Boolean)

    @Query("DELETE FROM command_history WHERE sessionId = :sessionId")
    suspend fun clearSessionHistory(sessionId: String)

    @Query("DELETE FROM command_history WHERE sessionId = :sessionId AND isUndone = 1")
    suspend fun clearRedoStack(sessionId: String)

    @Query("SELECT COUNT(*) FROM command_history WHERE sessionId = :sessionId")
    suspend fun getHistoryCount(sessionId: String): Int

    @Query("DELETE FROM command_history WHERE sessionId = :sessionId AND timestamp < :beforeTimestamp")
    suspend fun deleteOldHistory(sessionId: String, beforeTimestamp: Long)
}
