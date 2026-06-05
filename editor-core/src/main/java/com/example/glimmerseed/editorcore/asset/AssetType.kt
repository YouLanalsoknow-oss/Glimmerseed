package com.example.glimmerseed.editorcore.asset

import kotlinx.serialization.Serializable

@Serializable
enum class AssetType {
    IMAGE,
    SKELETON_PROJECT,
    ANIMATION_CLIP,
    AUDIO,
    FONT
}

@Serializable
enum class AssetSource {
    USER,
    OFFICIAL
}

@Serializable
enum class FallbackBehavior {
    HIDE,
    PLACEHOLDER
}