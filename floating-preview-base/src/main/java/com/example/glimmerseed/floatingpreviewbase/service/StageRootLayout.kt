package com.example.glimmerseed.floatingpreviewbase.service

import android.content.Context
import android.graphics.Canvas
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.glimmerseed.floatingpreviewbase.panel.PanelView
import com.example.glimmerseed.floatingpreviewbase.render.PanelRenderer
import com.example.glimmerseed.floatingpreviewbase.stage.StageEngine

/**
 * 舞台根布局 — 多面板合并渲染协调器
 *
 * 职责：
 * 1. 作为所有面板的容器 ViewGroup，管理子 View 的生命周期
 * 2. 通过 Choreographer 驱动渲染循环，实现 60fps 合成
 * 3. 按 z-order 从底到顶合成所有面板（含动画面板的实时骨骼数据）
 * 4. 将触摸事件分发给 StageEngine
 */
internal class StageRootLayout(context: Context) : ViewGroup(context) {

    var engine: StageEngine? = null

    /** 集中式渲染器 */
    val panelRenderer = PanelRenderer()

    /** 渲染循环是否活跃 */
    private var renderActive = false

    /** Choreographer 帧回调 */
    private val choreographer = Choreographer.getInstance()
    private lateinit var frameCallback: Choreographer.FrameCallback

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setWillNotDraw(false)
        startRenderLoop()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRenderLoop()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 子 PanelView 的位置由 SpatialOrchestrator 通过 layoutPanels() 单独设置
        // 这里不需要做布局计算
    }

    /**
     * 核心绘制方法：按 z-order 合并渲染所有面板
     *
     * 渲染顺序（从底到顶）：
     * 1. 按 zOrder 升序排列所有子 PanelView
     * 2. 对每个可见且激活的面板调用 PanelRenderer.render()
     * 3. PanelRenderer 内部处理骨骼蒙皮/帧动画/图层合成
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val engineRef = engine ?: return

        // 收集所有子 PanelView 并按 z-order 排序
        val panelViews = (0 until childCount)
            .mapNotNull { getChildAt(it) as? PanelView }
            .filter { it.isPanelActive }
            .sortedBy { it.zOrder }

        if (panelViews.isEmpty()) return

        // 通过 PanelRenderer 统一渲染（含动画数据注入）
        panelRenderer.render(canvas, panelViews)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return engine?.handleTouchEvent(event) ?: false
    }

    override fun shouldDelayChildPressedState(): Boolean = false

    /**
     * 启动渲染循环
     */
    private fun startRenderLoop() {
        if (renderActive) return
        renderActive = true
        frameCallback = Choreographer.FrameCallback { _ ->
            if (renderActive) {
                invalidate()
                choreographer.postFrameCallback(frameCallback)
            }
        }
        choreographer.postFrameCallback(frameCallback)
    }

    /**
     * 停止渲染循环
     */
    internal fun stopRenderLoop() {
        renderActive = false
        choreographer.removeFrameCallback(frameCallback)
    }

    /**
     * 获取当前所有面板视图（按 z-order 排序）
     */
    fun getSortedPanelViews(): List<PanelView> {
        return (0 until childCount)
            .mapNotNull { getChildAt(it) as? PanelView }
            .sortedBy { it.zOrder }
    }
}
