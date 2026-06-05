package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.editor.EditorState

class DeleteBoneCommand(
    private val boneId: Int
) : Command {

    private var removedBone: Bone? = null
    private var originalSkeleton: Skeleton? = null

    override fun execute(state: EditorState): EditorState {
        val skeleton = state.skeleton ?: return state
        val bone = skeleton.bones.find { it.id == boneId } ?: return state

        originalSkeleton = skeleton
        removedBone = bone

        val idsToRemove = collectDescendantIds(skeleton, boneId) + boneId
        val newBones = skeleton.bones.filter { it.id !in idsToRemove }

        val newSkeleton = Skeleton(bones = newBones)
        newSkeleton.updateAllWorldTransforms()

        return state.copy(
            skeleton = newSkeleton,
            selectedBoneId = if (state.selectedBoneId == boneId) null else state.selectedBoneId
        )
    }

    override fun undo(state: EditorState): EditorState {
        val skeleton = originalSkeleton ?: return state
        return state.copy(skeleton = skeleton)
    }

    private fun collectDescendantIds(skeleton: Skeleton, parentId: Int): Set<Int> {
        val children = skeleton.bones.filter { it.parentId == parentId }
        return children.flatMap { child ->
            setOf(child.id) + collectDescendantIds(skeleton, child.id)
        }.toSet()
    }
}