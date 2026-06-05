package com.example.glimmerseed.editorcore.editor

import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.panel.TouchMode
import com.example.glimmerseed.editorcore.stage.PanelSlot
import com.example.glimmerseed.editorcore.skin.SkinWeights
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f

sealed class EditorAction {
    data class SelectBone(val boneId: Int?) : EditorAction()
    data class MoveBone(val boneId: Int, val delta: Vector3f) : EditorAction()
    data class RotateBone(val boneId: Int, val delta: Quaternionf) : EditorAction()
    data class ScaleBone(val boneId: Int, val delta: Vector3f) : EditorAction()
    data class AddKeyframe(val boneId: Int, val time: Float) : EditorAction()
    data class DeleteKeyframe(val boneId: Int, val time: Float) : EditorAction()
    data class SetCurrentTime(val time: Float) : EditorAction()
    data class SetSelectedFrame(val frame: Int?) : EditorAction()
    data class SetPlaying(val isPlaying: Boolean) : EditorAction()
    data class SetManipulatorMode(val mode: EditorState.ManipulatorMode) : EditorAction()
    data class TranslateViewport(val delta: Vector2f) : EditorAction()
    data class ScaleViewport(val scale: Float, val pivot: Vector2f) : EditorAction()
    data class RotateViewport(val delta: Float) : EditorAction()
    data class ToggleGrid(val show: Boolean) : EditorAction()
    data class SetShowGrid(val show: Boolean) : EditorAction()
    data class SetAnimation(val animation: com.example.glimmerseed.editorcore.animation.AnimationClip?) : EditorAction()
    data class SetSkeleton(val skeleton: com.example.glimmerseed.editorcore.animation.Skeleton?) : EditorAction()
    data class SetMesh(val mesh: com.example.glimmerseed.editorcore.animation.Mesh?) : EditorAction()
    data class ToggleOnionSkin(val enabled: Boolean) : EditorAction()
    data class SetOnionSkinPreviousFrames(val count: Int) : EditorAction()
    data class SetOnionSkinNextFrames(val count: Int) : EditorAction()
    data class SetOnionSkinOpacity(val opacity: Float) : EditorAction()
    data class ToggleSkeletalPreview(val enabled: Boolean) : EditorAction()
    data class AddBone(val parentId: Int?) : EditorAction()
    data class DeleteBone(val boneId: Int) : EditorAction()
    data class DuplicateBone(val boneId: Int) : EditorAction()
    data class MirrorBone(val boneId: Int, val axis: String = "x") : EditorAction()
    data class RenameBone(val boneId: Int, val newName: String) : EditorAction()

    data class AddPanel(val data: PanelData, val slot: PanelSlot) : EditorAction()
    data class RemovePanel(val panelId: String) : EditorAction()
    data class SelectPanel(val panelId: String?) : EditorAction()
    data class UpdatePanelPosition(val panelId: String, val landscapeRect: NormalizedRect, val portraitRect: NormalizedRect) : EditorAction()
    data class UpdatePanelTouchMode(val panelId: String, val touchMode: TouchMode) : EditorAction()
    data class UpdatePanelSlot(val panelId: String, val slot: PanelSlot) : EditorAction()
    data class SetStageEditing(val enabled: Boolean) : EditorAction()
    
    // IK 系统操作
    data class AddIKConstraint(val constraint: com.example.glimmerseed.editorcore.ik.IKConstraint) : EditorAction()
    data class RemoveIKConstraint(val constraintId: Int) : EditorAction()
    data class UpdateIKConstraint(val constraint: com.example.glimmerseed.editorcore.ik.IKConstraint) : EditorAction()
    data class SetIKTargetPosition(val boneId: Int, val position: Vector3f) : EditorAction()
    data class ToggleIKEnabled(val enabled: Boolean) : EditorAction()
    object SolveIK : EditorAction()
    
    // 蒙皮编辑操作
    data class SetSkinWeights(val weights: SkinWeights) : EditorAction()
    object ResetSkinWeights : EditorAction()
    object ToggleWeightPaintMode : EditorAction()
    object SmoothSkinWeights : EditorAction()
    data class PaintWeight(val vertexIndex: Int, val boneId: Int, val weight: Float) : EditorAction()
    data class SetVertexWeight(val vertexIndex: Int, val boneId: Int, val weight: Float) : EditorAction()
    object CopyWeights : EditorAction()
    object PasteWeights : EditorAction()
    object MirrorWeights : EditorAction()
    object ExportWeights : EditorAction()
    object ImportWeights : EditorAction()

    // 姿势快照操作（PoseSnapshot 系统）
    object CaptureSnapshot : EditorAction()
    data class RemoveSnapshot(val snapshotId: String) : EditorAction()
    data class UpdateSnapshot(val snapshotId: String, val timestamp: Float?, val label: String?) : EditorAction()
    data class SetSnapshotTransition(val fromId: String, val toId: String, val mode: com.example.glimmerseed.editorcore.snapshot.InterpolationMode) : EditorAction()
}