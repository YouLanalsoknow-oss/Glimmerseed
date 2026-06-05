package com.example.glimmerseed.editorcore.panel

import com.example.glimmerseed.editorcore.coord.NormalizedRect
import kotlinx.serialization.Serializable

@Serializable
data class PanelData(
    val id: String,
    val name: String,
    val visual: VisualLayerData,
    val interaction: InteractionLayerData,
    val behavior: BehaviorLayerData
)

@Serializable
data class VisualLayerData(
    val type: VisualType,
    val animationId: String? = null,
    val layers: List<LayerData> = emptyList(),
    val opacity: Float = 1f,
    val visible: Boolean = true
)

@Serializable
enum class VisualType {
    FRAME_ANIMATION,
    SKELETAL_ANIMATION,
    LAYER_RENDERING
}

@Serializable
data class LayerData(
    val asset: AssetRef,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val blending: BlendingMode = BlendingMode.NORMAL,
    val opacity: Float = 1f
)

@Serializable
enum class BlendingMode {
    NORMAL,
    ADDITIVE,
    MULTIPLY
}

@Serializable
data class InteractionLayerData(
    val touchMode: TouchMode = TouchMode.PASSTHROUGH,
    val hitRegions: List<HitRegion> = emptyList()
)

@Serializable
enum class TouchMode {
    PASSTHROUGH,
    BLOCKING,
    REGION_BASED
}

@Serializable
data class HitRegion(
    val normalizedRect: NormalizedRect,
    val mode: TouchMode,
    val gestureTypes: List<GestureType> = emptyList()
)

@Serializable
enum class GestureType {
    TAP,
    LONG_PRESS,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    SWIPE_UP,
    SWIPE_DOWN
}

@Serializable
data class BehaviorLayerData(
    val gestureHandlers: List<GestureHandler> = emptyList(),
    val systemEventHandlers: List<SystemEventHandler> = emptyList(),
    val panelEventHandlers: List<PanelEventHandler> = emptyList()
)