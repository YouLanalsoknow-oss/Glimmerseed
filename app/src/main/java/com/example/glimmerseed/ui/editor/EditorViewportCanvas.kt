package com.example.glimmerseed.ui.editor

import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.glimmerseed.editorcore.animation.Mesh
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.max

private val WALLPAPER_BG = Color(0xFFF5F0E8)
private val GRID_COLOR = Color(0xFFD7CCC8)
private val AXIS_X_COLOR = Color(0xFFEF9A9A)
private val AXIS_Y_COLOR = Color(0xFFA5D6A7)
private val ORIGIN_COLOR = Color(0xFF795548)
private val BONE_NORMAL_COLOR = Color(0xFF8D6E63)
private val BONE_SELECTED_COLOR = Color(0xFFFFB300)
private val JOINT_NORMAL_COLOR = Color(0xFFA1887F)
private val JOINT_SELECTED_COLOR = Color(0xFFFFA000)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditorViewportCanvas(
    editorState: com.example.glimmerseed.editorcore.editor.EditorState,
    onAction: (com.example.glimmerseed.editorcore.editor.EditorAction) -> Unit
) {
    var lastZoom by remember { mutableStateOf(1f) }
    var lastPointerDistance by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    onAction(
                        com.example.glimmerseed.editorcore.editor.EditorAction.TranslateViewport(
                            Vector2f(dragAmount.x, dragAmount.y)
                        )
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val skeleton = editorState.skeleton
                    if (skeleton != null) {
                        val worldPoint = screenToWorld(
                            screenPoint = offset,
                            viewportTranslation = Vector2f(editorState.viewportTranslation.x, editorState.viewportTranslation.y),
                            viewportScale = editorState.viewportScale
                        )

                        for (bone in skeleton.bones) {
                            val parentId = bone.parentId
                            val startPoint = if (parentId != null) {
                                val parent = skeleton.getBoneById(parentId)
                                if (parent != null) {
                                    val localPos = Vector2f(parent.localTransform.getTranslation(Vector3f()).x, parent.localTransform.getTranslation(Vector3f()).y)
                                    localPos
                                } else {
                                    Vector2f(0f, 0f)
                                }
                            } else {
                                Vector2f(0f, 0f)
                            }

                            val endPoint = Vector2f(
                                bone.localTransform.getTranslation(Vector3f()).x,
                                bone.localTransform.getTranslation(Vector3f()).y
                            )

                            val screenStart = worldToScreen(
                                worldPoint = startPoint,
                                viewportTranslation = Vector2f(editorState.viewportTranslation.x, editorState.viewportTranslation.y),
                                viewportScale = editorState.viewportScale
                            )
                            val screenEnd = worldToScreen(
                                worldPoint = endPoint,
                                viewportTranslation = Vector2f(editorState.viewportTranslation.x, editorState.viewportTranslation.y),
                                viewportScale = editorState.viewportScale
                            )

                            val distance = distanceToLineSegment(
                                point = offset,
                                start = screenStart,
                                end = screenEnd
                            )

                            if (distance < 15f) {
                                onAction(com.example.glimmerseed.editorcore.editor.EditorAction.SelectBone(bone.id))
                                return@detectTapGestures
                            }
                        }

                        onAction(com.example.glimmerseed.editorcore.editor.EditorAction.SelectBone(null))
                    }
                }
            }
            .pointerInteropFilter { event ->
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (event.pointerCount >= 2) {
                            lastZoom = 1f
                            lastPointerDistance = getPointerDistance(event)
                            true
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (event.pointerCount >= 2) {
                            try {
                                val currentDistance = getPointerDistance(event)
                                if (lastPointerDistance > 0f) {
                                    val scaleFactor = currentDistance / lastPointerDistance
                                    val centerX = (event.getX(0) + event.getX(1)) / 2f
                                    val centerY = (event.getY(0) + event.getY(1)) / 2f

                                    onAction(
                                        com.example.glimmerseed.editorcore.editor.EditorAction.ScaleViewport(
                                            scaleFactor,
                                            Vector2f(centerX, centerY)
                                        )
                                    )
                                    lastPointerDistance = currentDistance
                                }
                            } catch (_: Exception) {
                            }
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
    ) {
        if (editorState.showGrid) {
            drawGrid(
                viewportTranslation = Vector2f(editorState.viewportTranslation.x, editorState.viewportTranslation.y),
                viewportScale = editorState.viewportScale
            )
        }

        val skeleton = editorState.skeleton
        if (editorState.onionSkinEnabled && skeleton != null) {
            drawOnionSkins(
                editorState = editorState,
                skeleton = skeleton,
                viewportTranslation = Vector2f(editorState.viewportTranslation.x, editorState.viewportTranslation.y),
                viewportScale = editorState.viewportScale
            )
        }

        editorState.skeleton?.let { skeleton ->
            drawSkeleton(
                skeleton = skeleton,
                selectedBoneId = editorState.selectedBoneId,
                viewportTranslation = Vector2f(editorState.viewportTranslation.x, editorState.viewportTranslation.y),
                viewportScale = editorState.viewportScale
            )
        }
    }

    if (editorState.showSkeletalPreview && editorState.skeleton != null && editorState.mesh != null) {
        SkinningPreviewOverlay(
            skeleton = editorState.skeleton!!,
            mesh = editorState.mesh!!,
            offsetX = editorState.viewportTranslation.x,
            offsetY = editorState.viewportTranslation.y,
            scale = editorState.viewportScale
        )
    }
}

@Composable
private fun SkinningPreviewOverlay(
    skeleton: com.example.glimmerseed.editorcore.animation.Skeleton,
    mesh: Mesh,
    offsetX: Float,
    offsetY: Float,
    scale: Float
) {
    AndroidView(
        factory = { context ->
            SkinningPreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { view ->
            view.updatePreview(
                skeleton = skeleton,
                mesh = mesh,
                offsetX = offsetX,
                offsetY = offsetY,
                scale = scale
            )
        }
    )
}

private fun getPointerDistance(event: MotionEvent): Float {
    if (event.pointerCount < 2) return 0f
    val dx = event.getX(0) - event.getX(1)
    val dy = event.getY(0) - event.getY(1)
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

private fun screenToWorld(
    screenPoint: Offset,
    viewportTranslation: Vector2f,
    viewportScale: Float
): Vector2f {
    val safeScale = viewportScale.coerceAtLeast(0.001f)
    return Vector2f(
        (screenPoint.x - viewportTranslation.x) / safeScale,
        (screenPoint.y - viewportTranslation.y) / safeScale
    )
}

private fun worldToScreen(
    worldPoint: Vector2f,
    viewportTranslation: Vector2f,
    viewportScale: Float
): Offset {
    return Offset(
        worldPoint.x * viewportScale + viewportTranslation.x,
        worldPoint.y * viewportScale + viewportTranslation.y
    )
}

private fun distanceToLineSegment(
    point: Offset,
    start: Offset,
    end: Offset
): Float {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lengthSquared = dx * dx + dy * dy

    if (lengthSquared == 0f) {
        return kotlin.math.sqrt((point.x - start.x) * (point.x - start.x) + (point.y - start.y) * (point.y - start.y))
    }

    var t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared
    t = max(0f, kotlin.math.min(1f, t))

    val closestX = start.x + t * dx
    val closestY = start.y + t * dy

    return kotlin.math.sqrt((point.x - closestX) * (point.x - closestX) + (point.y - closestY) * (point.y - closestY))
}

private fun DrawScope.drawSkeleton(
    skeleton: com.example.glimmerseed.editorcore.animation.Skeleton,
    selectedBoneId: Int?,
    viewportTranslation: Vector2f,
    viewportScale: Float
) {
    for (bone in skeleton.bones) {
        val parentId = bone.parentId
        val parent = if (parentId != null) skeleton.getBoneById(parentId) else null

        val startPoint = if (parent != null) {
            val localPos = Vector2f(parent.localTransform.getTranslation(Vector3f()).x, parent.localTransform.getTranslation(Vector3f()).y)
            worldToScreen(localPos, viewportTranslation, viewportScale)
        } else {
            worldToScreen(Vector2f(0f, 0f), viewportTranslation, viewportScale)
        }

        val endPoint = Vector2f(
            bone.localTransform.getTranslation(Vector3f()).x,
            bone.localTransform.getTranslation(Vector3f()).y
        )
        val screenEnd = worldToScreen(endPoint, viewportTranslation, viewportScale)

        val boneColor = if (bone.id == selectedBoneId) BONE_SELECTED_COLOR else BONE_NORMAL_COLOR
        val boneWidth = if (bone.id == selectedBoneId) 5f else 3f

        drawLine(
            color = boneColor,
            start = startPoint,
            end = screenEnd,
            strokeWidth = boneWidth
        )

        val jointColor = if (bone.id == selectedBoneId) JOINT_SELECTED_COLOR else JOINT_NORMAL_COLOR
        val jointRadius = if (bone.id == selectedBoneId) 8f else 5f

        drawCircle(
            color = jointColor,
            radius = jointRadius,
            center = screenEnd
        )
    }
}

private fun DrawScope.drawGrid(
    viewportTranslation: Vector2f,
    viewportScale: Float
) {
    val gridSize = 50f * viewportScale
    val strokeWidth = 0.5f

    val startX = (viewportTranslation.x % gridSize) - if (viewportTranslation.x > 0) 0f else gridSize
    val startY = (viewportTranslation.y % gridSize) - if (viewportTranslation.y > 0) 0f else gridSize

    var x = startX
    while (x < size.width) {
        drawLine(
            color = GRID_COLOR,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = strokeWidth
        )
        x += gridSize
    }

    var y = startY
    while (y < size.height) {
        drawLine(
            color = GRID_COLOR,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth
        )
        y += gridSize
    }

    drawLine(
        color = AXIS_X_COLOR,
        start = Offset(0f, viewportTranslation.y),
        end = Offset(size.width, viewportTranslation.y),
        strokeWidth = 2f
    )

    drawLine(
        color = AXIS_Y_COLOR,
        start = Offset(viewportTranslation.x, 0f),
        end = Offset(viewportTranslation.x, size.height),
        strokeWidth = 2f
    )

    drawCircle(
        color = ORIGIN_COLOR,
        radius = 4f,
        center = Offset(viewportTranslation.x, viewportTranslation.y)
    )
}

private fun DrawScope.drawOnionSkins(
    editorState: com.example.glimmerseed.editorcore.editor.EditorState,
    skeleton: com.example.glimmerseed.editorcore.animation.Skeleton,
    viewportTranslation: Vector2f,
    viewportScale: Float
) {
    val frameDuration = 1f / 30f

    for (i in 1..editorState.onionSkinPreviousFrames) {
        val timeOffset = -i * frameDuration
        val opacity = editorState.onionSkinOpacity * (1f - (i - 1) / (editorState.onionSkinPreviousFrames + 1).toFloat())
        drawOnionSkeleton(
            skeleton = skeleton,
            viewportTranslation = viewportTranslation,
            viewportScale = viewportScale,
            color = Color(0xFF90CAF9),
            opacity = opacity
        )
    }

    for (i in 1..editorState.onionSkinNextFrames) {
        val timeOffset = i * frameDuration
        val opacity = editorState.onionSkinOpacity * (1f - (i - 1) / (editorState.onionSkinNextFrames + 1).toFloat())
        drawOnionSkeleton(
            skeleton = skeleton,
            viewportTranslation = viewportTranslation,
            viewportScale = viewportScale,
            color = Color(0xFFEF9A9A),
            opacity = opacity
        )
    }
}

private fun DrawScope.drawOnionSkeleton(
    skeleton: com.example.glimmerseed.editorcore.animation.Skeleton,
    viewportTranslation: Vector2f,
    viewportScale: Float,
    color: Color,
    opacity: Float
) {
    val finalColor = color.copy(alpha = opacity)

    for (bone in skeleton.bones) {
        val parentId = bone.parentId
        val parent = if (parentId != null) skeleton.getBoneById(parentId) else null

        val startPoint = if (parent != null) {
            val localPos = Vector2f(parent.localTransform.getTranslation(Vector3f()).x, parent.localTransform.getTranslation(Vector3f()).y)
            worldToScreen(localPos, viewportTranslation, viewportScale)
        } else {
            worldToScreen(Vector2f(0f, 0f), viewportTranslation, viewportScale)
        }

        val endPoint = Vector2f(
            bone.localTransform.getTranslation(Vector3f()).x,
            bone.localTransform.getTranslation(Vector3f()).y
        )
        val screenEnd = worldToScreen(endPoint, viewportTranslation, viewportScale)

        drawLine(
            color = finalColor,
            start = startPoint,
            end = screenEnd,
            strokeWidth = 2f
        )

        drawCircle(
            color = finalColor,
            radius = 3f,
            center = screenEnd
        )
    }
}