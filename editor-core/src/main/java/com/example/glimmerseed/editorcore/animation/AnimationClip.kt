package com.example.glimmerseed.editorcore.animation

/**
 * 动画剪辑类
 * @param name 动画名称
 * @param duration 动画持续时间（秒）
 * @param boneKeyframes 每个骨骼的关键帧列表，key为骨骼ID
 */
data class AnimationClip(
    val name: String,
    val duration: Float,
    val boneKeyframes: Map<Int, List<Keyframe>> = emptyMap()
) {

    /**
     * 获取指定骨骼在指定时间的变换
     * @param boneId 骨骼ID
     * @param time 时间点
     * @return 变换对象
     */
    fun getBoneTransformAtTime(boneId: Int, time: Float): Transform {
        val keyframes = boneKeyframes[boneId] ?: return Transform()

        // 处理边界情况
        if (keyframes.isEmpty()) return Transform()
        if (time <= keyframes.first().time) {
            return Transform(
                translation = keyframes.first().translation,
                rotation = keyframes.first().rotation,
                scale = keyframes.first().scale
            )
        }
        if (time >= keyframes.last().time) {
            return Transform(
                translation = keyframes.last().translation,
                rotation = keyframes.last().rotation,
                scale = keyframes.last().scale
            )
        }

        // 查找包围的两个关键帧
        var prevKeyframe = keyframes.first()
        for (keyframe in keyframes.drop(1)) {
            if (time < keyframe.time) {
                val progress = (time - prevKeyframe.time) / (keyframe.time - prevKeyframe.time)
                return prevKeyframe.interpolate(keyframe, progress)
            }
            prevKeyframe = keyframe
        }

        return Transform(
            translation = keyframes.last().translation,
            rotation = keyframes.last().rotation,
            scale = keyframes.last().scale
        )
    }

    /**
     * 为动画添加关键帧
     * @param boneId 骨骼ID
     * @param keyframe 关键帧
     * @return 新的AnimationClip
     */
    fun addKeyframe(boneId: Int, keyframe: Keyframe): AnimationClip {
        val currentKeyframes = boneKeyframes[boneId].orEmpty().toMutableList()
        val insertIndex = currentKeyframes.indexOfFirst { it.time > keyframe.time }.let {
            if (it == -1) currentKeyframes.size else it
        }
        currentKeyframes.add(insertIndex, keyframe)

        val newKeyframes = boneKeyframes.toMutableMap()
        newKeyframes[boneId] = currentKeyframes

        return copy(boneKeyframes = newKeyframes)
    }
}
