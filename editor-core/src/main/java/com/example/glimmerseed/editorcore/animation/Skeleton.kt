package com.example.glimmerseed.editorcore.animation

import com.example.glimmerseed.editorcore.objectpool.ObjectPool
import org.joml.Matrix4f

/**
 * 骨骼类
 * @param bones 骨骼列表
 */
class Skeleton(
    val bones: List<Bone>
) {
    /**
     * 骨骼ID到骨骼的映射
     */
    val boneIdMap: Map<Int, Bone> = bones.associateBy { it.id }

    /**
     * 根骨骼ID
     */
    val rootBoneId: Int? = bones.find { it.parentId == null }?.id

    /**
     * 世界变换矩阵缓存
     */
    private val worldTransformCache = mutableMapOf<Int, Matrix4f>()

    /**
     * 更新所有骨骼的世界变换矩阵
     */
    fun updateAllWorldTransforms() {
        worldTransformCache.clear()
        rootBoneId?.let { rootId ->
            updateBoneWorldTransform(rootId, null)
        }
    }

    /**
     * 递归更新单个骨骼的世界变换
     * @param boneId 骨骼ID
     * @param parentWorldTransform 父骨骼的世界变换
     */
    private fun updateBoneWorldTransform(boneId: Int, parentWorldTransform: Matrix4f?) {
        val bone = boneIdMap[boneId] ?: return
        val worldTransform = bone.getWorldTransform(parentWorldTransform)
        worldTransformCache[boneId] = worldTransform

        bones.filter { it.parentId == boneId }.forEach { child ->
            updateBoneWorldTransform(child.id, worldTransform)
        }
    }

    /**
     * 根据ID获取骨骼
     * @param id 骨骼ID
     * @return 骨骼对象，找不到返回null
     */
    fun getBoneById(id: Int): Bone? = boneIdMap[id]

    /**
     * 获取指定骨骼的世界变换矩阵
     * @param boneId 骨骼ID
     * @return 世界变换矩阵，找不到返回单位矩阵
     */
    fun getBoneWorldTransform(boneId: Int): Matrix4f {
        return worldTransformCache[boneId] ?: Matrix4f().identity()
    }

    /**
     * 根据索引获取骨骼的世界变换矩阵
     * @param index 骨骼索引（0-based）
     * @return 世界变换矩阵
     */
    fun getBoneWorldTransformByIndex(index: Int): Matrix4f {
        return if (index in bones.indices) {
            getBoneWorldTransform(bones[index].id)
        } else {
            Matrix4f().identity()
        }
    }

    /**
     * 克隆当前骨骼
     */
    fun clone(): Skeleton {
        val clonedBones = bones.map { it.clone() }
        return Skeleton(clonedBones)
    }

    /**
     * 清理资源
     */
    fun clear() {
        worldTransformCache.clear()
    }
}
