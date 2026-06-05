package com.example.glimmerseed.editorcore.ik

import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Skeleton
import org.joml.Vector3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector4f

data class BoneTransformResult(
    val boneId: Int,
    val newLocalTransform: Matrix4f
)

data class IKSolveResult(
    val boneTransforms: Map<Int, Matrix4f>,
    val success: Boolean
)

interface IKSolver {
    fun solve(skeleton: Skeleton, constraint: IKConstraint): IKSolveResult
}

class FABRIKSolver : IKSolver {
    companion object {
        private const val DEFAULT_MAX_ITERATIONS = 20
        private const val DEFAULT_TOLERANCE = 0.001f
        private const val EPSILON = 0.0001f
    }

    override fun solve(skeleton: Skeleton, constraint: IKConstraint): IKSolveResult {
        val chain = buildBoneChain(skeleton, constraint.targetBone)
        if (chain.isEmpty()) return IKSolveResult(emptyMap(), false)

        val iterations = constraint.priority * 10.coerceAtLeast(DEFAULT_MAX_ITERATIONS)
        val tolerance = DEFAULT_TOLERANCE

        val boneLengths = chain.map { bone ->
            val parentId = bone.parentId
            if (parentId != null) {
                val parent = skeleton.getBoneById(parentId)
                if (parent != null) {
                    val parentPos = parent.localTransform.getTranslation(Vector3f())
                    val bonePos = bone.localTransform.getTranslation(Vector3f())
                    parentPos.distance(bonePos)
                } else {
                    0f
                }
            } else {
                0f
            }
        }

        val worldPositions = chain.map { bone ->
            skeleton.getBoneWorldTransform(bone.id).getTranslation(Vector3f())
        }.toMutableList()

        val rootPos = worldPositions[0]
        val targetPos = Vector3f(constraint.ikTarget)

        var converged = false
        for (iter in 0 until iterations) {
            worldPositions[chain.size - 1] = Vector3f(targetPos)

            for (i in chain.size - 2 downTo 0) {
                val dist = worldPositions[i].distance(worldPositions[i + 1])
                if (dist < EPSILON) continue

                val direction = Vector3f(worldPositions[i])
                    .sub(worldPositions[i + 1])
                    .normalize()
                    .mul(boneLengths[i])

                worldPositions[i] = Vector3f(worldPositions[i + 1]).add(direction)
            }

            worldPositions[0] = Vector3f(rootPos)

            for (i in 0 until chain.size - 1) {
                val dist = worldPositions[i].distance(worldPositions[i + 1])
                if (dist < EPSILON) continue

                val direction = Vector3f(worldPositions[i + 1])
                    .sub(worldPositions[i])
                    .normalize()
                    .mul(boneLengths[i])

                worldPositions[i + 1] = Vector3f(worldPositions[i]).add(direction)
            }

            if (constraint.poleTarget?.enable == true) {
                applyPoleConstraint(chain, worldPositions, constraint.poleTarget.position, rootPos)
            }

            val endPos = worldPositions.last()
            if (endPos.distance(targetPos) < tolerance) {
                converged = true
                break
            }
        }

        applyJointLimits(skeleton, chain, worldPositions, constraint)

        // 收集所有骨骼的新的 localTransform 到 Map 中，不修改原始骨骼
        val boneTransforms = mutableMapOf<Int, Matrix4f>()
        for (i in chain.indices) {
            val bone = chain[i]
            val newPos = worldPositions[i]

            val parentId = bone.parentId
            val parentTransform = if (parentId != null) {
                skeleton.getBoneWorldTransform(parentId)
            } else {
                Matrix4f()
            }

            val invParent = Matrix4f(parentTransform).invert()
            val localPos = Vector4f(newPos.x, newPos.y, newPos.z, 1f).mul(invParent)

            val newTransform = Matrix4f(bone.localTransform)
            newTransform.setTranslation(localPos.x, localPos.y, localPos.z)
            boneTransforms[bone.id] = newTransform
        }

        return IKSolveResult(boneTransforms, true)
    }

    private fun buildBoneChain(skeleton: Skeleton, targetBoneId: Int): List<Bone> {
        val chain = mutableListOf<Bone>()
        var currentId: Int? = targetBoneId

        while (currentId != null) {
            val bone = skeleton.getBoneById(currentId)
            if (bone == null) break

            chain.add(bone)
            currentId = bone.parentId
        }

        return chain.reversed()
    }

    private fun applyPoleConstraint(
        chain: List<Bone>,
        positions: MutableList<Vector3f>,
        polePos: Vector3f,
        rootPos: Vector3f
    ) {
        if (chain.size < 3) return

        val startIdx = 0
        val endIdx = chain.size - 1
        val midIdx = (startIdx + endIdx) / 2

        val rootToEnd = Vector3f(positions[endIdx]).sub(rootPos)
        val rootToPole = Vector3f(polePos).sub(rootPos)

        val planeNormal = Vector3f(rootToEnd).cross(rootToPole).normalize()

        if (planeNormal.length() < EPSILON) {
            return
        }

        for (i in startIdx + 1 until endIdx) {
            val rootToPoint = Vector3f(positions[i]).sub(rootPos)
            val projected = Vector3f(rootToPoint).sub(
                Vector3f(planeNormal).mul(rootToPoint.dot(planeNormal))
            ).normalize().mul(rootToPoint.length())

            positions[i] = Vector3f(rootPos).add(projected)
        }
    }

    private fun applyJointLimits(
        skeleton: Skeleton,
        chain: List<Bone>,
        positions: List<Vector3f>,
        constraint: IKConstraint
    ) {
        for (i in chain.indices) {
            val bone = chain[i]
            val limit = constraint.jointLimits[bone.id] ?: continue

            val parentId = bone.parentId
            if (parentId == null) continue

            val parent = skeleton.getBoneById(parentId) ?: continue

            val parentPos = if (i > 0) positions[i - 1] else {
                skeleton.getBoneWorldTransform(parentId).getTranslation(Vector3f())
            }

            val bonePos = positions[i]
            val direction = Vector3f(bonePos).sub(parentPos)

            for (axisLimit in limit.axes) {
                when (axisLimit.axis) {
                    LimitAxis.X -> {
                        val angle = Math.atan2(direction.y.toDouble(), direction.z.toDouble()).toFloat()
                        val clamped = angle.coerceIn(axisLimit.minAngle, axisLimit.maxAngle)
                        val len = direction.length()
                        direction.y = (Math.sin(clamped.toDouble()) * len).toFloat()
                        direction.z = (Math.cos(clamped.toDouble()) * len).toFloat()
                    }
                    LimitAxis.Y -> {
                        val angle = Math.atan2(direction.x.toDouble(), direction.z.toDouble()).toFloat()
                        val clamped = angle.coerceIn(axisLimit.minAngle, axisLimit.maxAngle)
                        val len = direction.length()
                        direction.x = (Math.sin(clamped.toDouble()) * len).toFloat()
                        direction.z = (Math.cos(clamped.toDouble()) * len).toFloat()
                    }
                    LimitAxis.Z -> {
                        val angle = Math.atan2(direction.y.toDouble(), direction.x.toDouble()).toFloat()
                        val clamped = angle.coerceIn(axisLimit.minAngle, axisLimit.maxAngle)
                        val len = direction.length()
                        direction.y = (Math.sin(clamped.toDouble()) * len).toFloat()
                        direction.x = (Math.cos(clamped.toDouble()) * len).toFloat()
                    }
                    LimitAxis.FREE -> continue
                }
            }

            positions.toMutableList()[i] = Vector3f(parentPos).add(direction)
        }
    }
}

class CCDSolver : IKSolver {
    companion object {
        private const val DEFAULT_MAX_ITERATIONS = 30
        private const val DEFAULT_TOLERANCE = 0.001f
    }

    override fun solve(skeleton: Skeleton, constraint: IKConstraint): IKSolveResult {
        val chain = buildBoneChain(skeleton, constraint.targetBone)
        if (chain.isEmpty()) return IKSolveResult(emptyMap(), false)

        val iterations = DEFAULT_MAX_ITERATIONS
        val tolerance = DEFAULT_TOLERANCE
        val targetPos = Vector3f(constraint.ikTarget)

        // 创建骨骼 localTransform 的副本，用于计算
        val boneLocalTransforms = mutableMapOf<Int, Matrix4f>()
        for (bone in chain) {
            boneLocalTransforms[bone.id] = Matrix4f(bone.localTransform)
        }

        for (iter in 0 until iterations) {
            for (i in chain.size - 1 downTo 1) {
                val bone = chain[i]
                val endBone = chain.last()

                // 使用副本计算世界变换
                val endPos = computeWorldPosition(skeleton, chain, boneLocalTransforms, endBone.id)

                if (endPos.distance(targetPos) < tolerance) {
                    return IKSolveResult(boneLocalTransforms, true)
                }

                val bonePos = computeWorldPosition(skeleton, chain, boneLocalTransforms, bone.id)

                val toEnd = Vector3f(endPos).sub(bonePos).normalize()
                val toTarget = Vector3f(targetPos).sub(bonePos).normalize()

                val rotation = Quaternionf().rotationTo(toEnd, toTarget)

                // 获取当前旋转（从副本的本地变换）
                val currentRot = Quaternionf()
                val currentTransform = boneLocalTransforms[bone.id]!!
                currentTransform.getRotation(org.joml.AxisAngle4f()).let { axisAngle ->
                    currentRot.set(axisAngle)
                }
                currentRot.mul(rotation)

                val limit = constraint.jointLimits[bone.id]
                if (limit != null) {
                    clampRotation(currentRot, limit)
                }

                // 更新副本而不是原始骨骼
                val newTransform = Matrix4f()
                newTransform.set(currentRot)
                newTransform.setTranslation(currentTransform.getTranslation(Vector3f()))
                boneLocalTransforms[bone.id] = newTransform
            }

            val endPos = computeWorldPosition(skeleton, chain, boneLocalTransforms, chain.last().id)
            if (endPos.distance(targetPos) < tolerance) {
                break
            }
        }

        return IKSolveResult(boneLocalTransforms, true)
    }

    /**
     * 根据骨骼链和本地变换副本计算指定骨骼的世界位置
     */
    private fun computeWorldPosition(
        skeleton: Skeleton,
        chain: List<Bone>,
        boneLocalTransforms: Map<Int, Matrix4f>,
        targetBoneId: Int
    ): Vector3f {
        val bone = chain.find { it.id == targetBoneId } ?: return Vector3f()

        // 从根到目标骨骼构建变换链
        val worldTransform = Matrix4f()
        var currentId: Int? = targetBoneId

        while (currentId != null) {
            val localTransform = boneLocalTransforms[currentId] ?: skeleton.getBoneById(currentId)?.localTransform ?: return Vector3f()
            worldTransform.mulAffine(localTransform)
            currentId = chain.find { it.id == currentId }?.parentId
        }

        return worldTransform.getTranslation(Vector3f())
    }

    private fun buildBoneChain(skeleton: Skeleton, targetBoneId: Int): List<Bone> {
        val chain = mutableListOf<Bone>()
        var currentId: Int? = targetBoneId

        while (currentId != null) {
            val bone = skeleton.getBoneById(currentId)
            if (bone == null) break

            chain.add(bone)
            currentId = bone.parentId
        }

        return chain.reversed()
    }

    private fun clampRotation(rotation: Quaternionf, limit: JointLimit) {
        val euler = Vector3f()
        rotation.getEulerAnglesXYZ(euler)

        for (axisLimit in limit.axes) {
            when (axisLimit.axis) {
                LimitAxis.X -> euler.x = euler.x.coerceIn(axisLimit.minAngle, axisLimit.maxAngle)
                LimitAxis.Y -> euler.y = euler.y.coerceIn(axisLimit.minAngle, axisLimit.maxAngle)
                LimitAxis.Z -> euler.z = euler.z.coerceIn(axisLimit.minAngle, axisLimit.maxAngle)
                LimitAxis.FREE -> continue
            }
        }

        rotation.rotationXYZ(euler.x, euler.y, euler.z)
    }
}
