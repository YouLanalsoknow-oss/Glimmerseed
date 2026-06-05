package com.example.glimmerseed.editorcore.asset

import kotlinx.serialization.Serializable

@Serializable
data class OfficialAssetManifest(
    val version: String,
    val assets: List<OfficialAssetEntry>
)

@Serializable
data class OfficialAssetEntry(
    val assetId: String,
    val type: String,
    val path: String,
    val displayName: String,
    val thumbnailPath: String? = null,
    val sizeBytes: Long = 0
)

enum class AssetCategory {
    IMAGES,
    PROJECTS,
    CLIPS
}