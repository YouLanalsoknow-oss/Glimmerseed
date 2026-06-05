package com.example.glimmerseed.floatingpreviewbase.panel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.panel.BehaviorAction
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.panel.TouchMode

class PanelView(context: Context) : View(context) {

    var panelId: String = ""
    var zOrder: Int = 0
    var isPanelActive: Boolean = true
    var landscapeRect: NormalizedRect = NormalizedRect(0f, 0f, 1f, 1f)
    var portraitRect: NormalizedRect = NormalizedRect(0f, 0f, 1f, 1f)
    var touchMode: TouchMode = TouchMode.PASSTHROUGH

    internal var pixelLeft: Int = 0
    internal var pixelTop: Int = 0
    internal var pixelWidth: Int = 0
    internal var pixelHeight: Int = 0

    internal var interactionLayer: InteractionLayer? = null
    internal var behaviorLayer: BehaviorLayer? = null

    val needsTouchInterception: Boolean
        get() = isPanelActive && touchMode != TouchMode.PASSTHROUGH

    private var visualLayer: VisualLayer? = null
    private var actionExecutor: ((BehaviorAction) -> Unit)? = null

    fun bindPanelData(data: PanelData, landscapeRect: NormalizedRect, portraitRect: NormalizedRect) {
        this.panelId = data.id
        this.landscapeRect = landscapeRect
        this.portraitRect = portraitRect
        this.touchMode = data.interaction.touchMode
        this.visualLayer = VisualLayer(data.visual, data.interaction.hitRegions)
        this.interactionLayer = InteractionLayer(data.interaction)
        this.behaviorLayer = BehaviorLayer(data.behavior) { action ->
            actionExecutor?.invoke(action)
        }
    }

    fun setActionExecutor(executor: (BehaviorAction) -> Unit) {
        this.actionExecutor = executor
    }

    fun setFrameBitmaps(bitmaps: List<Bitmap>) {
        visualLayer?.frameBitmaps = bitmaps
    }

    fun setLayerBitmaps(bitmaps: Map<Int, Bitmap>) {
        visualLayer?.layerBitmaps = bitmaps
    }

    fun setSkeletonCallback(callback: (Canvas, Int, Int) -> Unit) {
        visualLayer?.onDrawSkeleton = callback
    }

    fun setSkeletonBoneData(data: VisualLayer.SkeletonRenderData?) {
        visualLayer?.skeletonBoneData = data
    }

    fun advanceFrame(nowMs: Long) {
        visualLayer?.advanceFrame(nowMs)
    }

    fun isInTouchRegion(localX: Float, localY: Float): Boolean {
        if (touchMode == TouchMode.BLOCKING) return true
        if (touchMode == TouchMode.PASSTHROUGH) return false
        return interactionLayer?.isInTouchRegion(localX, localY, pixelWidth.coerceAtLeast(1), pixelHeight.coerceAtLeast(1)) ?: false
    }

    fun handleTouchEvent(event: MotionEvent): TouchDispatchResult {
        val gesture = interactionLayer?.handleTouchEvent(event)
        if (gesture != null) {
            behaviorLayer?.handleGestureEvent(gesture)
        }
        return TouchDispatchResult.CONSUMED
    }

    fun destroy() {
        interactionLayer?.destroy()
        interactionLayer = null
        behaviorLayer = null
        visualLayer = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        visualLayer?.draw(canvas, width, height)
    }
}