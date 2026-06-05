package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.Icon

/**
 * 关键帧文件夹时间轴
 * 只显示包含关键帧的时间分段
 */
@Composable
fun FolderTimeline(
    keyframes: List<Keyframe> = emptyList(),
    selectedTime: Float? = null,
    onTimeSelected: (Float) -> Unit = {},
    onAddKeyframe: (Float) -> Unit = {},
    uiScale: Float = 1f
) {
    // 构建关键帧文件夹树
    val keyframeTree by remember(keyframes) {
        derivedStateOf { buildKeyframeTree(keyframes) }
    }
    
    // 文件夹展开状态
    var expandedFolders by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // 切换文件夹展开状态
    val toggleFolder = { folderId: String ->
        expandedFolders = if (expandedFolders.contains(folderId)) {
            expandedFolders - folderId
        } else {
            expandedFolders + folderId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E8))
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEDE6DD))
                .padding(
                    horizontal = (12 * uiScale).dp,
                    vertical = (8 * uiScale).dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
            ) {
                Icon(
                    imageVector = AppIcons.Menu,
                    contentDescription = null,
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size((16 * uiScale).dp)
                )
                Text(
                    text = "关键帧",
                    fontSize = (14 * uiScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
                Text(
                    text = "(${keyframes.size})",
                    fontSize = (12 * uiScale).sp,
                    color = Color(0xFF795548)
                )
            }
            
            // 添加关键帧按钮
            IconButton(
                onClick = { onAddKeyframe(selectedTime ?: 0f) },
                modifier = Modifier.size((32 * uiScale).dp)
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = "添加关键帧",
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.size((20 * uiScale).dp)
                )
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth().height(1.dp),
            color = Color(0xFFC2B2A1)
        )

        if (keyframes.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding((24 * uiScale).dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Folder,
                        contentDescription = null,
                        tint = Color(0xFFC2B2A1),
                        modifier = Modifier.size((48 * uiScale).dp)
                    )
                    Text(
                        text = "暂无关键帧",
                        fontSize = (14 * uiScale).sp,
                        color = Color(0xFF795548)
                    )
                    Text(
                        text = "点击上方 + 添加第一个关键帧",
                        fontSize = (12 * uiScale).sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        } else {
            // 关键帧文件夹树
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding((8 * uiScale).dp),
                verticalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
            ) {
                keyframeTree.forEach { folder ->
                    KeyframeFolderItem(
                        folder = folder,
                        expanded = expandedFolders.contains(folder.id),
                        selectedTime = selectedTime,
                        depth = 0,
                        uiScale = uiScale,
                        onToggle = { toggleFolder(folder.id) },
                        onTimeSelected = onTimeSelected
                    )
                }
            }
        }
    }
}

/**
 * 关键帧文件夹项
 */
@Composable
private fun KeyframeFolderItem(
    folder: KeyframeFolder,
    expanded: Boolean,
    selectedTime: Float?,
    depth: Int,
    uiScale: Float,
    onToggle: () -> Unit,
    onTimeSelected: (Float) -> Unit
) {
    Column {
        // 文件夹头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(
                    start = ((depth * 16 + 8) * uiScale).dp,
                    top = (6 * uiScale).dp,
                    bottom = (6 * uiScale).dp,
                    end = (8 * uiScale).dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            Icon(
                imageVector = if (expanded) AppIcons.FolderOpen else AppIcons.Folder,
                contentDescription = if (expanded) "收起" else "展开",
                tint = Color(0xFF8D6E63),
                modifier = Modifier.size((18 * uiScale).dp)
            )
            Text(
                text = folder.label,
                fontSize = (13 * uiScale).sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3E2723)
            )
            Spacer(modifier = Modifier.weight(1f))
            // 关键帧数量
            Surface(
                color = Color(0xFFFFECB3),
                shape = RoundedCornerShape((12 * uiScale).dp)
            ) {
                Text(
                    text = "${folder.keyframeCount}",
                    fontSize = (11 * uiScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(
                        horizontal = (8 * uiScale).dp,
                        vertical = (2 * uiScale).dp
                    )
                )
            }
        }

        // 展开的内容
        if (expanded) {
            // 子文件夹
            folder.subFolders.forEach { subFolder ->
                KeyframeFolderItem(
                    folder = subFolder,
                    expanded = true, // 默认展开
                    selectedTime = selectedTime,
                    depth = depth + 1,
                    uiScale = uiScale,
                    onToggle = {}, // 叶子文件夹不切换
                    onTimeSelected = onTimeSelected
                )
            }
            
            // 关键帧
            folder.keyframes.forEach { keyframe ->
                KeyframeItem(
                    keyframe = keyframe,
                    selected = selectedTime == keyframe.time,
                    depth = depth + 1,
                    uiScale = uiScale,
                    onClick = { onTimeSelected(keyframe.time) }
                )
            }
        }
    }
}

/**
 * 关键帧项
 */
@Composable
private fun KeyframeItem(
    keyframe: Keyframe,
    selected: Boolean,
    depth: Int,
    uiScale: Float,
    onClick: () -> Unit
) {
    val bgColor = if (selected) Color(0xFFFFECB3) else Color.Transparent
    val textColor = if (selected) Color(0xFFE65100) else Color(0xFF5D4037)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ((depth * 16 + 8) * uiScale).dp),
        color = bgColor,
        shape = RoundedCornerShape((4 * uiScale).dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = (12 * uiScale).dp,
                    vertical = (8 * uiScale).dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            // 关键帧图标
            Surface(
                shape = RoundedCornerShape((4 * uiScale).dp),
                color = if (selected) Color(0xFFFFB300) else Color(0xFF8D6E63),
                modifier = Modifier.size((12 * uiScale).dp)
            ) {}
            
            // 时间标签
            Text(
                text = formatKeyframeTime(keyframe.time),
                fontSize = (12 * uiScale).sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 关键帧类型
            Text(
                text = when (keyframe.type) {
                    KeyframeType.NORMAL -> "普通"
                    KeyframeType.LINEAR -> "线性"
                    KeyframeType.BEZIER -> "贝塞尔"
                    KeyframeType.STEP -> "步进"
                },
                fontSize = (10 * uiScale).sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

/**
 * 关键帧文件夹数据类
 */
data class KeyframeFolder(
    val id: String,
    val label: String,
    val startSecond: Int,
    val endSecond: Int,
    val keyframes: List<Keyframe> = emptyList(),
    val subFolders: List<KeyframeFolder> = emptyList()
) {
    val keyframeCount: Int
        get() = keyframes.size + subFolders.sumOf { it.keyframeCount }
}

/**
 * 构建关键帧文件夹树
 */
private fun buildKeyframeTree(keyframes: List<Keyframe>): List<KeyframeFolder> {
    if (keyframes.isEmpty()) return emptyList()
    
    // 按秒分组
    val secondsMap = keyframes.groupBy { it.time.toInt() }
    
    // 构建文件夹
    val folders = mutableListOf<KeyframeFolder>()
    
    secondsMap.forEach { (second, frames) ->
        // 检查是否可以合并到更大的时间段
        // 这里简化处理：每秒一个文件夹
        folders.add(
            KeyframeFolder(
                id = "sec_$second",
                label = "${second}秒",
                startSecond = second,
                endSecond = second + 1,
                keyframes = frames.sortedBy { it.time }
            )
        )
    }
    
    // 如果有很多文件夹，可以进一步按分钟分组
    return if (folders.size > 10) {
        // 按分钟分组
        val minuteMap = folders.groupBy { it.startSecond / 60 }
        minuteMap.map { (minute, secFolders) ->
            KeyframeFolder(
                id = "min_$minute",
                label = "${minute + 1}分钟",
                startSecond = minute * 60,
                endSecond = (minute + 1) * 60,
                subFolders = secFolders
            )
        }.sortedBy { it.startSecond }
    } else {
        folders.sortedBy { it.startSecond }
    }
}

/**
 * 格式化关键帧时间
 */
private fun formatKeyframeTime(seconds: Float): String {
    val mins = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    val fraction = ((seconds - seconds.toInt()) * 100).toInt()
    
    return if (mins > 0) {
        "%02d:%02d.%02d".format(mins, secs, fraction)
    } else {
        "%02d.%02d".format(secs, fraction)
    }
}
