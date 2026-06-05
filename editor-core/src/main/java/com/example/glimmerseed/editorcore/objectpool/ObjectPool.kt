package com.example.glimmerseed.editorcore.objectpool

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * 对象池管理器，用于复用频繁创建的JOML对象以减少GC压力
 */
object ObjectPool {

    private val vector3fPool = mutableListOf<Vector3f>()
    private val quaternionfPool = mutableListOf<Quaternionf>()
    private val matrix4fPool = mutableListOf<Matrix4f>()

    private const val MAX_POOL_SIZE = 100

    /**
     * 从池中获取或创建一个Vector3f
     */
    fun acquireVector3f(): Vector3f {
        return synchronized(vector3fPool) {
            vector3fPool.removeFirstOrNull() ?: Vector3f()
        }
    }

    /**
     * 将Vector3f归还到池中
     */
    fun releaseVector3f(vector: Vector3f) {
        synchronized(vector3fPool) {
            if (vector3fPool.size < MAX_POOL_SIZE) {
                vector.zero()
                vector3fPool.add(vector)
            }
        }
    }

    /**
     * 从池中获取或创建一个Quaternionf
     */
    fun acquireQuaternionf(): Quaternionf {
        return synchronized(quaternionfPool) {
            quaternionfPool.removeFirstOrNull() ?: Quaternionf()
        }
    }

    /**
     * 将Quaternionf归还到池中
     */
    fun releaseQuaternionf(quaternion: Quaternionf) {
        synchronized(quaternionfPool) {
            if (quaternionfPool.size < MAX_POOL_SIZE) {
                quaternion.identity()
                quaternionfPool.add(quaternion)
            }
        }
    }

    /**
     * 从池中获取或创建一个Matrix4f
     */
    fun acquireMatrix4f(): Matrix4f {
        return synchronized(matrix4fPool) {
            matrix4fPool.removeFirstOrNull() ?: Matrix4f()
        }
    }

    /**
     * 将Matrix4f归还到池中
     */
    fun releaseMatrix4f(matrix: Matrix4f) {
        synchronized(matrix4fPool) {
            if (matrix4fPool.size < MAX_POOL_SIZE) {
                matrix.identity()
                matrix4fPool.add(matrix)
            }
        }
    }

    /**
     * 清空所有池
     */
    fun clear() {
        synchronized(vector3fPool) { vector3fPool.clear() }
        synchronized(quaternionfPool) { quaternionfPool.clear() }
        synchronized(matrix4fPool) { matrix4fPool.clear() }
    }
}
