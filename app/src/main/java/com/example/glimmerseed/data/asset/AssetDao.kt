package com.example.glimmerseed.data.asset

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert
    suspend fun insert(asset: AssetEntity)

    @Update
    suspend fun update(asset: AssetEntity)

    @Delete
    suspend fun delete(asset: AssetEntity)

    @Query("SELECT * FROM assets ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE assetId = :assetId")
    suspend fun getAssetById(assetId: String): AssetEntity?

    @Query("SELECT * FROM assets WHERE type = :type ORDER BY createdAt DESC")
    fun getAssetsByType(type: String): Flow<List<AssetEntity>>

    @Query("DELETE FROM assets WHERE assetId = :assetId")
    suspend fun deleteById(assetId: String)

    @Query("SELECT COUNT(*) FROM assets")
    suspend fun getAssetCount(): Int
}