package com.example.glimmerseed.editorcore.animation

import com.example.glimmerseed.editorcore.objectpool.ObjectPool
import org.joml.Matrix4f

/**
 * 骨骼类
 * @param id 骨骼ID
 * @param name 骨骼名称
 * @param parentId 父骨骼ID，根骨骼为null
 * @param localTransform 局部变换矩阵
 */
class Bone(
    val id: Int,
    val name: String,
    val parentId: Int? = null,
    val localTransform: Matrix4f = Matrix4f()
) {
    /**
     * 世界变换矩阵缓存
     */
    private var worldTransformCache: Matrix4f? = null

    /**
     * 局部变换版本号，用于检测是否需要重新计算世界变换
     */
    private var localTransformVersion = 0L

    /**
     * 获取世界变换矩阵（带缓存）
     * @param parentWorldTransform 父骨骼的世界变换矩阵
     * @return 当前骨骼的世界变换矩阵
     */
    fun getWorldTransform(parentWorldTransform: Matrix4f?): Matrix4f {
        val matrix = ObjectPool.acquireMatrix4f()
        try {
            if (parentWorldTransform != null) {
                parentWorldTransform.mul(localTransform, matrix)
            } else {
                matrix.set(localTransform)
            }
            return Matrix4f(matrix)
        } finally {
            ObjectPool.releaseMatrix4f(matrix)
        }
    }

    /**
     * 更新局部变换矩阵
     * @param newTransform 新的局部变换矩阵
     */
    fun updateLocalTransform(newTransform: Matrix4f) {
        localTransform.set(newTransform)
        localTransformVersion++
        worldTransformCache = null
    }

    /**
     * 克隆当前骨骼
     * 使用 ObjectPool 复用 Matrix4f 以减少 GC 压力
     */
    fun clone(): Bone {
        val matrix = ObjectPool.acquireMatrix4f()
        matrix.set(localTransform)
        return Bone(
            id = id,
            name = name,
            parentId = parentId,
            localTransform = matrix
        )
    }
}
