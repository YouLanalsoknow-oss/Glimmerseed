package com.example.glimmerseed.editorcore.editor

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.ik.IKConstraint
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.skin.SkinWeights
import com.example.glimmerseed.editorcore.stage.PanelSlot
import org.joml.Vector2f
import org.joml.Vector3f

data class EditorState(
    val skeleton: Skeleton? = null,
    val mesh: Mesh? = null,
    val skinWeights: SkinWeights = SkinWeights(),
    val selectedBoneId: Int? = null,
    val currentTime: Float = 0f,
    val selectedFrame: Int? = null,
    val currentAnimation: AnimationClip? = null,
    val isPlaying: Boolean = false,
    val viewportTranslation: Vector2f = Vector2f(0f, 0f),
    val viewportScale: Float = 1f,
    val viewportRotation: Float = 0f,
    val manipulatorMode: ManipulatorMode = ManipulatorMode.TRANSLATE,
    val showGrid: Boolean = true,
    val ikConstraints: List<IKConstraint> = emptyList(),
    val ikTargetPositions: Map<Int, Vector3f> = emptyMap(),
    val ikEnabled: Boolean = true,
    val onionSkinEnabled: Boolean = false,
    val onionSkinPreviousFrames: Int = 2,
    val onionSkinNextFrames: Int = 2,
    val onionSkinOpacity: Float = 0.3f,
    val showSkeletalPreview: Boolean = false,
    val isWeightPaintMode: Boolean = false,
    val brushSize: Float = 15f,
    val brushWeight: Float = 1f,
    val panelEditing: PanelEditingState = PanelEditingState()
) {
    enum class ManipulatorMode {
        TRANSLATE, ROTATE, SCALE
    }
}

data class PanelEditingState(
    val panels: List<PanelData> = emptyList(),
    val slots: Map<String, PanelSlot> = emptyMap(),
    val selectedPanelId: String? = null,
    val editingMode: PanelEditingMode = PanelEditingMode.VIEW
)

enum class PanelEditingMode {
    VIEW,
    POSITION,
    TOUCH_CONFIG
}
