package com.example.glimmerseed.editorcore.panel

import kotlinx.serialization.Serializable

@Serializable
data class AssetRef(
    val type: AssetType,
    val localPath: String,
    val fallback: FallbackBehavior = FallbackBehavior.HIDE
)

@Serializable
enum class AssetType {
    IMAGE,
    ANIMATION,
    FONT
}

@Serializable
enum class FallbackBehavior {
    HIDE,
    PLACEHOLDER
}