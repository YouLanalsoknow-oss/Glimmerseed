package com.example.glimmerseed.editorcore.asset

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

abstract class AssetManager(context: Context) {

    abstract suspend fun importAsset(uri: String): AssetRef?

    abstract suspend fun resolveAsset(assetId: String, source: AssetSource): AssetRef?

    abstract suspend fun deleteAsset(assetId: String): Boolean

    abstract suspend fun loadBitmap(assetRef: AssetRef): Bitmap?

    abstract suspend fun getAssetThumbnail(assetId: String): Bitmap?

    abstract fun observeAllAssets(): Flow<List<AssetDisplayItem>>

    abstract fun observeUserAssets(): Flow<List<AssetDisplayItem>>

    abstract suspend fun getAssetById(assetId: String): AssetDisplayItem?

    data class AssetDisplayItem(
        val assetId: String,
        val type: AssetType,
        val source: AssetSource,
        val displayName: String,
        val thumbnailPath: String?,
        val createdAt: Long,
        val sizeBytes: Long
    )
}