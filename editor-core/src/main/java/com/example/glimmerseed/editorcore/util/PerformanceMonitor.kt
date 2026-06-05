package com.example.glimmerseed.editorcore.util

/**
 * 性能监控器
 * 用于追踪渲染帧率、内存使用等性能指标
 */
class PerformanceMonitor {
    
    private var lastFrameTime = 0L
    private var frameCount = 0
    private var fps = 0.0
    private val frameTimes = mutableListOf<Long>()
    
    private var memoryUsage = 0L
    
    /**
     * 开始帧
     */
    fun beginFrame() {
        lastFrameTime = System.currentTimeMillis()
    }
    
    /**
     * 结束帧
     */
    fun endFrame(): Double {
        val currentTime = System.currentTimeMillis()
        val frameTime = currentTime - lastFrameTime
        
        frameTimes.add(frameTime)
        if (frameTimes.size > 60) {
            frameTimes.removeAt(0)
        }
        
        frameCount++
        
        // 每秒计算一次FPS
        if (frameCount % 60 == 0) {
            val avgFrameTime = frameTimes.average()
            fps = 1000.0 / avgFrameTime
        }
        
        return fps
    }
    
    /**
     * 获取当前FPS
     */
    fun getFPS(): Double = fps
    
    /**
     * 更新内存使用情况
     */
    fun updateMemoryUsage() {
        val runtime = Runtime.getRuntime()
        memoryUsage = runtime.totalMemory() - runtime.freeMemory()
    }
    
    /**
     * 获取内存使用（MB）
     */
    fun getMemoryUsageMB(): Double = memoryUsage / (1024.0 * 1024.0)
    
    /**
     * 重置计数器
     */
    fun reset() {
        frameCount = 0
        frameTimes.clear()
        fps = 0.0
        lastFrameTime = System.currentTimeMillis()
    }
}
