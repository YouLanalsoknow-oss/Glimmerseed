package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.editor.EditorState

class AddBoneCommand(
    private val name: String,
    private val parentId: Int?
) : Command {

    private var addedBoneId: Int? = null
    private var originalSkeleton: Skeleton? = null

    override fun execute(state: EditorState): EditorState {
        val skeleton = state.skeleton ?: return state
        originalSkeleton = skeleton

        val maxId = skeleton.bones.maxOfOrNull { it.id } ?: 0
        val newId = maxId + 1

        val bone = Bone(
            id = newId,
            name = name,
            parentId = parentId,
            localTransform = org.joml.Matrix4f()
        )
        addedBoneId = newId

        val newSkeleton = Skeleton(bones = skeleton.bones + bone)
        newSkeleton.updateAllWorldTransforms()

        return state.copy(skeleton = newSkeleton)
    }

    override fun undo(state: EditorState): EditorState {
        val skeleton = originalSkeleton ?: return state
        return state.copy(skeleton = skeleton)
    }
}