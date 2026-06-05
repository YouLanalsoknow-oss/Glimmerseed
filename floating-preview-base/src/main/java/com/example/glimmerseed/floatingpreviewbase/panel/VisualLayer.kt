package com.example.glimmerseed.floatingpreviewbase.panel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.example.glimmerseed.editorcore.panel.BlendingMode
import com.example.glimmerseed.editorcore.panel.HitRegion
import com.example.glimmerseed.editorcore.panel.VisualType
import com.example.glimmerseed.editorcore.panel.VisualLayerData

class VisualLayer(private val data: VisualLayerData, val hitRegions: List<HitRegion> = emptyList()) {

    data class BoneRenderInfo(
        val id: Int,
        val name: String,
        val parentId: Int?,
        val worldX: Float,
        val worldY: Float
    )

    data class SkeletonRenderData(
        val bones: List<BoneRenderInfo>
    )

    private val debugPaint = Paint().apply {
        color = Color.argb(60, 100, 200, 255)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val debugBorderPaint = Paint().apply {
        color = Color.argb(120, 100, 200, 255)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val skeletonBonePaint = Paint().apply {
        color = Color.argb(180, 255, 200, 100)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val skeletonJointPaint = Paint().apply {
        color = Color.argb(200, 255, 200, 100)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val layerPaint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    private val placeHolderPaint = Paint().apply {
        color = Color.argb(40, 150, 150, 150)
        style = Paint.Style.FILL
    }

    var frameBitmaps: List<Bitmap> = emptyList()
    var layerBitmaps: Map<Int, Bitmap> = emptyMap()
    var onDrawSkeleton: ((Canvas, Int, Int) -> Unit)? = null
    var skeletonBoneData: SkeletonRenderData? = null

    private var currentFrameIndex = 0
    private var lastFrameTimeMs = 0L
    private val frameDurationMs: Long = (1000f / 24f).toLong()

    fun draw(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        if (!data.visible || data.opacity <= 0f) return

        canvas.save()

        val alpha = (data.opacity * 255).toInt().coerceIn(0, 255)

        when (data.type) {
            VisualType.FRAME_ANIMATION -> {
                drawFrameAnimation(canvas, viewWidth, viewHeight, alpha)
            }
            VisualType.SKELETAL_ANIMATION -> {
                drawSkeletalAnimation(canvas, viewWidth, viewHeight, alpha)
            }
            VisualType.LAYER_RENDERING -> {
                drawLayers(canvas, viewWidth, viewHeight, alpha)
            }
        }

        drawDebugRegions(canvas, viewWidth, viewHeight)
        canvas.restore()
    }

    fun advanceFrame(nowMs: Long) {
        if (data.type != VisualType.FRAME_ANIMATION) return
        if (frameBitmaps.isEmpty()) return
        if (lastFrameTimeMs == 0L) {
            lastFrameTimeMs = nowMs
            return
        }
        val elapsed = nowMs - lastFrameTimeMs
        if (elapsed >= frameDurationMs) {
            val framesToAdvance = (elapsed / frameDurationMs).toInt()
            currentFrameIndex = (currentFrameIndex + framesToAdvance) % frameBitmaps.size
            lastFrameTimeMs = nowMs - (elapsed % frameDurationMs)
        }
    }

    private fun drawFrameAnimation(canvas: Canvas, viewWidth: Int, viewHeight: Int, alpha: Int) {
        if (frameBitmaps.isEmpty()) {
            drawPlaceholder(canvas, viewWidth, viewHeight, "No frames")
            return
        }
        val bitmap = frameBitmaps[currentFrameIndex.coerceIn(0, frameBitmaps.lastIndex)]
        layerPaint.alpha = alpha
        val destRect = android.graphics.Rect(0, 0, viewWidth, viewHeight)
        val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
        canvas.drawBitmap(bitmap, srcRect, destRect, layerPaint)
    }

    private fun drawSkeletalAnimation(canvas: Canvas, viewWidth: Int, viewHeight: Int, @Suppress("UNUSED_PARAMETER") alpha: Int) {
        val boneData = skeletonBoneData
        if (boneData != null) {
            drawSkeletonBones(canvas, viewWidth, viewHeight, boneData)
        }

        val callback = onDrawSkeleton
        if (callback != null) {
            val saveCount = canvas.saveLayer(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), layerPaint)
            callback(canvas, viewWidth, viewHeight)
            canvas.restoreToCount(saveCount)
        } else if (boneData == null) {
            drawPlaceholder(canvas, viewWidth, viewHeight, "Skeleton")
        }
    }

    private fun drawSkeletonBones(canvas: Canvas, viewWidth: Int, viewHeight: Int, data: SkeletonRenderData) {
        val boneMap = data.bones.associateBy { it.id }
        val jointRadius = 5f

        for (bone in data.bones) {
            val jointX = bone.worldX * viewWidth
            val jointY = bone.worldY * viewHeight

            canvas.drawCircle(jointX, jointY, jointRadius, skeletonJointPaint)

            val parentId = bone.parentId
            if (parentId != null) {
                val parent = boneMap[parentId]
                if (parent != null) {
                    val parentX = parent.worldX * viewWidth
                    val parentY = parent.worldY * viewHeight
                    canvas.drawLine(parentX, parentY, jointX, jointY, skeletonBonePaint)
                }
            }
        }
    }

    private fun drawLayers(canvas: Canvas, viewWidth: Int, viewHeight: Int, alpha: Int) {
        if (data.layers.isEmpty()) {
            drawPlaceholder(canvas, viewWidth, viewHeight, "No layers")
            return
        }

        for ((index, layer) in data.layers.withIndex()) {
            val bitmap = layerBitmaps[index]
            if (bitmap == null) continue

            val saveCount = canvas.save()

            val cx = viewWidth * (layer.posX + 0.5f)
            val cy = viewHeight * (layer.posY + 0.5f)

            canvas.translate(cx, cy)
            canvas.rotate(layer.rotation)
            canvas.scale(layer.scaleX, layer.scaleY)

            layerPaint.alpha = (alpha * layer.opacity).toInt().coerceIn(0, 255)

            when (layer.blending) {
                BlendingMode.NORMAL -> {
                    layerPaint.xfermode = null
                }
                BlendingMode.ADDITIVE -> {
                    layerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
                }
                BlendingMode.MULTIPLY -> {
                    layerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                }
            }

            val layerW = viewWidth
            val layerH = viewHeight
            val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
            val destRect = android.graphics.Rect(-layerW / 2, -layerH / 2, layerW / 2, layerH / 2)
            canvas.drawBitmap(bitmap, srcRect, destRect, layerPaint)

            layerPaint.xfermode = null
            canvas.restoreToCount(saveCount)
        }
    }

    private fun drawPlaceholder(canvas: Canvas, viewWidth: Int, viewHeight: Int, @Suppress("UNUSED_PARAMETER") label: String) {
        canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), placeHolderPaint)

        val circlePaint = Paint().apply {
            color = Color.argb(180, 200, 220, 240)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val minSize = minOf(viewWidth, viewHeight)
        val circleRadius = minSize * 0.25f
        canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, circleRadius, circlePaint)

        val borderPaint = Paint().apply {
            color = Color.argb(200, 150, 180, 220)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawCircle(viewWidth / 2f, viewHeight / 2f, circleRadius, borderPaint)

        val typePaint = Paint().apply {
            color = Color.argb(120, 100, 120, 150)
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(data.type.name, viewWidth / 2f, viewHeight / 2f + circleRadius + 30f, typePaint)
    }

    private fun drawDebugRegions(canvas: Canvas, viewWidth: Int, viewHeight: Int) {
        for (region in hitRegions) {
            val r = region.normalizedRect
            val left = r.x * viewWidth
            val top = r.y * viewHeight
            val right = (r.x + r.width) * viewWidth
            val bottom = (r.y + r.height) * viewHeight
            canvas.drawRect(left, top, right, bottom, debugPaint)
            canvas.drawRect(left, top, right, bottom, debugBorderPaint)
        }
    }
}