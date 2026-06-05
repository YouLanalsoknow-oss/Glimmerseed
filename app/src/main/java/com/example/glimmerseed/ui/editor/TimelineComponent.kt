package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.editor.EditorViewModel
import com.example.glimmerseed.editorcore.editor.TimelineState
import com.example.glimmerseed.editorcore.editor.TimelineViewModel
import org.joml.Vector2f

/**
 * 时间轴组件
 * 显示时间标尺、播放头和关键帧轨道
 */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
@Composable
fun TimelineComponent(
    modifier: Modifier = Modifier,
    editorViewModel: EditorViewModel,
    timelineViewModel: TimelineViewModel
) {
    val editorState by editorViewModel.state.collectAsState()
    val timelineState by timelineViewModel.timelineState.collectAsState()
    val textMeasurer = rememberTextMeasurer()
    
    // 如果编辑器有动画，通知时间轴ViewModel
    editorState.currentAnimation?.let { animation ->
        timelineViewModel.setAnimation(animation)
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            // 处理拖拽
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    if (dragAmount.x != 0f) {
                        timelineViewModel.pan(-dragAmount.x, -dragAmount.y)
                    }
                }
            }
            // 处理点击
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    handleTimelineClick(
                        offset = offset,
                        editorState = editorState,
                        timelineState = timelineState,
                        onSeek = { time ->
                            editorViewModel.dispatch(EditorAction.SetCurrentTime(time))
                        }
                    )
                }
            }
    ) {
        drawTimelineBackground()
        
        // 绘制时间标尺
        drawTimeRuler(
            timelineState = timelineState,
            textMeasurer = textMeasurer
        )
        
        // 绘制轨道
        drawTracks(
            editorState = editorState,
            timelineState = timelineState
        )
        
        // 绘制播放头
        drawPlayhead(
            editorState = editorState,
            timelineState = timelineState
        )
    }
}

/**
 * 绘制时间轴背景
 */
private fun DrawScope.drawTimelineBackground() {
    drawRect(color = Color(0xFF2A2A2A))
}

/**
 * 绘制时间标尺
 */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun DrawScope.drawTimeRuler(
    timelineState: TimelineState,
    textMeasurer: TextMeasurer
) {
    val topY = 16f
    val height = 48f
    val scale = timelineState.viewportScale.coerceAtLeast(0.001f)
    val offset = timelineState.viewportOffset.x
    
    // 绘制标尺背景
    drawRect(
        color = Color(0xFF3A3A3A),
        topLeft = Offset(0f, 0f),
        size = androidx.compose.ui.geometry.Size(size.width, height)
    )
    
    // 计算刻度的间隔（秒）
    val targetInterval = 50f * scale // 目标像素间隔
    var interval = when {
        targetInterval > 200f -> 1f
        targetInterval > 100f -> 0.5f
        targetInterval > 50f -> 0.25f
        else -> 0.1f
    }
    
    // 绘制刻度
    val startTime = (-offset / scale / 60f).toInt() * interval
    val endTime = ((size.width - offset) / scale / 60f).toInt() * interval + interval
    
    var time = startTime
    while (time <= endTime) {
        val x = time * 60f * scale + offset
        if (x >= -10f && x <= size.width + 10f) {
            // 主刻度
            drawLine(
                color = Color(0xFF888888),
                start = Offset(x, topY),
                end = Offset(x, 0f),
                strokeWidth = 1f
            )
            
            // 绘制时间标签
            val label = String.format("%.1f", time)
            val textLayoutResult = textMeasurer.measure(
                label,
                style = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFFCCCCCC),
                    fontSize = 10.sp
                )
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x - textLayoutResult.size.width / 2,
                    4f
                )
            )
        }
        time += interval
    }
}

/**
 * 绘制轨道
 */
private fun DrawScope.drawTracks(
    editorState: EditorState,
    timelineState: TimelineState
) {
    val animation = editorState.currentAnimation ?: return
    val skeleton = editorState.skeleton ?: return
    val trackHeight = timelineState.trackHeight
    val scale = timelineState.viewportScale.coerceAtLeast(0.001f)
    val offset = timelineState.viewportOffset
    
    // 绘制每个骨骼的轨道
    var trackY = 56f
    for (bone in skeleton.bones) {
        val keyframes = animation.boneKeyframes[bone.id] ?: emptyList()
        
        // 绘制轨道背景
        drawRect(
            color = Color(0xFF2A2A2A),
            topLeft = Offset(0f, trackY),
            size = androidx.compose.ui.geometry.Size(size.width, trackHeight)
        )
        
        // 绘制轨道分割线
        drawLine(
            color = Color(0xFF444444),
            start = Offset(0f, trackY),
            end = Offset(size.width, trackY),
            strokeWidth = 1f
        )
        
        // 绘制关键帧
        for (keyframe in keyframes) {
            val x = keyframe.time * 60f * scale + offset.x
            if (x >= -10f && x <= size.width + 10f) {
                drawKeyframe(
                    position = Offset(x, trackY + trackHeight / 2f),
                    isSelected = timelineState.selectedKeyframe?.boneId == bone.id
                            && timelineState.selectedKeyframe?.time == keyframe.time
                )
            }
        }
        
        trackY += trackHeight
    }
}

/**
 * 绘制单个关键帧
 */
private fun DrawScope.drawKeyframe(
    position: Offset,
    isSelected: Boolean
) {
    val radius = 8f
    
    // 关键帧圆
    drawCircle(
        color = if (isSelected) Color(0xFFFFD700) else Color(0xFFFF6B6B),
        radius = radius,
        center = position
    )
    
    // 外圈轮廓
    drawCircle(
        color = if (isSelected) Color(0xFFFF6B00) else Color(0xFFFF4444),
        radius = radius,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
        center = position
    )
}

/**
 * 绘制播放头
 */
private fun DrawScope.drawPlayhead(
    editorState: EditorState,
    timelineState: TimelineState
) {
    val x = editorState.currentTime * 60f * timelineState.viewportScale + timelineState.viewportOffset.x
    
    // 绘制播放头垂直线
    drawLine(
        color = Color(0xFF00D4FF),
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = 2f
    )
    
    // 绘制播放头顶部标记
    drawTriangle(
        color = Color(0xFF00D4FF),
        center = Offset(x, 56f),
        size = 16f
    )
}

/**
 * 绘制三角形
 */
private fun DrawScope.drawTriangle(
    color: Color,
    center: Offset,
    size: Float
) {
    val halfSize = size / 2f
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - halfSize)
        lineTo(center.x - halfSize, center.y + halfSize)
        lineTo(center.x + halfSize, center.y + halfSize)
        close()
    }
    drawPath(
        path = path,
        color = color
    )
}

/**
 * 处理时间轴点击
 */
private fun handleTimelineClick(
    offset: Offset,
    editorState: EditorState,
    timelineState: TimelineState,
    onSeek: (time: Float) -> Unit
) {
    val scale = timelineState.viewportScale.coerceAtLeast(0.001f)
    val xOffset = timelineState.viewportOffset.x
    
    // 计算点击的时间
    val time = (offset.x - xOffset) / scale / 60f
    onSeek(time)
}
