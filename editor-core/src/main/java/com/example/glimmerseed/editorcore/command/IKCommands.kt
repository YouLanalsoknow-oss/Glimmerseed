package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.ik.IKConstraint
import org.joml.Vector3f

class AddIKConstraintCommand(
    private val constraint: IKConstraint
) : Command {

    override fun execute(state: EditorState): EditorState {
        return state.copy(
            ikConstraints = state.ikConstraints + constraint
        )
    }

    override fun undo(state: EditorState): EditorState {
        return state.copy(
            ikConstraints = state.ikConstraints.filter { it.id != constraint.id }
        )
    }
}

class RemoveIKConstraintCommand(
    private val constraintId: Int,
    private var removedConstraint: IKConstraint? = null
) : Command {

    override fun execute(state: EditorState): EditorState {
        val constraint = state.ikConstraints.find { it.id == constraintId }
        removedConstraint = constraint
        return state.copy(
            ikConstraints = state.ikConstraints.filter { it.id != constraintId }
        )
    }

    override fun undo(state: EditorState): EditorState {
        val constraint = removedConstraint ?: return state
        return state.copy(
            ikConstraints = state.ikConstraints + constraint
        )
    }
}

class UpdateIKConstraintCommand(
    private val newConstraint: IKConstraint,
    private var oldConstraint: IKConstraint? = null
) : Command {

    override fun execute(state: EditorState): EditorState {
        val old = state.ikConstraints.find { it.id == newConstraint.id }
        oldConstraint = old
        return state.copy(
            ikConstraints = state.ikConstraints.map {
                if (it.id == newConstraint.id) newConstraint else it
            }
        )
    }

    override fun undo(state: EditorState): EditorState {
        val old = oldConstraint ?: return state
        return state.copy(
            ikConstraints = state.ikConstraints.map {
                if (it.id == old.id) old else it
            }
        )
    }
}

class SetIKTargetPositionCommand(
    private val boneId: Int,
    private val newPosition: Vector3f,
    private var oldPosition: Vector3f? = null
) : Command {

    override fun execute(state: EditorState): EditorState {
        val currentPos = state.ikTargetPositions[boneId]
        oldPosition = currentPos ?: Vector3f()
        return state.copy(
            ikTargetPositions = state.ikTargetPositions + (boneId to newPosition)
        )
    }

    override fun undo(state: EditorState): EditorState {
        val old = oldPosition ?: return state
        return state.copy(
            ikTargetPositions = state.ikTargetPositions + (boneId to old)
        )
    }
}

class SnapToIKCommand(
    private val constraintId: Int,
    private val skeleton: Skeleton,
    private val autoKey: Boolean = false
) : Command {

    override fun execute(state: EditorState): EditorState {
        val constraint = state.ikConstraints.find { it.id == constraintId }
        if (constraint == null) return state

        val targetBone = skeleton.getBoneById(constraint.targetBone)
        if (targetBone == null) return state

        val worldPos = skeleton.getBoneWorldTransform(targetBone.id).getTranslation(Vector3f())

        val updatedConstraint = constraint.copy(ikTarget = worldPos)

        if (constraint.poleTarget == null && skeleton.bones.size >= 3) {
            val chain = buildBoneChain(skeleton, constraint.targetBone)
            if (chain.size >= 3) {
                val midBone = chain[chain.size / 2]
                val midPos = skeleton.getBoneWorldTransform(midBone.id).getTranslation(Vector3f())
                val rootPos = skeleton.getBoneWorldTransform(chain[0].id).getTranslation(Vector3f())
                val endPos = skeleton.getBoneWorldTransform(chain.last().id).getTranslation(Vector3f())

                val direction = Vector3f(endPos).sub(rootPos).normalize()
                val polePos = Vector3f(midPos).add(Vector3f(direction).mul(50f))

                return state.copy(
                    ikConstraints = state.ikConstraints.map {
                        if (it.id == constraintId) {
                            updatedConstraint.copy(
                                poleTarget = com.example.glimmerseed.editorcore.ik.PoleTarget(
                                    position = polePos,
                                    enable = true
                                )
                            )
                        } else it
                    }
                )
            }
        }

        return state.copy(
            ikConstraints = state.ikConstraints.map {
                if (it.id == constraintId) updatedConstraint else it
            }
        )
    }

    override fun undo(state: EditorState): EditorState {
        return state
    }

    private fun buildBoneChain(skeleton: Skeleton, targetBoneId: Int): List<com.example.glimmerseed.editorcore.animation.Bone> {
        val chain = mutableListOf<com.example.glimmerseed.editorcore.animation.Bone>()
        var currentId: Int? = targetBoneId

        while (currentId != null) {
            val bone = skeleton.getBoneById(currentId)
            if (bone == null) break

            chain.add(bone)
            currentId = bone.parentId
        }

        return chain.reversed()
    }
}

class SetBoneWeightOffsetCommand(
    private val constraintId: Int,
    private val boneId: Int,
    private val offset: Float,
    private var oldOffset: Float? = null
) : Command {

    override fun execute(state: EditorState): EditorState {
        return state.copy(
            ikConstraints = state.ikConstraints.map { constraint ->
                if (constraint.id == constraintId) {
                    oldOffset = constraint.boneWeightOffsets[boneId]
                    val newOffsets = constraint.boneWeightOffsets + (boneId to offset)
                    constraint.copy(boneWeightOffsets = newOffsets)
                } else {
                    constraint
                }
            }
        )
    }

    override fun undo(state: EditorState): EditorState {
        val old = oldOffset ?: return state
        return state.copy(
            ikConstraints = state.ikConstraints.map { constraint ->
                if (constraint.id == constraintId) {
                    val newOffsets = constraint.boneWeightOffsets + (boneId to old)
                    constraint.copy(boneWeightOffsets = newOffsets)
                } else {
                    constraint
                }
            }
        )
    }
}

class SetPoleTargetCommand(
    private val constraintId: Int,
    private val position: Vector3f,
    private var oldPosition: Vector3f? = null
) : Command {

    override fun execute(state: EditorState): EditorState {
        return state.copy(
            ikConstraints = state.ikConstraints.map { constraint ->
                if (constraint.id == constraintId) {
                    oldPosition = constraint.poleTarget?.position ?: Vector3f()
                    val newPoleTarget = constraint.poleTarget?.copy(position = position)
                        ?: com.example.glimmerseed.editorcore.ik.PoleTarget(position = position)
                    constraint.copy(poleTarget = newPoleTarget)
                } else {
                    constraint
                }
            }
        )
    }

    override fun undo(state: EditorState): EditorState {
        val old = oldPosition ?: return state
        return state.copy(
            ikConstraints = state.ikConstraints.map { constraint ->
                if (constraint.id == constraintId) {
                    val newPoleTarget = constraint.poleTarget?.copy(position = old)
                    constraint.copy(poleTarget = newPoleTarget)
                } else {
                    constraint
                }
            }
        )
    }
}
