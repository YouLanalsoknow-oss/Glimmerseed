package com.example.glimmerseed.editorcore.ik

import org.joml.Vector3f
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

@Serializable
enum class LimitAxis { X, Y, Z, FREE }

@Serializable
data class SingleAxisLimit(
    val axis: LimitAxis,
    val minAngle: Float,
    val maxAngle: Float
)

@Serializable
data class JointLimit(
    val axes: List<SingleAxisLimit> = emptyList()
)

@Serializable
data class PoleTarget(
    @Contextual val position: Vector3f = Vector3f(),
    val enable: Boolean = true
)

@Serializable
data class IKConstraint(
    val id: Int,
    val targetBone: Int,
    @Contextual val ikTarget: Vector3f = Vector3f(),
    val blendFactor: Float = 1f,
    val boneWeightOffsets: Map<Int, Float> = emptyMap(),
    val priority: Int = 1,
    val jointLimits: Map<Int, JointLimit> = emptyMap(),
    val poleTarget: PoleTarget? = null,
    val enable: Boolean = true
) {
    companion object {
        private var nextId = 1

        fun create(
            targetBone: Int,
            ikTarget: Vector3f = Vector3f(),
            blendFactor: Float = 1f,
            priority: Int = 1
        ): IKConstraint {
            return IKConstraint(
                id = nextId++,
                targetBone = targetBone,
                ikTarget = ikTarget,
                blendFactor = blendFactor,
                priority = priority
            )
        }
    }

    fun createSolver(): IKSolver {
        return FABRIKSolver()
    }
}
