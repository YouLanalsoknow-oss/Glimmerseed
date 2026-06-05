package com.example.glimmerseed.editorcore.asset

import kotlinx.serialization.Serializable

@Serializable
data class AssetRef(
    val assetId: String,
    val type: AssetType,
    val source: AssetSource,
    @Serializable(with = PathSerializer::class)
    val localPath: String,
    val fallback: FallbackBehavior = FallbackBehavior.HIDE
) {
    companion object {
        fun createPlaceholder(type: AssetType): AssetRef {
            return AssetRef(
                assetId = "",
                type = type,
                source = AssetSource.USER,
                localPath = "",
                fallback = FallbackBehavior.PLACEHOLDER
            )
        }
    }
}