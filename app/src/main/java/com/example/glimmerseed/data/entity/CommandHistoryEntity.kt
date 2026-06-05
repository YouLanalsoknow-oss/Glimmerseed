package com.example.glimmerseed.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 编辑器历史记录实体
 * 用于持久化存储编辑器的撤销/重做历史
 */
@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val sessionId: String,

    val actionType: String,

    val actionData: String,

    val timestamp: Long = System.currentTimeMillis(),

    val isUndone: Boolean = false
)
