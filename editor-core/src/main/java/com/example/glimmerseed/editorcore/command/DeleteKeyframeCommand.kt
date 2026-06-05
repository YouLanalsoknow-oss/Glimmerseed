package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.Keyframe
import com.example.glimmerseed.editorcore.editor.EditorState

class DeleteKeyframeCommand(
    private val boneId: Int,
    private val time: Float
) : Command {

    private var deletedKeyframe: Keyframe? = null

    override fun execute(state: EditorState): EditorState {
        val animation = state.currentAnimation ?: return state

        val keyframes = animation.boneKeyframes[boneId] ?: return state
        val keyframe = keyframes.find { it.time == time } ?: return state

        deletedKeyframe = keyframe

        val currentKeyframes = keyframes.toMutableList()
        currentKeyframes.removeAll { it.time == time }

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

    override fun undo(state: EditorState): EditorState {
        val animation = state.currentAnimation ?: return state
        val keyframe = deletedKeyframe ?: return state

        val currentKeyframes = animation.boneKeyframes[boneId].orEmpty().toMutableList()

        val insertIndex = currentKeyframes.indexOfFirst { it.time > keyframe.time }.let {
            if (it == -1) currentKeyframes.size else it
        }
        currentKeyframes.add(insertIndex, keyframe)

        val newKeyframes = animation.boneKeyframes.toMutableMap()
        newKeyframes[boneId] = currentKeyframes

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
