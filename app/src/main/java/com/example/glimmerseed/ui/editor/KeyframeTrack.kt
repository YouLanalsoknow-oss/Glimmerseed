package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.example.glimmerseed.ui.dimens.AppDimens
import com.example.glimmerseed.ui.utils.TimeAxisDimens
import com.example.glimmerseed.ui.utils.getAdaptiveTimeAxisDimens

data class Keyframe(
    val time: Float,
    val type: KeyframeType = KeyframeType.NORMAL,
    val isSelected: Boolean = false
)

enum class KeyframeType {
    NORMAL, LINEAR, BEZIER, STEP
}

@Composable
fun KeyframeTrack(
    state: TimeAxisState,
    keyframes: List<Keyframe>,
    onKeyframeSelected: (Keyframe?) -> Unit,
    onKeyframeMoved: (Keyframe, Float) -> Unit,
    modifier: Modifier = Modifier,
    dimens: TimeAxisDimens = getAdaptiveTimeAxisDimens(), // 自适应尺寸
    backgroundColor: Color = Color(0xFFF5F0E8),
    lineColor: Color = Color(0xFFC2B2A1),
    keyframeColor: Color = Color(0xFF8D6E63),
    selectedKeyframeColor: Color = Color(0xFFFFB300)
) {
    val isPreview = LocalInspectionMode.current
    val trackHeightPx = with(LocalDensity.current) { dimens.trackHeight.toPx() }
    val lineThinPx = with(LocalDensity.current) { AppDimens.LineThin.toPx() }
    val lineNormalPx = with(LocalDensity.current) { AppDimens.LineNormal.toPx() }
    var draggedKeyframe: Keyframe? by remember { mutableStateOf(null) }

    val visibleKeyframes by remember(keyframes, state.visibleTimeRange) {
        derivedStateOf { keyframes.filter { it.time in state.visibleTimeRange } }
    }

    Canvas(
        modifier = modifier
            .height(dimens.trackHeight)
            .fillMaxWidth()
            .then(
                if (!isPreview) {
                    Modifier.pointerInput(state, keyframes) {
                        detectTapGestures { offset ->
                            val clicked = visibleKeyframes.firstOrNull {
                                val x = state.timeToPixel(it.time)
                                kotlin.math.abs(offset.x - x) < 12f
                            }
                            onKeyframeSelected(clicked)
                        }
                    }
                    .pointerInput(state, keyframes) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                draggedKeyframe = visibleKeyframes.firstOrNull {
                                    val x = state.timeToPixel(it.time)
                                    kotlin.math.abs(offset.x - x) < 12f
                                }
                            },
                            onDrag = { change, _ ->
                                draggedKeyframe?.let {
                                    val newTime = state.pixelToTime(change.position.x).coerceIn(0f, state.duration)
                                    onKeyframeMoved(it, newTime)
                                }
                            },
                            onDragEnd = { draggedKeyframe = null }
                        )
                    }
                } else Modifier
            )
    ) {
        drawRect(backgroundColor)
        drawLine(
            color = lineColor,
            start = Offset(0f, trackHeightPx),
            end = Offset(size.width, trackHeightPx),
            strokeWidth = lineThinPx
        )

        // 连接线
        if (visibleKeyframes.size >= 2) {
            val points = visibleKeyframes.map {
                Offset(state.timeToPixel(it.time), trackHeightPx / 2)
            }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = keyframeColor.copy(alpha = 0.5f),
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = lineNormalPx
                )
            }
        }

        // 绘制关键帧
        visibleKeyframes.forEach { keyframe ->
            drawKeyframe(
                keyframe = keyframe,
                state = state,
                trackHeightPx = trackHeightPx,
                keyframeColor = keyframeColor,
                selectedColor = selectedKeyframeColor
            )
        }
    }
}

private fun DrawScope.drawKeyframe(
    keyframe: Keyframe,
    state: TimeAxisState,
    trackHeightPx: Float,
    keyframeColor: Color,
    selectedColor: Color
) {
    val x = state.timeToPixel(keyframe.time)
    val y = trackHeightPx / 2
    if (x < -20f || x > size.width + 20f) return

    val color = if (keyframe.isSelected) selectedColor else keyframeColor
    val shapeSize = 14f // 适当放大关键帧，便于点击，不拥挤

    when (keyframe.type) {
        KeyframeType.NORMAL -> drawCircle(color, shapeSize / 2, Offset(x, y))
        KeyframeType.LINEAR -> drawDiamond(x, y, shapeSize, color)
        KeyframeType.BEZIER -> drawRect(color, Offset(x - shapeSize / 2, y - shapeSize / 2), Size(shapeSize, shapeSize))
        KeyframeType.STEP -> drawTriangleKeyframe(x, y, shapeSize, color)
    }

    if (keyframe.isSelected) {
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = shapeSize / 2 + 4f,
            center = Offset(x, y),
            style = androidx.compose.ui.graphics.drawscope.Stroke(2f)
        )
    }
}

// 辅助图形
private fun DrawScope.drawDiamond(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy - size / 2)
        lineTo(cx + size / 2, cy)
        lineTo(cx, cy + size / 2)
        lineTo(cx - size / 2, cy)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawTriangleKeyframe(cx: Float, cy: Float, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy - size / 2)
        lineTo(cx + size / 2, cy + size / 2)
        lineTo(cx - size / 2, cy + size / 2)
        close()
    }
    drawPath(path, color)
}
