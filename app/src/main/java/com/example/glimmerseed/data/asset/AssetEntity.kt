package com.example.glimmerseed.data.asset

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey val assetId: String,
    val type: String,
    val localPath: String,
    val thumbnailPath: String?,
    val createdAt: Long,
    val sizeBytes: Long,
    val displayName: String
)