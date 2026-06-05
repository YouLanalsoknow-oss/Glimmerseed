package com.example.glimmerseed.editorcore.stage

import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.panel.TouchMode
import kotlinx.serialization.Serializable

@Serializable
data class StageData(
    val version: Int = 1,
    val panels: List<PanelSlot> = emptyList(),
    val landscape: OrientationLayout = OrientationLayout(),
    val portrait: OrientationLayout = OrientationLayout(),
    val canvasInfo: CanvasInfo? = null
)

@Serializable
data class PanelSlot(
    val panelId: String,
    val zOrder: Int = 0,
    val active: Boolean = true,
    val landscapeRect: NormalizedRect,
    val portraitRect: NormalizedRect,
    val touchMode: TouchMode = TouchMode.PASSTHROUGH
)

@Serializable
data class OrientationLayout(
    val screenWidth: Float = 1f,
    val screenHeight: Float = 1f
)

@Serializable
data class CanvasInfo(
    val designWidth: Int = 0,
    val designHeight: Int = 0,
    val timestamp: Long = 0L
)