package com.example.glimmerseed.editorcore.ik

import com.example.glimmerseed.editorcore.animation.Skeleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Matrix4f
import org.joml.AxisAngle4f
import java.util.concurrent.ConcurrentHashMap

class IKManager {

    private val constraintIdMap = ConcurrentHashMap<Int, IKConstraint>()
    private val solverCache = ConcurrentHashMap<String, IKSolver>()

    private val computationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun getConstraints(): List<IKConstraint> {
        return constraintIdMap.values.toList()
    }

    fun addConstraint(constraint: IKConstraint) {
        constraintIdMap[constraint.id] = constraint
    }

    fun removeConstraint(id: Int) {
        constraintIdMap.remove(id)
    }

    fun updateConstraint(constraint: IKConstraint) {
        constraintIdMap[constraint.id] = constraint
    }

    fun getConstraint(id: Int): IKConstraint? {
        return constraintIdMap[id]
    }

    fun solveAll(skeleton: Skeleton): IKSolveResult {
        val workingSkeleton = skeleton.clone()
        val constraints = constraintIdMap.values
            .filter { it.enable }
            .sortedBy { it.priority }

        val allBoneTransforms = mutableMapOf<Int, Matrix4f>()
        var allSuccess = true

        for (constraint in constraints) {
            val result = solveConstraint(workingSkeleton, constraint)
            allBoneTransforms.putAll(result.boneTransforms)
            if (!result.success) allSuccess = false
        }

        return IKSolveResult(allBoneTransforms, allSuccess)
    }

    private fun solveConstraint(skeleton: Skeleton, constraint: IKConstraint): IKSolveResult {
        val solver = solverCache.getOrPut("FABRIK") {
            FABRIKSolver()
        }

        return solver.solve(skeleton, constraint)
    }

    fun solveAllAsync(skeleton: Skeleton, onComplete: (IKSolveResult) -> Unit = {}) {
        computationScope.launch {
            val result = solveAll(skeleton)
            onComplete(result)
        }
    }

    fun clear() {
        constraintIdMap.clear()
    }

    fun applyIKBlend(skeleton: Skeleton, time: Float = 0f): IKSolveResult {
        val workingSkeleton = skeleton.clone()
        val constraints = constraintIdMap.values
            .filter { it.enable }
            .sortedBy { it.priority }

        val allBoneTransforms = mutableMapOf<Int, Matrix4f>()
        var allSuccess = true

        for (constraint in constraints) {
            val result = applyConstraintBlend(workingSkeleton, constraint, time)
            allBoneTransforms.putAll(result.boneTransforms)
            if (!result.success) allSuccess = false
        }

        return IKSolveResult(allBoneTransforms, allSuccess)
    }

    private fun applyConstraintBlend(skeleton: Skeleton, constraint: IKConstraint, time: Float): IKSolveResult {
        val baseBlend = constraint.blendFactor

        val chain = buildBoneChain(skeleton, constraint.targetBone)
        if (chain.isEmpty()) return IKSolveResult(emptyMap(), false)

        val solver = FABRIKSolver()
        val ikSkeleton = skeleton.clone()
        val ikResult = solver.solve(ikSkeleton, constraint)

        // 收集混合后的骨骼变换，不直接修改原始骨骼
        val blendedTransforms = mutableMapOf<Int, Matrix4f>()

        for (bone in chain) {
            val boneOff = constraint.boneWeightOffsets.getOrDefault(bone.id, 0f)
            val weff = (baseBlend + boneOff).coerceIn(0f, 1f)

            if (weff <= 0f) continue

            val baseRot = Quaternionf()
            val baseAxisAngle = AxisAngle4f()
            skeleton.getBoneWorldTransform(bone.id).getRotation(baseAxisAngle)
            baseRot.set(baseAxisAngle)

            val ikRot = Quaternionf()
            val ikAxisAngle = AxisAngle4f()
            ikSkeleton.getBoneWorldTransform(bone.id).getRotation(ikAxisAngle)
            ikRot.set(ikAxisAngle)

            val blendedRot = Quaternionf(baseRot)
            blendedRot.slerp(ikRot, weff)

            val localAxisAngle = AxisAngle4f()
            bone.localTransform.getRotation(localAxisAngle)
            val localRot = Quaternionf()
            localRot.set(localAxisAngle)
            localRot.slerp(blendedRot, weff)

            // 创建新的变换矩阵，不修改原始骨骼
            val newTransform = Matrix4f(bone.localTransform)
            val axisAngle = AxisAngle4f(localRot)
            newTransform.rotate(axisAngle)
            blendedTransforms[bone.id] = newTransform
        }

        return IKSolveResult(blendedTransforms, true)
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
