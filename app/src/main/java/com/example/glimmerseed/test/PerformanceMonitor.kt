package com.example.glimmerseed.test

import android.os.SystemClock

/**
 * 简单的性能监控工具
 */
class PerformanceMonitor {

    private var lastFrameTime = 0L
    private var frameCount = 0
    private var lastFPSUpdate = 0L

    // 性能统计
    var fps = 0
        private set

    var minFrameTime = Long.MAX_VALUE
        private set

    var maxFrameTime = 0L
        private set

    var totalFrames = 0
        private set

    /**
     * 开始测量一帧
     */
    fun beginFrame(): Long {
        return SystemClock.elapsedRealtimeNanos()
    }

    /**
     * 结束测量一帧
     */
    fun endFrame(beginTime: Long) {
        val currentTime = SystemClock.elapsedRealtimeNanos()
        val frameTime = currentTime - beginTime

        frameCount++
        totalFrames++

        // 更新帧时间统计
        if (frameTime < minFrameTime) {
            minFrameTime = frameTime
        }
        if (frameTime > maxFrameTime) {
            maxFrameTime = frameTime
        }

        // 每秒更新一次FPS
        if (currentTime - lastFPSUpdate >= 1_000_000_000L) {
            fps = frameCount
            frameCount = 0
            lastFPSUpdate = currentTime
        }
    }

    /**
     * 重置统计
     */
    fun reset() {
        frameCount = 0
        fps = 0
        minFrameTime = Long.MAX_VALUE
        maxFrameTime = 0
        totalFrames = 0
        lastFPSUpdate = SystemClock.elapsedRealtimeNanos()
    }

    /**
     * 获取统计信息
     */
    fun getStats(): String {
        return """
            FPS: $fps
            Frame Time:
              Min: ${(minFrameTime / 1_000_000.0)} ms
              Max: ${(maxFrameTime / 1_000_000.0)} ms
            Total Frames: $totalFrames
        """.trimIndent()
    }
}
