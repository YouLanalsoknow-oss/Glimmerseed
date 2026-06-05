package com.example.glimmerseed.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.floatingpreviewbase.render.CanvasSkinningRenderer

class SkinningPreviewView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val renderer = CanvasSkinningRenderer()
    
    var skeleton: Skeleton? = null
    var mesh: Mesh? = null
    var offsetX: Float = 0f
    var offsetY: Float = 0f
    var previewScaleX: Float = 1f
    var previewScaleY: Float = 1f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val skeleton = skeleton
        val mesh = mesh
        
        if (skeleton != null && mesh != null) {
            val input = CanvasSkinningRenderer.SkinningInput(
                skeleton = skeleton,
                mesh = mesh,
                offsetX = offsetX,
                offsetY = offsetY,
                scaleX = previewScaleX,
                scaleY = previewScaleY
            )
            renderer.render(canvas, input)
        }
    }

    fun updatePreview(
        skeleton: Skeleton?,
        mesh: Mesh?,
        offsetX: Float,
        offsetY: Float,
        scale: Float
    ) {
        this.skeleton = skeleton
        this.mesh = mesh
        this.offsetX = offsetX
        this.offsetY = offsetY
        this.previewScaleX = scale
        this.previewScaleY = scale
        invalidate()
    }
}