package com.example.glimmerseed.editorcore.animation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.joml.Matrix4f

/**
 * 动画播放器
 * @param skeleton 要播放动画的骨骼
 */
class AnimationPlayer(
    private val skeleton: Skeleton
) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())
    private var animationJob: Job? = null
    private val currentSkeleton = skeleton.clone()

    /**
     * 播放状态
     */
    @Volatile
    var isPlaying: Boolean = false
        private set

    /**
     * 当前播放时间
     */
    @Volatile
    var currentTime: Float = 0f
        private set

    /**
     * 播放速度
     */
    @Volatile
    var playbackSpeed: Float = 1f

    /**
     * 是否循环播放
     */
    @Volatile
    var isLooping: Boolean = true

    /**
     * 当前播放的动画
     */
    @Volatile
    var currentClip: AnimationClip? = null
        private set

    private val _onAnimationUpdate = MutableStateFlow(currentSkeleton.clone())
    val onAnimationUpdate: StateFlow<Skeleton> = _onAnimationUpdate.asStateFlow()

    /**
     * 开始播放动画
     * @param clip 要播放的动画剪辑
     */
    fun play(clip: AnimationClip) {
        stop()
        currentClip = clip
        currentTime = 0f
        isPlaying = true
        startAnimationLoop()
    }

    /**
     * 暂停播放
     */
    fun pause() {
        isPlaying = false
    }

    /**
     * 恢复播放
     */
    fun resume() {
        if (currentClip != null && !isPlaying) {
            isPlaying = true
            startAnimationLoop()
        }
    }

    /**
     * 停止播放并重置
     */
    fun stop() {
        animationJob?.cancel()
        animationJob = null
        isPlaying = false
        currentTime = 0f
    }

    /**
     * 跳转到指定时间
     * @param time 目标时间（秒）
     */
    fun seekTo(time: Float) {
        currentClip?.let { clip ->
            currentTime = time.coerceIn(0f, clip.duration)
            updateSkeletonState()
        }
    }

    /**
     * 启动动画循环
     */
    private fun startAnimationLoop() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            val frameDuration = 16L // 约60fps
            while (isPlaying && currentClip != null) {
                val clip = currentClip ?: break
                currentTime += frameDuration / 1000f * playbackSpeed

                // 处理循环
                if (currentTime >= clip.duration) {
                    if (isLooping) {
                        currentTime -= clip.duration
                    } else {
                        stop()
                        break
                    }
                }

                updateSkeletonState()
                delay(frameDuration)
            }
        }
    }

    /**
     * 更新骨骼状态
     */
    private fun updateSkeletonState() {
        currentClip?.let { clip ->
            skeleton.bones.forEach { bone ->
                val transform = clip.getBoneTransformAtTime(bone.id, currentTime)
                val matrix = transform.toMatrix4f()
                val currentBone = currentSkeleton.getBoneById(bone.id)
                currentBone?.updateLocalTransform(matrix)
            }
            currentSkeleton.updateAllWorldTransforms()
            _onAnimationUpdate.value = currentSkeleton.clone()
        }
    }

    companion object {
        /**
         * 在指定时间计算骨骼状态
         *
         * 此函数无状态，线程安全
         * 可在任意线程调用（GL线程/Default线程）
         * 不得在函数内访问或修改任何成员变量
         *
         * @param clip 动画剪辑
         * @param time 目标时间（秒），会在 [0, clip.duration] 范围内钳制
         * @param skeleton 来源骨骼（不会被修改，仅读取其结构和bind pose）
         * @return 指定时间下的骨骼状态（新创建的克隆，独立于输入）
         */
        fun evaluateAt(clip: AnimationClip, time: Float, skeleton: Skeleton): Skeleton {
            val result = skeleton.clone()
            val clampedTime = time.coerceIn(0f, clip.duration)
            skeleton.bones.forEach { bone ->
                val transform = clip.getBoneTransformAtTime(bone.id, clampedTime)
                val matrix = transform.toMatrix4f()
                val resultBone = result.getBoneById(bone.id)
                resultBone?.updateLocalTransform(matrix)
            }
            result.updateAllWorldTransforms()
            return result
        }
    }
}
