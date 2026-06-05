package com.example.glimmerseed.floatingpreviewbase.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FpsMonitor {

    companion object {
        private const val UPDATE_INTERVAL_MS = 1000L
        private const val SAMPLE_SIZE = 60
    }

    private val frameTimes = mutableListOf<Long>()
    private var lastTime = 0L
    private var lastUpdateTime = 0L

    private val _fps = MutableStateFlow(60f)
    val fps: StateFlow<Float> = _fps

    private val _isLowFps = MutableStateFlow(false)
    val isLowFps: StateFlow<Boolean> = _isLowFps

    fun onFrameStart() {
        val currentTime = System.nanoTime()
        if (lastTime > 0) {
            val frameTime = (currentTime - lastTime) / 1_000_000f
            frameTimes.add(frameTime.toLong())

            if (frameTimes.size > SAMPLE_SIZE) {
                frameTimes.removeFirst()
            }

            val currentTimeMs = System.currentTimeMillis()
            if (currentTimeMs - lastUpdateTime >= UPDATE_INTERVAL_MS) {
                updateFps()
                lastUpdateTime = currentTimeMs
            }
        }
        lastTime = currentTime
    }

    private fun updateFps() {
        if (frameTimes.isEmpty()) {
            _fps.value = 60f
            return
        }

        val avgFrameTime = frameTimes.average().toFloat()
        val calculatedFps = if (avgFrameTime > 0) 1000f / avgFrameTime else 0f
        _fps.value = calculatedFps
        _isLowFps.value = calculatedFps < 30f
    }

    fun reset() {
        frameTimes.clear()
        lastTime = 0L
        lastUpdateTime = 0L
        _fps.value = 60f
        _isLowFps.value = false
    }

    fun getAverageFrameTime(): Float {
        return if (frameTimes.isEmpty()) 0f else frameTimes.average().toFloat()
    }
}