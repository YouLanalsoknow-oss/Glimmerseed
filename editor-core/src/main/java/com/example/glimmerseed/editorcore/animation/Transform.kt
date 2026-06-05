package com.example.glimmerseed.editorcore.animation

import com.example.glimmerseed.editorcore.objectpool.ObjectPool
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * 变换数据类，包含平移、旋转和缩放
 * @param translation 平移向量
 * @param rotation 旋转四元数
 * @param scale 缩放向量
 */
data class Transform(
    val translation: Vector3f = Vector3f(),
    val rotation: Quaternionf = Quaternionf(),
    val scale: Vector3f = Vector3f(1f, 1f, 1f)
) {

    /**
     * 将变换转换为Matrix4f
     * @return 变换矩阵
     */
    fun toMatrix4f(): Matrix4f {
        val matrix = ObjectPool.acquireMatrix4f()
        try {
            matrix.identity()
            matrix.translate(translation)
            matrix.rotate(rotation)
            matrix.scale(scale)
            return Matrix4f(matrix)
        } finally {
            ObjectPool.releaseMatrix4f(matrix)
        }
    }

    /**
     * 从Matrix4f创建Transform
     * @param matrix 变换矩阵
     * @return Transform对象
     */
    companion object {
        fun fromMatrix4f(matrix: Matrix4f): Transform {
            val translation = ObjectPool.acquireVector3f()
            val rotation = ObjectPool.acquireQuaternionf()
            val scale = ObjectPool.acquireVector3f()

            try {
                matrix.getTranslation(translation)
                matrix.getNormalizedRotation(rotation)
                matrix.getScale(scale)
                return Transform(
                    translation = Vector3f(translation),
                    rotation = Quaternionf(rotation),
                    scale = Vector3f(scale)
                )
            } finally {
                ObjectPool.releaseVector3f(translation)
                ObjectPool.releaseQuaternionf(rotation)
                ObjectPool.releaseVector3f(scale)
            }
        }
    }
}
