package com.example.glimmerseed.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.AnimationPlayer
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.floatingpreviewbase.render.CanvasSkinningRenderer
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.max
import java.util.concurrent.atomic.AtomicBoolean

class EditorCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ========== 数据源 ==========
    private var skeleton: Skeleton? = null
    private var animationClip: AnimationClip? = null
    private var mesh: Mesh? = null
    private var texture: Bitmap? = null

    // ========== 显示状态 ==========
    private var showGrid: Boolean = true
    private var showOnionSkin: Boolean = false
    private var showSkeletalPreview: Boolean = true
    private var selectedBoneId: Int? = null
    private var editMode: Boolean = true
    private var currentTime: Float = 0f

    // ========== 视图变换 ==========
    private var viewScale: Float = 1f
    private var viewOffsetX: Float = 0f
    private var viewOffsetY: Float = 0f

    // ========== 渲染器 ==========
    private val skinningRenderer = CanvasSkinningRenderer()

    // ========== 回调 ==========
    private var onBoneSelected: ((Int?) -> Unit)? = null
    private var onBoneMoved: ((Int, Float, Float) -> Unit)? = null

    // ========== Paint 成员变量（全部在 init 中初始化一次）==========
    private val gridPaint: Paint
    private val axisXPaint: Paint
    private val axisYPaint: Paint
    private val bonePaint: Paint
    private val jointPaint: Paint
    private val selectedJointPaint: Paint
    private val onionPaint: Paint
    private val manipulatorXPaint: Paint
    private val manipulatorYPaint: Paint
    private val manipulatorXFill: Paint
    private val manipulatorYFill: Paint

    init {
        gridPaint = Paint().apply {
            color = Color.argb(30, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
            isAntiAlias = true
        }
        axisXPaint = Paint().apply {
            color = Color.argb(200, 255, 80, 80)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        axisYPaint = Paint().apply {
            color = Color.argb(200, 80, 255, 80)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        bonePaint = Paint().apply {
            color = Color.argb(180, 200, 200, 80)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        jointPaint = Paint().apply {
            color = Color.argb(200, 255, 200, 80)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        selectedJointPaint = Paint().apply {
            color = Color.argb(220, 255, 150, 50)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        onionPaint = Paint().apply {
            style = Paint.Style.FILL
            alpha = 60
            color = Color.argb(100, 180, 180, 180)
            isAntiAlias = true
        }
        manipulatorXPaint = Paint().apply {
            color = Color.argb(200, 255, 80, 80)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        manipulatorYPaint = Paint().apply {
            color = Color.argb(200, 80, 255, 80)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        manipulatorXFill = Paint().apply {
            color = Color.argb(200, 255, 80, 80)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        manipulatorYFill = Paint().apply {
            color = Color.argb(200, 80, 255, 80)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    // ========== 后台计算线程 ==========
    private val evalHandlerThread = HandlerThread("SkeletonEval").apply { start() }
    private val evalHandler = Handler(evalHandlerThread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var cachedSkeleton: Skeleton? = null
    @Volatile
    private var cachedOnionPrev: Skeleton? = null
    @Volatile
    private var cachedOnionNext: Skeleton? = null
    private val evaluationPending = AtomicBoolean(false)
    private var lastEvalParams: Triple<AnimationClip?, Float, Skeleton?>? = null

    private fun scheduleEvaluation() {
        val clip = animationClip
        val skel = skeleton
        val time = currentTime
        if (clip == null || skel == null) {
            cachedSkeleton = skel
            return
        }
        val params = Triple(clip, time, skel)
        if (params == lastEvalParams && cachedSkeleton != null) return
        lastEvalParams = params
        if (evaluationPending.compareAndSet(false, true)) {
            evalHandler.post {
                try {
                    // 主帧
                    val result = AnimationPlayer.evaluateAt(clip, time, skel)
                    // 洋葱皮帧（仅在启用时计算）
                    var onionPrev: Skeleton? = null
                    var onionNext: Skeleton? = null
                    if (showOnionSkin) {
                        val frameTime = 1f / 24f
                        val prevTime = time - frameTime
                        val nextTime = time + frameTime
                        if (prevTime >= 0f) onionPrev = AnimationPlayer.evaluateAt(clip, prevTime, skel)
                        if (nextTime <= clip.duration) onionNext = AnimationPlayer.evaluateAt(clip, nextTime, skel)
                    }
                    mainHandler.post {
                        cachedSkeleton = result
                        cachedOnionPrev = onionPrev
                        cachedOnionNext = onionNext
                        evaluationPending.set(false)
                        invalidate()
                        // 检查参数是否在计算期间已变化，如有则重新调度
                        val current = Triple(animationClip, currentTime, skeleton)
                        if (current != lastEvalParams) scheduleEvaluation()
                    }
                } catch (e: Exception) {
                    mainHandler.post { evaluationPending.set(false) }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        evalHandlerThread.quitSafely()
    }

    // ========== 手势检测器 ==========
    private val gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!editMode) return false
                val (wx, wy) = screenToWorld(e.x, e.y)
                val hitBoneId = findNearestBone(wx, wy)
                if (hitBoneId != selectedBoneId) {
                    selectedBoneId = hitBoneId
                    onBoneSelected?.invoke(hitBoneId)
                    invalidate()
                }
                return true
            }

            override fun onDown(e: MotionEvent): Boolean = true
        })
    }

    private val scaleDetector: ScaleGestureDetector by lazy {
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val newScale = (viewScale * detector.scaleFactor).coerceIn(0.1f, 10f)
                if (newScale != viewScale) {
                    viewScale = newScale
                    invalidate()
                }
                return true
            }
        })
    }

    // ========== 拖拽状态 ==========
    private var isDragging: Boolean = false
    private var dragStartX: Float = 0f
    private var dragStartY: Float = 0f
    private var dragBoneStartPos: Vector3f? = null
    private var pointerCountAtDown: Int = 0

    // ========== 公共 API ==========

    fun setSkeleton(skeleton: Skeleton?) {
        this.skeleton = skeleton
        lastEvalParams = null
        cachedSkeleton = null
        scheduleEvaluation()
        invalidate()
    }

    fun setAnimationClip(clip: AnimationClip?) {
        this.animationClip = clip
        lastEvalParams = null
        cachedSkeleton = null
        scheduleEvaluation()
        invalidate()
    }

    fun setCurrentTime(time: Float) {
        this.currentTime = time
        scheduleEvaluation()
        invalidate()
    }

    fun setShowGrid(show: Boolean) {
        this.showGrid = show
        invalidate()
    }

    fun setShowOnionSkin(show: Boolean) {
        this.showOnionSkin = show
        invalidate()
    }

    fun setShowSkeletalPreview(show: Boolean) {
        this.showSkeletalPreview = show
        invalidate()
    }

    fun setSelectedBoneId(boneId: Int?) {
        this.selectedBoneId = boneId
        invalidate()
    }

    fun setEditMode(enabled: Boolean) {
        this.editMode = enabled
        invalidate()
    }

    fun setMesh(mesh: Mesh?) {
        this.mesh = mesh
        invalidate()
    }

    fun setTexture(bitmap: Bitmap?) {
        this.texture = bitmap
        invalidate()
    }

    fun setViewTransform(scale: Float, offsetX: Float, offsetY: Float) {
        this.viewScale = scale.coerceIn(0.1f, 10f)
        this.viewOffsetX = offsetX
        this.viewOffsetY = offsetY
        invalidate()
    }

    fun setOnBoneSelected(callback: (Int?) -> Unit) {
        this.onBoneSelected = callback
    }

    fun setOnBoneMoved(callback: (Int, Float, Float) -> Unit) {
        this.onBoneMoved = callback
    }

    // ========== 坐标转换 ==========

    private fun screenToWorld(sx: Float, sy: Float): Pair<Float, Float> {
        val cx = width / 2f + viewOffsetX
        val cy = height / 2f + viewOffsetY
        return Pair((sx - cx) / viewScale, (sy - cy) / viewScale)
    }

    private fun worldToScreen(wx: Float, wy: Float): Pair<Float, Float> {
        val cx = width / 2f + viewOffsetX
        val cy = height / 2f + viewOffsetY
        return Pair(wx * viewScale + cx, wy * viewScale + cy)
    }

    // ========== 触摸事件 ==========

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!editMode) return false
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pointerCountAtDown = event.pointerCount
                isDragging = true
                dragStartX = event.x
                dragStartY = event.y
                val hitBoneId = selectedBoneId
                if (hitBoneId != null) {
                    skeleton?.getBoneById(hitBoneId)?.let {
                        val mat = skeleton!!.getBoneWorldTransform(hitBoneId)
                        dragBoneStartPos = Vector3f(mat.m30(), mat.m31(), mat.m32())
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragging) return true
                // 多指时不做骨骼拖拽或视图平移（交给缩放）
                if (event.pointerCount > 1) return true
                if (selectedBoneId != null && dragBoneStartPos != null) {
                    val (wx, wy) = screenToWorld(event.x, event.y)
                    val (startWx, startWy) = screenToWorld(dragStartX, dragStartY)
                    val dx = wx - startWx
                    val dy = wy - startWy
                    onBoneMoved?.invoke(selectedBoneId!!, dragBoneStartPos!!.x + dx, dragBoneStartPos!!.y + dy)
                } else if (selectedBoneId == null) {
                    val dx = event.x - dragStartX
                    val dy = event.y - dragStartY
                    viewOffsetX += dx
                    viewOffsetY += dy
                    dragStartX = event.x
                    dragStartY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                dragBoneStartPos = null
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findNearestBone(worldX: Float, worldY: Float): Int? {
        val skel = skeleton ?: return null
        // 使用缓存骨架进行命中检测（与显示一致）
        val displaySk = cachedSkeleton ?: skel
        val hitRadius = 20f / viewScale
        var nearestId: Int? = null
        var nearestDist = Float.MAX_VALUE

        for (bone in displaySk.bones) {
            val mat = displaySk.getBoneWorldTransform(bone.id)
            val bx = mat.m30()
            val by = mat.m31()
            val dist = (worldX - bx) * (worldX - bx) + (worldY - by) * (worldY - by)
            if (dist < hitRadius * hitRadius && dist < nearestDist) {
                nearestDist = dist
                nearestId = bone.id
            }
        }
        return nearestId
    }

    // ========== 绘制 ==========

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        val skel = skeleton ?: return

        scheduleEvaluation()

        val displaySkeleton = cachedSkeleton ?: skel

        val cx = width / 2f + viewOffsetX
        val cy = height / 2f + viewOffsetY

        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(viewScale, viewScale)

        if (showGrid) drawGrid(canvas)
        drawAxes(canvas)

        // 使用 skinningRenderer 渲染蒙皮或骨架
        val skinningInput = CanvasSkinningRenderer.SkinningInput(
            skeleton = displaySkeleton,
            mesh = mesh ?: Mesh(emptyList(), emptyList()),
            texture = texture,
            offsetX = 0f,
            offsetY = 0f,
            scaleX = 1f,
            scaleY = 1f
        )

        // 第一层：舞台基底 — 始终渲染蒙皮网格（有纹理时显示带纹理的蒙皮结果）
        if (mesh != null) {
            skinningRenderer.render(canvas, skinningInput)
        }

        // 第二层：骨骼线框叠加 — 始终在蒙皮之上绘制骨架线框，便于编辑操作
        skinningRenderer.renderSkeletonOnly(canvas, skinningInput)

        if (showOnionSkin) {
            drawOnionSkin(canvas)
        }

        if (editMode && selectedBoneId != null) {
            drawSelectionAndManipulator(canvas, displaySkeleton)
        }

        canvas.restore()
    }

    private fun drawGrid(canvas: Canvas) {
        val gridSize = 50f
        val extent = max(width, height).toFloat() / viewScale +
                abs(viewOffsetX / viewScale) + abs(viewOffsetY / viewScale)
        val gridExtent = (extent / gridSize).toInt() * gridSize + gridSize

        for (i in (-gridExtent.toInt())..(gridExtent.toInt()) step gridSize.toInt()) {
            canvas.drawLine(-gridExtent, i.toFloat(), gridExtent, i.toFloat(), gridPaint)
            canvas.drawLine(i.toFloat(), -gridExtent, i.toFloat(), gridExtent, gridPaint)
        }
    }

    private fun drawAxes(canvas: Canvas) {
        val len = 100f
        canvas.drawLine(0f, 0f, len, 0f, axisXPaint)
        canvas.drawLine(0f, 0f, 0f, len, axisYPaint)
    }

    private fun drawOnionSkin(canvas: Canvas) {
        cachedOnionPrev?.let { drawSkeletonDots(canvas, it) }
        cachedOnionNext?.let { drawSkeletonDots(canvas, it) }
    }

    private fun drawSkeletonDots(canvas: Canvas, sk: Skeleton) {
        for (bone in sk.bones) {
            val mat = sk.getBoneWorldTransform(bone.id)
            canvas.drawCircle(mat.m30(), mat.m31(), 4f, onionPaint)
        }
    }

    private fun drawSelectionAndManipulator(canvas: Canvas, sk: Skeleton) {
        val boneId = selectedBoneId ?: return
        val bone = sk.getBoneById(boneId) ?: return
        val mat = sk.getBoneWorldTransform(boneId)
        val bx = mat.m30()
        val by = mat.m31()

        canvas.drawCircle(bx, by, 8f, selectedJointPaint)

        val handleLen = 40f
        val angleRad = kotlin.math.atan2(mat.m10().toDouble(), mat.m00().toDouble())
        val angle = angleRad.toFloat()

        val hx = handleLen * kotlin.math.cos(angle)
        val hy = handleLen * kotlin.math.sin(angle)

        canvas.drawLine(bx, by, bx + hx, by + hy, manipulatorXPaint)
        canvas.drawLine(bx, by, bx - hy, by + hx, manipulatorYPaint)

        canvas.drawCircle(bx + hx, by + hy, 4f, manipulatorXFill)
        canvas.drawCircle(bx - hy, by + hx, 4f, manipulatorYFill)
    }
}
