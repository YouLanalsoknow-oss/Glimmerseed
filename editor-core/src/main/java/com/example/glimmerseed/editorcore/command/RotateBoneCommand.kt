package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Transform
import com.example.glimmerseed.editorcore.editor.EditorState
import org.joml.Matrix4f
import org.joml.Quaternionf

class RotateBoneCommand(
    private val boneId: Int,
    private val delta: Quaternionf
) : Command {

    private var originalTransform: Matrix4f? = null

    override fun execute(state: EditorState): EditorState {
        val skeleton = state.skeleton ?: return state

        val newSkeleton = Skeleton(
            bones = skeleton.bones.map { bone ->
                if (bone.id == boneId) {
                    if (originalTransform == null) {
                        originalTransform = Matrix4f(bone.localTransform)
                    }
                    val newBone = bone.clone()
                    val currentTransform = Transform.fromMatrix4f(newBone.localTransform)
                    val newRotation = Quaternionf(currentTransform.rotation).mul(delta)
                    val newTransformData = currentTransform.copy(rotation = newRotation)
                    newBone.updateLocalTransform(newTransformData.toMatrix4f())
                    newBone
                } else {
                    bone.clone()
                }
            }
        )

        return EditorState(
            skeleton = newSkeleton,
            mesh = state.mesh,
            selectedBoneId = state.selectedBoneId,
            currentTime = state.currentTime,
            currentAnimation = state.currentAnimation,
            isPlaying = state.isPlaying,
            viewportTranslation = state.viewportTranslation,
            viewportScale = state.viewportScale,
            viewportRotation = state.viewportRotation,
            manipulatorMode = state.manipulatorMode,
            showGrid = state.showGrid,
            ikConstraints = state.ikConstraints,
            ikTargetPositions = state.ikTargetPositions,
            ikEnabled = state.ikEnabled,
            onionSkinEnabled = state.onionSkinEnabled,
            onionSkinPreviousFrames = state.onionSkinPreviousFrames,
            onionSkinNextFrames = state.onionSkinNextFrames,
            onionSkinOpacity = state.onionSkinOpacity
        )
    }

    override fun undo(state: EditorState): EditorState {
        val skeleton = state.skeleton ?: return state
        val original = originalTransform ?: return state

        val newSkeleton = Skeleton(
            bones = skeleton.bones.map { bone ->
                if (bone.id == boneId) {
                    val newBone = bone.clone()
                    newBone.updateLocalTransform(original)
                    newBone
                } else {
                    bone.clone()
                }
            }
        )

        return EditorState(
            skeleton = newSkeleton,
            mesh = state.mesh,
            selectedBoneId = state.selectedBoneId,
            currentTime = state.currentTime,
            currentAnimation = state.currentAnimation,
            isPlaying = state.isPlaying,
            viewportTranslation = state.viewportTranslation,
            viewportScale = state.viewportScale,
            viewportRotation = state.viewportRotation,
            manipulatorMode = state.manipulatorMode,
            showGrid = state.showGrid,
            ikConstraints = state.ikConstraints,
            ikTargetPositions = state.ikTargetPositions,
            ikEnabled = state.ikEnabled,
            onionSkinEnabled = state.onionSkinEnabled,
            onionSkinPreviousFrames = state.onionSkinPreviousFrames,
            onionSkinNextFrames = state.onionSkinNextFrames,
            onionSkinOpacity = state.onionSkinOpacity
        )
    }
}
