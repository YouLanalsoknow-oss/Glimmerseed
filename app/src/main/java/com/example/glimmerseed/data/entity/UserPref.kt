package com.example.glimmerseed.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户设置实体类
 */
@Entity(tableName = "user_prefs")
data class UserPref(
    @PrimaryKey
    val key: String,
    val value: String
)
