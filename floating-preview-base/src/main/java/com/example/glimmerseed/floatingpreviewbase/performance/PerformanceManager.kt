package com.example.glimmerseed.floatingpreviewbase.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class PerformanceManager(
    private val fpsMonitor: FpsMonitor,
    private val maxActivePanels: Int = 5,
    private val lowFpsThreshold: Float = 30f
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var panelCount = 0
    private var currentQualityLevel = QualityLevel.HIGH
    private var isDegraded = false

    init {
        startMonitoring()
    }

    fun setPanelCount(count: Int) {
        panelCount = count
        checkPanelLimit()
    }

    fun getCurrentQualityLevel(): QualityLevel {
        return currentQualityLevel
    }

    fun isPerformanceDegraded(): Boolean {
        return isDegraded
    }

    private fun startMonitoring() {
        scope.launch {
            fpsMonitor.isLowFps.collectLatest { isLow ->
                if (isLow) {
                    triggerDegradation()
                } else {
                    restorePerformance()
                }
            }
        }
    }

    private fun checkPanelLimit() {
        if (panelCount > maxActivePanels) {
        }
    }

    private fun triggerDegradation() {
        if (isDegraded) return

        isDegraded = true
        val previousLevel = currentQualityLevel

        currentQualityLevel = when (currentQualityLevel) {
            QualityLevel.HIGH -> QualityLevel.MEDIUM
            QualityLevel.MEDIUM -> QualityLevel.LOW
            QualityLevel.LOW -> QualityLevel.LOW
        }

        if (currentQualityLevel != previousLevel) {
            onQualityChanged(currentQualityLevel)
        }
    }

    private fun restorePerformance() {
        if (!isDegraded) return

        scope.launch {
            delay(3000)
            if (!fpsMonitor.isLowFps.value) {
                isDegraded = false
                val previousLevel = currentQualityLevel

                currentQualityLevel = when (currentQualityLevel) {
                    QualityLevel.LOW -> QualityLevel.MEDIUM
                    QualityLevel.MEDIUM -> QualityLevel.HIGH
                    QualityLevel.HIGH -> QualityLevel.HIGH
                }

                if (currentQualityLevel != previousLevel) {
                    onQualityChanged(currentQualityLevel)
                }
            }
        }
    }

    private fun onQualityChanged(level: QualityLevel) {
    }

    fun getFrameIntervalMs(): Long {
        return when (currentQualityLevel) {
            QualityLevel.HIGH -> 16
            QualityLevel.MEDIUM -> 33
            QualityLevel.LOW -> 50
        }
    }

    fun shouldSkipFrame(): Boolean {
        return currentQualityLevel != QualityLevel.HIGH
    }

    fun release() {
        scope.cancel()
    }

    enum class QualityLevel {
        HIGH,
        MEDIUM,
        LOW
    }
}