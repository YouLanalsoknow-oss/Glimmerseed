package com.example.glimmerseed.floatingpreviewbase.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.stage.PanelSlot
import com.example.glimmerseed.floatingpreviewbase.FloatingRecorder
import com.example.glimmerseed.floatingpreviewbase.panel.PanelView

class StageService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "stage_service_channel"

        @JvmStatic
        private var instanceRef: java.lang.ref.WeakReference<StageService>? = null

        fun getPanelRenderer() = instanceRef?.get()?.rootView?.panelRenderer

        fun startService(context: Context) {
            val intent = Intent(context, StageService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, StageService::class.java)
            context.stopService(intent)
        }
    }

    internal var windowManager: WindowManager? = null
    internal var rootView: StageRootLayout? = null
    internal var rootParams: WindowManager.LayoutParams? = null
    private var overlayAttached = false

    internal var panelManager: PanelManager? = null
    private var screenOffReceiver: ScreenOffReceiver? = null
    private val recorder: FloatingRecorder by lazy { FloatingRecorder(this) }
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recordingCaptureRunnable: Runnable? = null

    /**
     * 获取面板渲染器（用于编辑器传入骨骼/动画/网格/纹理数据）
     * 返回 null 表示服务未初始化或根视图未附加
     */
    fun getPanelRenderer() = rootView?.panelRenderer

    override fun onCreate() {
        super.onCreate()
        instanceRef = java.lang.ref.WeakReference(this)
        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, createNotification())
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            panelManager = PanelManager(this)
            screenOffReceiver = ScreenOffReceiver(this)
            screenOffReceiver?.register()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun ensureOverlayAttached() {
        if (overlayAttached) return
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        rootView = StageRootLayout(this).apply {
            engine = panelManager?.engine
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        rootParams = params
        windowManager?.addView(rootView, params)
        overlayAttached = true
    }

    private fun detachOverlayIfEmpty() {
        if (!overlayAttached) return
        val hasActivePanels = panelManager?.engine?.spatialOrchestrator?.sortedPanelIds?.isNotEmpty() == true
        if (!hasActivePanels) {
            try {
                rootView?.let { windowManager?.removeView(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            rootView = null
            rootParams = null
            overlayAttached = false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        instanceRef = null
        screenOffReceiver?.unregister()
        // 停止渲染循环和动画控制器
        try {
            rootView?.stopRenderLoop()
            rootView?.panelRenderer?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            panelManager?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (overlayAttached) {
                rootView?.let { windowManager?.removeView(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        rootView = null
        rootParams = null
        overlayAttached = false
        panelManager = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        panelManager?.onConfigurationChanged()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "多面板交互",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "运行多面板交互系统"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("面板交互运行中")
            .setContentText("全屏交互面板层已启动")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .build()
    }

    internal fun onTouchModeChanged(needsTouch: Boolean) {
        if (!overlayAttached) return
        rootParams?.let { params ->
            val baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            val newFlags = if (needsTouch) {
                baseFlags
            } else {
                baseFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
            if (newFlags != params.flags) {
                params.flags = newFlags
                try {
                    windowManager?.updateViewLayout(rootView, params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addPanel(data: PanelData, slot: PanelSlot) {
        ensureOverlayAttached()
        panelManager?.addPanel(data, slot)
    }

    fun loadStage(panels: List<PanelData>, slots: Map<String, PanelSlot>) {
        for (panel in panels) {
            val slot = slots[panel.id] ?: createDefaultSlot(panel.id)
            addPanel(panel, slot)
        }
    }

    private fun createDefaultSlot(panelId: String): PanelSlot {
        return PanelSlot(
            panelId = panelId,
            zOrder = 0,
            active = true,
            landscapeRect = NormalizedRect(0f, 0f, 1f, 1f),
            portraitRect = NormalizedRect(0f, 0f, 1f, 1f)
        )
    }

    fun startRecording(config: FloatingRecorder.RecordConfig) {
        recorder.setListener(object : FloatingRecorder.RecorderListener {
            override fun onStateChanged(state: FloatingRecorder.RecordState) {}
            override fun onProgress(currentFrame: Int, totalFrames: Int) {}
            override fun onFinished(file: java.io.File) {}
            override fun onError(error: String) {}
        })
        recorder.startRecording(config)
        recordingCaptureRunnable = Runnable {
            captureRecordingFrame()
        }
        mainHandler.post(recordingCaptureRunnable!!)
    }

    fun stopRecording() {
        recordingCaptureRunnable?.let { mainHandler.removeCallbacks(it) }
        recordingCaptureRunnable = null
        recorder.stopRecording()
    }

    fun captureScreenshot(): java.io.File? {
        val root = rootView ?: return null
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        root.draw(canvas)
        return recorder.takeScreenshot(bitmap)
    }

    private fun captureRecordingFrame() {
        val root = rootView ?: return
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        root.draw(canvas)
        recorder.addFrame(bitmap)
        recordingCaptureRunnable?.let {
            mainHandler.postDelayed(it, 1000L / 30L)
        }
    }

    fun removePanel(panelId: String) {
        panelManager?.removePanel(panelId)
        detachOverlayIfEmpty()
    }

    fun setPanelActive(panelId: String, active: Boolean) {
        panelManager?.setPanelActive(panelId, active)
    }
}
