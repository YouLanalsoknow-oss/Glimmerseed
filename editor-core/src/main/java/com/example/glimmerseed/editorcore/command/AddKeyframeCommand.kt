package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Keyframe
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Transform
import com.example.glimmerseed.editorcore.editor.EditorState
import org.joml.Quaternionf
import org.joml.Vector3f

class AddKeyframeCommand(
    private val boneId: Int,
    private val time: Float
) : Command {

    private var addedKeyframe: Keyframe? = null

    override fun execute(state: EditorState): EditorState {
        val animation = state.currentAnimation ?: return state
        val skeleton = state.skeleton ?: return state

        val bone = skeleton.bones.find { it.id == boneId } ?: return state

        val currentTransform = Transform.fromMatrix4f(bone.localTransform)
        val keyframe = Keyframe(
            time = time,
            translation = Vector3f(currentTransform.translation),
            rotation = Quaternionf(currentTransform.rotation),
            scale = Vector3f(currentTransform.scale)
        )

        addedKeyframe = keyframe

        val newAnimation = animation.addKeyframe(boneId, keyframe)

        return EditorState(
            skeleton = state.skeleton,
            mesh = state.mesh,
            selectedBoneId = state.selectedBoneId,
            currentTime = state.currentTime,
            selectedFrame = state.selectedFrame,
            currentAnimation = newAnimation,
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
        val animation = state.currentAnimation ?: return state
        val keyframe = addedKeyframe ?: return state

        val currentKeyframes = animation.boneKeyframes[boneId].orEmpty().toMutableList()
        currentKeyframes.removeAll { it.time == keyframe.time }

        val newKeyframes = animation.boneKeyframes.toMutableMap()
        if (currentKeyframes.isEmpty()) {
            newKeyframes.remove(boneId)
        } else {
            newKeyframes[boneId] = currentKeyframes
        }

        val newAnimation = animation.copy(boneKeyframes = newKeyframes)

        return EditorState(
            skeleton = state.skeleton,
            mesh = state.mesh,
            selectedBoneId = state.selectedBoneId,
            currentTime = state.currentTime,
            selectedFrame = state.selectedFrame,
            currentAnimation = newAnimation,
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
