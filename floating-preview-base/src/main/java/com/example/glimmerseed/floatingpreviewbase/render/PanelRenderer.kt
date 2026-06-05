package com.example.glimmerseed.floatingpreviewbase.render

import android.graphics.Canvas
import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import android.graphics.Bitmap
import com.example.glimmerseed.floatingpreviewbase.panel.PanelView
import com.example.glimmerseed.floatingpreviewbase.stage.StageAnimationController

class PanelRenderer {

    private var frameCount = 0
    private var lastFpsCheck = 0L
    var currentFps: Int = 60

    private val skinningRenderer = CanvasSkinningRenderer()

    /** 舞台动画播放控制器（用于编辑器预览面板的实时动画） */
    val animController = StageAnimationController()

    data class SkinningPanelInput(
        val panelId: String,
        val input: CanvasSkinningRenderer.SkinningInput
    )

    var skinnedPanels: List<SkinningPanelInput> = emptyList()

    init {
        // 将动画控制器的帧就绪回调接入渲染管线
        animController.onFrameReady = { input ->
            updateSkinnedPanel(PREVIEW_PANEL_ID, input)
        }
    }

    /**
     * 设置编辑器预览数据并启动动画播放
     * 从编辑器传入骨骼/动画/网格/纹理，在后台线程评估后推送到渲染
     */
    fun setEditorPreview(
        skeleton: Skeleton,
        clip: AnimationClip?,
        mesh: Mesh?,
        texture: Bitmap?
    ) {
        animController.setSource(skeleton, clip, mesh, texture)
        animController.startPlayback()
    }

    /**
     * 停止编辑器预览动画
     */
    fun stopEditorPreview() {
        animController.stopPlayback()
    }

    fun render(canvas: Canvas, panelViews: List<PanelView>) {
        val now = System.currentTimeMillis()
        frameCount++
        if (now - lastFpsCheck > 1000) {
            currentFps = frameCount
            frameCount = 0
            lastFpsCheck = now
        }

        val skinInputMap = skinnedPanels.associateBy { it.panelId }

        for (panel in panelViews) {
            if (!panel.isPanelActive) continue

            panel.advanceFrame(now)

            val skinning = skinInputMap[panel.panelId]
            if (skinning != null) {
                panel.setSkeletonCallback { c, vw, vh ->
                    skinningRenderer.render(c, skinning.input)
                }
            }

            canvas.save()
            panel.draw(canvas)
            canvas.restore()
        }
    }

    fun updateSkinnedPanel(panelId: String, input: CanvasSkinningRenderer.SkinningInput) {
        val existing = skinnedPanels.toMutableList()
        val idx = existing.indexOfFirst { it.panelId == panelId }
        val entry = SkinningPanelInput(panelId, input)
        if (idx >= 0) {
            existing[idx] = entry
        } else {
            existing.add(entry)
        }
        skinnedPanels = existing
    }

    fun removeSkinnedPanel(panelId: String) {
        skinnedPanels = skinnedPanels.filter { it.panelId != panelId }
    }

    fun clearSkeletonInputs() {
        skinnedPanels = emptyList()
    }

    fun destroy() {
        animController.release()
        clearSkeletonInputs()
    }

    companion object {
        const val PREVIEW_PANEL_ID = "__editor_preview__"
    }
}