package com.example.glimmerseed.editorcore.animation

import com.example.glimmerseed.editorcore.objectpool.ObjectPool
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * 关键帧类
 * @param time 关键帧时间
 * @param translation 平移向量
 * @param rotation 旋转四元数
 * @param scale 缩放向量
 */
data class Keyframe(
    val time: Float,
    val translation: Vector3f = Vector3f(),
    val rotation: Quaternionf = Quaternionf(),
    val scale: Vector3f = Vector3f(1f, 1f, 1f)
) {

    /**
     * 与下一个关键帧进行插值
     * @param next 下一个关键帧
     * @param progress 插值进度 (0.0 ~ 1.0)
     * @return 插值后的变换
     */
    fun interpolate(next: Keyframe, progress: Float): Transform {
        val clampedProgress = progress.coerceIn(0f, 1f)

        val translation = ObjectPool.acquireVector3f()
        val rotation = ObjectPool.acquireQuaternionf()
        val scale = ObjectPool.acquireVector3f()

        try {
            this.translation.lerp(next.translation, clampedProgress, translation)
            this.rotation.slerp(next.rotation, clampedProgress, rotation)
            this.scale.lerp(next.scale, clampedProgress, scale)

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
