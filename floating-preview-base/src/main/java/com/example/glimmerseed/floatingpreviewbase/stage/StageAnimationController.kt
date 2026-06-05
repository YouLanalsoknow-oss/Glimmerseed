package com.example.glimmerseed.floatingpreviewbase.stage

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.AnimationPlayer
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.floatingpreviewbase.render.CanvasSkinningRenderer
import java.lang.ref.WeakReference

/**
 * 舞台动画播放控制器
 *
 * 职责：
 * 1. 持有编辑器传入的骨骼/网格/纹理/动画数据
 * 2. 在后台线程调用 AnimationPlayer.evaluateAt() 计算每帧骨骼姿态
 * 3. 将计算结果推送到 PanelRenderer 进行渲染
 *
 * 线程安全：所有 evaluateAt() 调用在 evalThread 上执行，
 * 结果通过 volatile + Handler 回到主线程更新渲染数据
 */
class StageAnimationController {

    private var evalThread: HandlerThread? = null
    private var evalHandler: Handler? = null

    @Volatile
    private var isPlaying = false

    @Volatile
    private var currentTime = 0f

    private val frameDurationMs = 16L // ~60fps

    private var sourceSkeleton: Skeleton? = null
    private var sourceClip: AnimationClip? = null
    private var sourceMesh: Mesh? = null
    /** 使用 WeakReference 避免纹理 Bitmap 导致 Activity 内存泄漏 */
    private var textureRef: WeakReference<Bitmap>? = null

    /** 当前帧的骨骼快照（由evalThread写入，主线程读取） */
    @Volatile
    var currentEvaluatedSkeleton: Skeleton? = null
        private set

    /** 渲染回调：每帧评估完成后通知 PanelRenderer 更新 */
    var onFrameReady: ((CanvasSkinningRenderer.SkinningInput) -> Unit)? = null

    /**
     * 设置动画源数据（从编辑器传入）
     */
    fun setSource(
        skeleton: Skeleton,
        clip: AnimationClip?,
        mesh: Mesh?,
        texture: Bitmap?
    ) {
        sourceSkeleton = skeleton
        sourceClip = clip
        sourceMesh = mesh
        textureRef = if (texture != null && !texture.isRecycled) {
            WeakReference(texture)
        } else null
        // 立即评估一帧作为初始状态
        if (clip != null) {
            currentEvaluatedSkeleton = AnimationPlayer.evaluateAt(clip, 0f, skeleton)
        } else {
            currentEvaluatedSkeleton = skeleton.clone()
        }
        notifyFrame()
    }

    /**
     * 开始播放动画
     */
    fun startPlayback() {
        if (sourceClip == null || sourceSkeleton == null) return
        if (isPlaying) return

        ensureEvalThread()
        isPlaying = true
        currentTime = 0f
        evalHandler?.post(evaluateRunnable)
    }

    /**
     * 停止播放
     */
    fun stopPlayback() {
        isPlaying = false
        evalHandler?.removeCallbacks(evaluateRunnable)
    }

    /**
     * 释放资源
     */
    fun release() {
        stopPlayback()
        evalThread?.quitSafely()
        evalThread = null
        evalHandler = null
        sourceSkeleton = null
        sourceClip = null
        sourceMesh = null
        textureRef = null
        currentEvaluatedSkeleton = null
        onFrameReady = null
    }

    private fun ensureEvalThread() {
        if (evalThread?.isAlive == true) return
        evalThread = HandlerThread("StageAnimEval").apply { start() }
        evalHandler = Handler(evalThread!!.looper)
    }

    private val evaluateRunnable = object : Runnable {
        override fun run() {
            if (!isPlaying) return

            val clip = sourceClip ?: return
            val skeleton = sourceSkeleton ?: return

            // 推进时间
            currentTime += frameDurationMs / 1000f
            if (currentTime >= clip.duration) {
                currentTime -= clip.duration
            }

            // 在后台线程评估骨骼姿态（纯函数，线程安全）
            val evaluated = AnimationPlayer.evaluateAt(clip, currentTime, skeleton)
            currentEvaluatedSkeleton = evaluated

            // 通知主线程更新渲染
            onFrameReady?.let { callback ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    notifyFrame()
                }
            }

            // 安排下一帧
            evalHandler?.postDelayed(this, frameDurationMs)
        }
    }

    private fun notifyFrame() {
        val skel = currentEvaluatedSkeleton ?: return
        val mesh = sourceMesh
        // 安全获取纹理引用（可能已被回收）
        val tex = textureRef?.get()?.takeIf { !it.isRecycled }
        onFrameReady?.invoke(
            CanvasSkinningRenderer.SkinningInput(
                skeleton = skel,
                mesh = mesh ?: Mesh(emptyList(), emptyList()),
                texture = tex
            )
        )
    }
}
