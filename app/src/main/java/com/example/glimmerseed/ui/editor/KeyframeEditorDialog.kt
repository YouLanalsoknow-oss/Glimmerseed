package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.GlimmerseedTextField
import com.example.glimmerseed.ui.common.Icon

/**
 * 关键帧编辑对话框
 * 包含时间轴滚动条和关键帧设置
 */
@Composable
fun KeyframeEditorDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    initialTime: Float = 0f,
    duration: Float = 10f,
    existingKeyframes: List<Keyframe> = emptyList(),
    onSave: (Float) -> Unit,
    uiScale: Float = 1f
) {
    if (!showDialog) return

    var currentTime by remember { mutableFloatStateOf(initialTime) }
    val timeAxisState = rememberTimeAxisState(
        initialDuration = duration,
        initialCurrentTime = initialTime,
        initialPixelsPerSecond = 100f
    )

    // 同步时间
    LaunchedEffect(timeAxisState.currentTime) {
        currentTime = timeAxisState.currentTime
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = (20 * uiScale).dp),
            shape = RoundedCornerShape((16 * uiScale).dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0E8)),
            elevation = CardDefaults.cardElevation(defaultElevation = (8 * uiScale).dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding((16 * uiScale).dp)
            ) {
                // 标题栏
                Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "编辑关键帧",
                    fontSize = (18 * uiScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size((32 * uiScale).dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = "关闭",
                        tint = Color(0xFF5D4037),
                        modifier = Modifier.size((24 * uiScale).dp)
                    )
                }
            }

                Spacer(modifier = Modifier.height((16 * uiScale).dp))

                // 时间显示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE6DD)),
                    shape = RoundedCornerShape((8 * uiScale).dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC2B2A1))
                ) {
                    Row(
                        modifier = Modifier.padding((12 * uiScale).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "当前时间",
                                fontSize = (12 * uiScale).sp,
                                color = Color(0xFF795548)
                            )
                            Text(
                                text = formatTime(currentTime),
                                fontSize = (20 * uiScale).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                        }
                        // 时间输入框
                        GlimmerseedTextField(
                            value = "%.2f".format(currentTime),
                            onValueChange = { newValue ->
                                newValue.toFloatOrNull()?.let { time ->
                                    currentTime = time.coerceIn(0f, duration)
                                    timeAxisState.currentTime = currentTime
                                }
                            },
                            modifier = Modifier.width((120 * uiScale).dp),
                            placeholder = "秒",
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8D6E63),
                                unfocusedBorderColor = Color(0xFFC2B2A1),
                                focusedLabelColor = Color(0xFF8D6E63),
                                unfocusedLabelColor = Color(0xFF795548)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height((16 * uiScale).dp))

                // 时间轴组件
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F0E8)),
                    shape = RoundedCornerShape((8 * uiScale).dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC2B2A1))
                ) {
                    Column(
                        modifier = Modifier.padding((12 * uiScale).dp)
                    ) {
                        Text(
                            text = "时间轴",
                            fontSize = (14 * uiScale).sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height((8 * uiScale).dp))
                        
                        // 时间轴标尺
                        FolderTimeAxis(
                            state = timeAxisState,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height((8 * uiScale).dp))
                        
                        // 关键帧轨道
                        KeyframeTrack(
                            state = timeAxisState,
                            keyframes = existingKeyframes,
                            onKeyframeSelected = { keyframe ->
                                keyframe?.let {
                                    currentTime = it.time
                                    timeAxisState.setCurrentTimeAndEnsureVisible(it.time, 1000f)
                                }
                            },
                            onKeyframeMoved = { _, newTime ->
                                // 暂时不处理移动
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height((20 * uiScale).dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5D4037)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC2B2A1))
                    ) {
                        Text("取消", fontSize = (14 * uiScale).sp)
                    }
                    Button(
                onClick = { onSave(currentTime) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6E63))
            ) {
                Icon(
                    imageVector = AppIcons.Save,
                    contentDescription = null,
                    modifier = Modifier.size((18 * uiScale).dp)
                )
                Spacer(modifier = Modifier.width((8 * uiScale).dp))
                Text("保存", fontSize = (14 * uiScale).sp)
            }
                }
            }
        }
    }
}

/**
 * 格式化时间显示
 */
private fun formatTime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    val fraction = ((seconds - totalSeconds) * 100).toInt()
    return "%02d:%02d.%02d".format(mins, secs, fraction)
}
