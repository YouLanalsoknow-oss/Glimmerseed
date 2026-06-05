package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.ui.dimens.AppDimens
import com.example.glimmerseed.ui.utils.TimeAxisDimens
import com.example.glimmerseed.ui.utils.getAdaptiveTimeAxisDimens
import kotlin.math.roundToInt

/**
 * 判断是否在预览环境中
 */
@Composable
fun isInPreview(): Boolean {
    return LocalInspectionMode.current
}

data class TimeSegment(
    val startTime: Float,
    val endTime: Float,
    val unit: TimeAxisState.TimeUnit,
    val level: Int,
    val isExpanded: Boolean,
    val children: List<TimeSegment> = emptyList()
)

@Composable
fun FolderTimeAxis(
    state: TimeAxisState,
    modifier: Modifier = Modifier,
    dimens: TimeAxisDimens = getAdaptiveTimeAxisDimens(),
    backgroundColor: Color = Color(0xFFF5F0E8),
    lineColor: Color = Color(0xFFC2B2A1),
    highlightColor: Color = Color(0xFFFFB300),
    textColor: Color = Color(0xFF5D4037)
) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 12.sp, color = textColor)
    val isPreview = LocalInspectionMode.current
    // dp 安全转换
    val folderLevelOffsetPx = with(LocalDensity.current) { dimens.folderOffset.toPx() }
    val lineThinPx = with(LocalDensity.current) { AppDimens.LineThin.toPx() }
    val lineNormalPx = with(LocalDensity.current) { AppDimens.LineNormal.toPx() }

    val timeSegments by remember(state.pixelsPerSecond, state.scrollOffset, state.duration) {
        derivedStateOf { generateTimeSegments(state) }
    }

    Canvas(
        modifier = modifier
            .height(dimens.rulerHeight) // 统一高度
            .fillMaxWidth()
            // 预览禁用手势，真机保留
            .then(
                if (!isPreview) {
                    Modifier.pointerInput(state) {
                        detectTapGestures { offset ->
                            val time = state.pixelToTime(offset.x)
                            state.setCurrentTimeAndEnsureVisible(time, size.width.toFloat())
                        }
                    }
                    .pointerInput(state) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val playheadX = state.timeToPixel(state.currentTime)
                                if (kotlin.math.abs(offset.x - playheadX) < 10f) {
                                    state.isDraggingPlayhead = true
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (state.isDraggingPlayhead) {
                                    val time = state.pixelToTime(change.position.x)
                                    state.currentTime = time.coerceIn(0f, state.duration)
                                } else {
                                    state.scroll(-dragAmount.x)
                                }
                            },
                            onDragEnd = { state.isDraggingPlayhead = false }
                        )
                    }
                } else Modifier
            )
    ) {
        drawRect(backgroundColor)

        // 绘制文件夹刻度（使用自适应层级偏移）
        timeSegments.forEach { segment ->
            drawTimeSegment(
                segment = segment,
                state = state,
                textMeasurer = textMeasurer,
                textStyle = textStyle,
                lineColor = lineColor,
                highlightColor = highlightColor,
                levelOffsetPx = folderLevelOffsetPx,
                lineThinPx = lineThinPx
            )
        }

        // 播放头
        val playheadX = state.timeToPixel(state.currentTime)
        if (playheadX in 0f..size.width) {
            drawLine(
                color = highlightColor,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, size.height),
                strokeWidth = lineNormalPx
            )
            drawTriangle(
                color = highlightColor,
                topLeft = Offset(playheadX - 8f, 0f),
                size = Size(16f, 8f)
            )
        }

        // 底部分割线
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height - lineThinPx),
            end = Offset(size.width, size.height - lineThinPx),
            strokeWidth = lineThinPx
        )
    }
}

// 内部绘制方法：接收外部尺寸，不再硬编码
private fun DrawScope.drawTimeSegment(
    segment: TimeSegment,
    state: TimeAxisState,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    lineColor: Color,
    highlightColor: Color,
    levelOffsetPx: Float,
    lineThinPx: Float
) {
    val startX = state.timeToPixel(segment.startTime)
    val endX = state.timeToPixel(segment.endTime)
    val segmentWidth = endX - startX

    if (endX < 0 || startX > size.width || segmentWidth <= 0f) return

    // 层级高度（按配置偏移，防拥挤）
    val top = segment.level * levelOffsetPx
    val height = levelOffsetPx - 4f

    // 文件夹背景+边框
    val radiusSmallPx = AppDimens.RadiusSmall.toPx()
    drawRoundRect(
        color = Color(0xFFEDE6DD),
        topLeft = Offset(startX + 2f, top + 2f),
        size = Size(segmentWidth - 4f, height - 2f),
        cornerRadius = CornerRadius(radiusSmallPx, radiusSmallPx)
    )
    drawRoundRect(
        color = lineColor,
        topLeft = Offset(startX + 2f, top + 2f),
        size = Size(segmentWidth - 4f, height - 2f),
        cornerRadius = CornerRadius(radiusSmallPx, radiusSmallPx),
        style = Stroke(lineThinPx)
    )

    // 文字绘制（增加宽度校验，防崩溃+防重叠）
    val label = getSegmentLabel(segment)
    val textLayout = textMeasurer.measure(label, textStyle)
    val minTextWidth = textLayout.size.width + 32f // 加大左右留白

    if (segmentWidth >= minTextWidth) {
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                startX + (segmentWidth - textLayout.size.width) / 2,
                top + (height - textLayout.size.height) / 2
            )
        )
    }

    // 箭头指示器
    drawExpandIndicator(segment, startX, top, height, textStyle.color)

    // 递归限制层级，防爆栈+过度绘制
    if (segment.level < 3) {
        segment.children.forEach { child ->
            drawTimeSegment(
                child, state, textMeasurer, textStyle,
                lineColor, highlightColor, levelOffsetPx, lineThinPx
            )
        }
    }
}

// 辅助：获取标签
private fun getSegmentLabel(segment: TimeSegment): String {
    return when (segment.unit) {
        TimeAxisState.TimeUnit.FRAME -> "${(segment.startTime * 30).roundToInt()}帧"
        TimeAxisState.TimeUnit.SECOND -> "${segment.startTime.roundToInt()}秒"
        TimeAxisState.TimeUnit.THIRTY_SECONDS -> "${segment.startTime.roundToInt()}-${segment.endTime.roundToInt()}秒"
        TimeAxisState.TimeUnit.MINUTE -> "${(segment.startTime / 60).roundToInt()}分钟"
    }
}

// 辅助：绘制展开箭头
private fun DrawScope.drawExpandIndicator(
    segment: TimeSegment,
    startX: Float,
    top: Float,
    height: Float,
    color: Color
) {
    val centerY = top + height / 2
    val indicatorX = startX + 8f
    val stroke = 1.5f

    if (segment.isExpanded) {
        drawLine(color, Offset(indicatorX, centerY - 3f), Offset(indicatorX + 6f, centerY + 3f), stroke)
        drawLine(color, Offset(indicatorX + 6f, centerY + 3f), Offset(indicatorX + 12f, centerY - 3f), stroke)
    } else {
        drawLine(color, Offset(indicatorX + 3f, centerY - 3f), Offset(indicatorX + 9f, centerY), stroke)
        drawLine(color, Offset(indicatorX + 9f, centerY), Offset(indicatorX + 3f, centerY + 3f), stroke)
    }
}

// 三角形绘制
private fun DrawScope.drawTriangle(color: Color, topLeft: Offset, size: Size) {
    val path = Path().apply {
        moveTo(topLeft.x, topLeft.y)
        lineTo(topLeft.x + size.width, topLeft.y)
        lineTo(topLeft.x + size.width / 2, topLeft.y + size.height)
        close()
    }
    drawPath(path, color)
}

// 生成时间段的辅助函数
private fun generateTimeSegments(state: TimeAxisState): List<TimeSegment> {
    val visibleStart = state.visibleTimeRange.start
    val visibleEnd = state.visibleTimeRange.endInclusive

    val maxExpandLevel = when (state.currentTimeUnit) {
        TimeAxisState.TimeUnit.FRAME -> 3
        TimeAxisState.TimeUnit.SECOND -> 2
        TimeAxisState.TimeUnit.THIRTY_SECONDS -> 1
        TimeAxisState.TimeUnit.MINUTE -> 0
    }

    return generateSegmentsRecursive(
        startTime = 0f,
        endTime = state.duration,
        unit = TimeAxisState.TimeUnit.MINUTE,
        level = 0,
        maxExpandLevel = maxExpandLevel,
        visibleStart = visibleStart,
        visibleEnd = visibleEnd
    )
}

private fun generateSegmentsRecursive(
    startTime: Float,
    endTime: Float,
    unit: TimeAxisState.TimeUnit,
    level: Int,
    maxExpandLevel: Int,
    visibleStart: Float,
    visibleEnd: Float
): List<TimeSegment> {
    val segments = mutableListOf<TimeSegment>()
    val segmentDuration = unit.value

    var currentTime = kotlin.math.floor(startTime / segmentDuration) * segmentDuration
    while (currentTime < endTime) {
        val segmentEnd = currentTime + segmentDuration
        val isVisible = segmentEnd > visibleStart && currentTime < visibleEnd

        if (isVisible) {
            val isExpanded = level < maxExpandLevel
            val children = if (isExpanded && level < 3) {
                val nextUnit = when (unit) {
                    TimeAxisState.TimeUnit.MINUTE -> TimeAxisState.TimeUnit.THIRTY_SECONDS
                    TimeAxisState.TimeUnit.THIRTY_SECONDS -> TimeAxisState.TimeUnit.SECOND
                    TimeAxisState.TimeUnit.SECOND -> TimeAxisState.TimeUnit.FRAME
                    TimeAxisState.TimeUnit.FRAME -> null
                }

                nextUnit?.let {
                    generateSegmentsRecursive(
                        startTime = currentTime,
                        endTime = segmentEnd,
                        unit = it,
                        level = level + 1,
                        maxExpandLevel = maxExpandLevel,
                        visibleStart = visibleStart,
                        visibleEnd = visibleEnd
                    )
                } ?: emptyList()
            } else {
                emptyList()
            }

            segments.add(
                TimeSegment(
                    startTime = currentTime,
                    endTime = segmentEnd,
                    unit = unit,
                    level = level,
                    isExpanded = isExpanded,
                    children = children
                )
            )
        }

        currentTime += segmentDuration
    }

    return segments
}

// 纯展示UI，所有参数外部传入，预览安全
@Composable
fun FolderTimeAxisPreviewUI(
    timeAxisState: TimeAxisState,
    tracks: List<List<Keyframe>>,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        FolderTimeAxis(
            state = timeAxisState,
            modifier = Modifier
                .fillMaxWidth()
        )
        tracks.forEach { keyframes ->
            KeyframeTrack(
                state = timeAxisState,
                keyframes = keyframes,
                onKeyframeSelected = {},
                onKeyframeMoved = { _, _ -> },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, name = "时间轴预览")
@Composable
fun TimeAxisSafePreview() {
    MaterialTheme {
        // 预览用固定状态，不走 remember
        val previewState = TimeAxisState(
            initialDuration = 10f,
            initialCurrentTime = 3f,
            initialPixelsPerSecond = 50f
        )
        val previewTracks = listOf(
            listOf(
                Keyframe(0f),
                Keyframe(1.5f),
                Keyframe(3f),
                Keyframe(6f),
                Keyframe(9f)
            )
        )

        FolderTimeAxisPreviewUI(
            timeAxisState = previewState,
            tracks = previewTracks,
            modifier = Modifier.fillMaxSize()
        )
    }
}
