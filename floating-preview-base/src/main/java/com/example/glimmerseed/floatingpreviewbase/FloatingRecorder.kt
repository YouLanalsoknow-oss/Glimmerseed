package com.example.glimmerseed.floatingpreviewbase

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * 悬浮窗录制器
 * 支持录制视频、GIF和截图
 */
class FloatingRecorder(private val context: Context) {
    
    /**
     * 录制格式
     */
    enum class RecordFormat {
        MP4,    // MP4视频
        GIF,    // GIF动画
        PNG_SEQUENCE // PNG序列
    }
    
    /**
     * 录制配置
     */
    data class RecordConfig(
        val format: RecordFormat = RecordFormat.MP4,
        val width: Int = 720,
        val height: Int = 480,
        val frameRate: Int = 30,
        val bitRate: Int = 4_000_000,
        val durationSeconds: Float = 10f
    )
    
    /**
     * 录制状态
     */
    enum class RecordState {
        IDLE,           // 空闲
        RECORDING,      // 录制中
        PROCESSING      // 处理中
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    private var currentState = RecordState.IDLE
    private var currentConfig: RecordConfig? = null
    
    private val frameList = mutableListOf<Bitmap>()
    private var startTime = 0L
    
    // 录制监听器
    interface RecorderListener {
        fun onStateChanged(state: RecordState)
        fun onProgress(currentFrame: Int, totalFrames: Int)
        fun onFinished(file: File)
        fun onError(error: String)
    }
    
    private var listener: RecorderListener? = null
    
    fun setListener(listener: RecorderListener) {
        this.listener = listener
    }
    
    /**
     * 开始录制
     */
    fun startRecording(config: RecordConfig) {
        if (currentState != RecordState.IDLE) {
            listener?.onError("Already recording or processing")
            return
        }
        
        currentConfig = config
        currentState = RecordState.RECORDING
        startTime = System.currentTimeMillis()
        frameList.clear()
        
        listener?.onStateChanged(RecordState.RECORDING)
    }
    
    /**
     * 添加帧
     */
    fun addFrame(bitmap: Bitmap) {
        if (currentState != RecordState.RECORDING) return
        
        val config = currentConfig ?: return
        val elapsed = (System.currentTimeMillis() - startTime) / 1000f
        
        if (elapsed >= config.durationSeconds) {
            stopRecording()
            return
        }
        
        frameList.add(Bitmap.createBitmap(bitmap))
        
        val totalFrames = (config.frameRate * config.durationSeconds).toInt()
        if (frameList.size > totalFrames * 2) {
            stopRecording()
            return
        }
        listener?.onProgress(frameList.size, totalFrames)
    }
    
    /**
     * 停止录制并处理
     */
    fun stopRecording() {
        if (currentState != RecordState.RECORDING) return
        
        currentState = RecordState.PROCESSING
        listener?.onStateChanged(RecordState.PROCESSING)
        
        // 在后台处理
        scope.launch {
            try {
                val outputFile = processRecording()
                
                handler.post {
                    currentState = RecordState.IDLE
                    listener?.onStateChanged(RecordState.IDLE)
                    listener?.onFinished(outputFile)
                }
            } catch (e: Exception) {
                handler.post {
                    currentState = RecordState.IDLE
                    listener?.onStateChanged(RecordState.IDLE)
                    listener?.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
    
    /**
     * 处理录制的帧并输出文件
     */
    private fun processRecording(): File {
        val config = currentConfig ?: throw IllegalStateException("No recording config")
        
        // 临时输出目录
        val outputDir = File(context.getExternalFilesDir(null), "recordings")
        outputDir.mkdirs()
        
        val outputFile = when (config.format) {
            RecordFormat.PNG_SEQUENCE -> {
                // 保存PNG序列
                val sequenceDir = File(outputDir, "sequence_${System.currentTimeMillis()}")
                sequenceDir.mkdirs()
                
                frameList.forEachIndexed { index, bitmap ->
                    val frameFile = File(sequenceDir, "frame_${index.toString().padStart(5, '0')}.png")
                    FileOutputStream(frameFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                
                sequenceDir
            }
            RecordFormat.GIF -> {
                // GIF编码（简化实现）
                val gifFile = File(outputDir, "recording_${System.currentTimeMillis()}.gif")
                
                // 仅保存第一帧作为占位
                FileOutputStream(gifFile).use { out ->
                    frameList.firstOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                
                gifFile
            }
            RecordFormat.MP4 -> {
                // MP4编码（简化实现）
                val mp4File = File(outputDir, "recording_${System.currentTimeMillis()}.mp4")
                
                // 仅保存第一帧作为占位
                FileOutputStream(mp4File).use { out ->
                    frameList.firstOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                
                mp4File
            }
        }
        
        return outputFile
    }
    
    /**
     * 取消录制
     */
    fun cancel() {
        if (currentState == RecordState.RECORDING) {
            frameList.clear()
            currentState = RecordState.IDLE
            listener?.onStateChanged(RecordState.IDLE)
        }
    }
    
    /**
     * 截图
     */
    fun takeScreenshot(bitmap: Bitmap): File {
        val outputDir = File(context.getExternalFilesDir(null), "screenshots")
        outputDir.mkdirs()
        
        val screenshotFile = File(outputDir, "screenshot_${System.currentTimeMillis()}.png")
        FileOutputStream(screenshotFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return screenshotFile
    }
    
    /**
     * 获取当前状态
     */
    fun getState(): RecordState = currentState
}
