package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.joml.Vector3f
import com.example.glimmerseed.ui.common.AppIcons

private val CARD_BG = Color(0xFFF5F0E8)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF5D4037)
private val TEXT_MUTED = Color(0xFF795548)
private val PANEL_BG = Color(0xFFEDE6DD)
private val ACCENT = Color(0xFF8D6E63)
private val SLIDER_ACTIVE = Color(0xFFFFB300)
private val SLIDER_INACTIVE = Color(0xFFD7CCC8)
private val KEYFRAME_COLOR = Color(0xFFE65100)
private const val FRAMES_PER_SEC = 24

@Composable
fun PropertyPanel(
    editorState: com.example.glimmerseed.editorcore.editor.EditorState,
    onAction: (com.example.glimmerseed.editorcore.editor.EditorAction) -> Unit,
    snapshotManager: com.example.glimmerseed.editorcore.snapshot.SnapshotManager,
    uiScale: Float = 1f
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=存档列表, 1=精修面板

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PANEL_BG)
    ) {
        // Tab 栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CARD_BG)
                .padding(horizontal = (8 * uiScale).dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton(
                text = "存档",
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                uiScale = uiScale
            )
            TabButton(
                text = "精修",
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                uiScale = uiScale
            )
        }

        // Tab 内容
        when (selectedTab) {
            0 -> SnapshotListTab(snapshotManager, onAction, uiScale)
            1 -> RefinePanelTab(editorState, onAction, uiScale)
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    uiScale: Float
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(vertical = (10 * uiScale).dp, horizontal = (16 * uiScale).dp)
            .clickable(onClick = onClick),
        style = MaterialTheme.typography.titleSmall,
        color = if (selected) ACCENT else TEXT_MUTED,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        fontSize = (14 * uiScale).sp
    )
}

/** 存档列表 Tab — 显示 PoseSnapshot 快照 + 插值配置 */
@Composable
private fun SnapshotListTab(
    snapshotManager: com.example.glimmerseed.editorcore.snapshot.SnapshotManager,
    onAction: (com.example.glimmerseed.editorcore.editor.EditorAction) -> Unit,
    uiScale: Float
) {
    val snapshots by remember { derivedStateOf { snapshotManager.getAllSnapshots() } }
    val sequence = remember { snapshotManager.currentSequence }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((12 * uiScale).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
    ) {
        // 顶部操作栏：捕获 + 序列信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = ACCENT.copy(alpha = 0.15f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ACCENT.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(com.example.glimmerseed.editorcore.editor.EditorAction.CaptureSnapshot) }
                        .padding((10 * uiScale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((6 * uiScale).dp)
                ) {
                    Icon(imageVector = AppIcons.Add, contentDescription = null, tint = ACCENT, modifier = Modifier.size((16 * uiScale).dp))
                    Text("捕获姿势", style = MaterialTheme.typography.bodyMedium, color = ACCENT, fontWeight = FontWeight.Medium)
                }
            }
            // 序列总时长和循环状态
            Surface(
                color = CARD_BG,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
            ) {
                Text(
                    text = "%.1fs | ${if (sequence.loop) "循环" else "单次"}".format(sequence.totalDuration),
                    modifier = Modifier.padding(horizontal = (10 * uiScale).dp, vertical = (6 * uiScale).dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TEXT_MUTED
                )
            }
        }

        if (snapshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                Text("暂无快照", style = MaterialTheme.typography.bodySmall, color = TEXT_MUTED)
            }
        } else {
            // 快照时间轴可视化
            SnapshotTimelineBar(snapshots = snapshots, sequence = sequence, uiScale = uiScale)

            // 快照卡片列表 + 区间插值配置
            snapshots.forEachIndexed { index, snap ->
                val nextSnap = if (index < snapshots.lastIndex) snapshots.getOrNull(index + 1) else null
                val transitionKey = if (nextSnap != null) "${snap.id}->${nextSnap.id}" else null
                val transition = if (transitionKey != null) sequence.transitions[transitionKey] else null

                EnhancedSnapshotCard(
                    snapshot = snap,
                    index = index,
                    totalCount = snapshots.size,
                    transition = transition,
                    onApply = {
                        // 应用此快照的姿势到编辑器（通过更新骨骼变换）
                        // 当前快照数据已存储在 SnapshotManager 中，UI 可读取显示
                    },
                    onDelete = { onAction(com.example.glimmerseed.editorcore.editor.EditorAction.RemoveSnapshot(snap.id)) },
                    onTransitionModeChange = { mode ->
                        if (nextSnap != null) {
                            onAction(com.example.glimmerseed.editorcore.editor.EditorAction.SetSnapshotTransition(snap.id, nextSnap.id, mode))
                        }
                    },
                    uiScale = uiScale
                )
            }
        }
    }
}

/** 快照时间轴可视化条 */
@Composable
private fun SnapshotTimelineBar(
    snapshots: List<com.example.glimmerseed.editorcore.snapshot.PoseSnapshot>,
    sequence: com.example.glimmerseed.editorcore.snapshot.PoseSequence,
    uiScale: Float
) {
    if (snapshots.isEmpty()) return

    val totalDuration = sequence.totalDuration.coerceAtLeast(0.01f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEDE6DD),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
    ) {
        Box(modifier = Modifier.padding(vertical = (12 * uiScale).dp, horizontal = (8 * uiScale).dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                snapshots.forEachIndexed { index, snap ->
                    val progress = (snap.timestamp / totalDuration).coerceIn(0f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // 时间点标记
                        Box(
                            modifier = Modifier
                                .size((10 * uiScale).dp)
                                .background(ACCENT, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.height((3 * uiScale).dp))
                        Text(
                            text = "%.1f".format(snap.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TEXT_MUTED,
                            fontSize = (9 * uiScale).sp
                        )
                        // 区间连线
                        if (index < snapshots.lastIndex) {
                            val nextProgress = (snapshots[index + 1].timestamp / totalDuration).coerceIn(0f, 1f)
                            val transKey = "${snap.id}->${snapshots[index + 1].id}"
                            val mode = sequence.transitions[transKey]?.mode
                                ?: com.example.glimmerseed.editorcore.snapshot.InterpolationMode.LINEAR
                            // 插值模式标签
                            Text(
                                text = when (mode) {
                                    com.example.glimmerseed.editorcore.snapshot.InterpolationMode.STEP -> "━"
                                    com.example.glimmerseed.editorcore.snapshot.InterpolationMode.LINEAR -> "━"
                                    com.example.glimmerseed.editorcore.snapshot.InterpolationMode.BEZIER -> "⌒"
                                    com.example.glimmerseed.editorcore.snapshot.InterpolationMode.EASE_IN_OUT -> "~"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = ACCENT.copy(alpha = 0.7f),
                                fontSize = (8 * uiScale).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EnhancedSnapshotCard(
    snapshot: com.example.glimmerseed.editorcore.snapshot.PoseSnapshot,
    index: Int,
    totalCount: Int,
    transition: com.example.glimmerseed.editorcore.snapshot.TransitionInterval?,
    onApply: () -> Unit,
    onDelete: () -> Unit,
    onTransitionModeChange: (com.example.glimmerseed.editorcore.snapshot.InterpolationMode) -> Unit,
    uiScale: Float
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onApply, onLongClick = { showMenu = true }),
            color = CARD_BG,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
        ) {
            Column(modifier = Modifier.padding((10 * uiScale).dp)) {
                // 头部行：序号 + 标签 + 骨骼数
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    // 序号圆点
                    Box(
                        modifier = Modifier
                            .size((20 * uiScale).dp)
                            .background(ACCENT, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = Color.White, fontSize = (11 * uiScale).sp, fontWeight = FontWeight.Bold)
                    }
                    // 标签
                    Text(
                        text = snapshot.label.ifEmpty { "快照 #${snapshot.id.take(6)}" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TEXT_PRIMARY,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    // 骨骼数标签
                    Surface(color = Color(0xFFFFECB3), shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)) {
                        Text("${snapshot.boneTransforms.size}骨", fontSize = (10 * uiScale).sp, color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = (6 * uiScale).dp, vertical = (2 * uiScale).dp))
                    }
                }

                // 第二行：时间戳
                Text(text = "时间: ${"%.2f".format(snapshot.timestamp)}s", style = MaterialTheme.typography.bodySmall, color = TEXT_MUTED)

                // 插值模式选择器（如果有下一个快照）
                if (transition != null) {
                    Spacer(modifier = Modifier.height((4 * uiScale).dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((6 * uiScale).dp)
                    ) {
                        Text("→", color = TEXT_MUTED, fontSize = (12 * uiScale).sp)
                        InterpolationModeChip(mode = transition.mode, isSelected = true, onClick = {}, uiScale = uiScale)
                        com.example.glimmerseed.editorcore.snapshot.InterpolationMode.entries
                            .filter { it != transition.mode }
                            .forEach { mode ->
                                InterpolationModeChip(mode = mode, isSelected = false, onClick = { onTransitionModeChange(mode) }, uiScale = uiScale)
                            }
                    }
                }
            }
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("删除", color = Color(0xFFE57373)) },
                onClick = { showMenu = false; onDelete() }
            )
        }
    }
}

@Composable
private fun InterpolationModeChip(
    mode: com.example.glimmerseed.editorcore.snapshot.InterpolationMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    uiScale: Float
) {
    val label = when (mode) {
        com.example.glimmerseed.editorcore.snapshot.InterpolationMode.STEP -> "步进"
        com.example.glimmerseed.editorcore.snapshot.InterpolationMode.LINEAR -> "线性"
        com.example.glimmerseed.editorcore.snapshot.InterpolationMode.BEZIER -> "贝塞尔"
        com.example.glimmerseed.editorcore.snapshot.InterpolationMode.EASE_IN_OUT -> "缓动"
    }
    val bgColor = if (isSelected) ACCENT.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isSelected) ACCENT else TEXT_MUTED

    Surface(
        onClick = onClick,
        color = bgColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(0.5.dp, ACCENT) else null
    ) {
        Text(label, modifier = Modifier.padding(horizontal = (6 * uiScale).dp, vertical = (3 * uiScale).dp), fontSize = (11 * uiScale).sp, color = textColor)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SnapshotCard(
    snapshot: com.example.glimmerseed.editorcore.snapshot.PoseSnapshot,
    onAction: (com.example.glimmerseed.editorcore.editor.EditorAction) -> Unit,
    uiScale: Float
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                ),
            color = CARD_BG,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
        ) {
            Row(
                modifier = Modifier.padding((10 * uiScale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = snapshot.label.ifEmpty { "快照 #${snapshot.id.take(6)}" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TEXT_PRIMARY,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height((2 * uiScale).dp))
                    Text(
                        text = "时间: ${"%.2f".format(snapshot.timestamp)}s | 骨骼数: ${snapshot.boneTransforms.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TEXT_MUTED
                    )
                }
            }
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("删除", color = Color(0xFFE57373)) },
                onClick = {
                    showMenu = false
                    onAction(com.example.glimmerseed.editorcore.editor.EditorAction.RemoveSnapshot(snapshot.id))
                }
            )
        }
    }
}

/** 精修面板 Tab — 原有的骨骼属性编辑 */
@Composable
private fun RefinePanelTab(
    editorState: com.example.glimmerseed.editorcore.editor.EditorState,
    onAction: (com.example.glimmerseed.editorcore.editor.EditorAction) -> Unit,
    uiScale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PANEL_BG)
            .padding((12 * uiScale).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
    ) {
        val skeleton = editorState.skeleton
        val selectedBoneId = editorState.selectedBoneId
        val currentTime = editorState.currentTime
        val selectedFrame = editorState.selectedFrame
        val currentAnimation = editorState.currentAnimation

        if (selectedBoneId != null && skeleton != null) {
            val selectedBone = skeleton.getBoneById(selectedBoneId)

            if (selectedBone != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CARD_BG,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "骨骼信息",
                            style = MaterialTheme.typography.titleMedium,
                            color = ACCENT,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )

                        Text(
                            text = "名称: ${selectedBone.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TEXT_SECONDARY
                        )

                        Text(
                            text = "ID: ${selectedBone.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TEXT_SECONDARY
                        )

                        Text(
                            text = "父骨骼: ${selectedBone.parentId ?: "无"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TEXT_SECONDARY
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CARD_BG,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, KEYFRAME_COLOR.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = KEYFRAME_COLOR,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "关键帧",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = KEYFRAME_COLOR,
                                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${formatTime(currentTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TEXT_PRIMARY,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selectedFrame != null) {
                                    Text(
                                        text = " (帧 $selectedFrame)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TEXT_SECONDARY
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KeyframeButton(
                                icon = AppIcons.Add,
                                label = "添加",
                                onClick = {
                                    onAction(
                                        com.example.glimmerseed.editorcore.editor.EditorAction.AddKeyframe(
                                            selectedBone.id,
                                            currentTime
                                        )
                                    )
                                },
                                enabled = true,
                                uiScale = uiScale,
                                modifier = Modifier.weight(1f)
                            )
                            KeyframeButton(
                                icon = AppIcons.Delete,
                                label = "删除",
                                onClick = {
                                    onAction(
                                        com.example.glimmerseed.editorcore.editor.EditorAction.DeleteKeyframe(
                                            selectedBone.id,
                                            currentTime
                                        )
                                    )
                                },
                                enabled = hasKeyframeAtCurrentTime(selectedBone.id, currentAnimation, currentTime),
                                uiScale = uiScale,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (currentAnimation != null) {
                            val boneKeyframes = currentAnimation.boneKeyframes[selectedBone.id]
                            if (!boneKeyframes.isNullOrEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "此骨骼关键帧:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TEXT_MUTED
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(20.dp)
                                            .background(Color(0xFFF5F5F5))
                                            .padding(2.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            boneKeyframes.forEach { keyframe ->
                                                val isCurrent = Math.abs(keyframe.time - currentTime) < 0.01f
                                                Box(
                                                    modifier = Modifier
                                                        .width(16.dp)
                                                        .fillMaxHeight()
                                                        .background(if (isCurrent) KEYFRAME_COLOR else Color(0xFFBDBDBD))
                                                        .clickable {
                                                            onAction(
                                                                com.example.glimmerseed.editorcore.editor.EditorAction.SetCurrentTime(
                                                                    keyframe.time
                                                                )
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isCurrent) {
                                                        Icon(
                                                            imageVector = AppIcons.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "此骨骼暂无关键帧",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TEXT_MUTED
                                )
                            }
                        } else {
                            Text(
                                text = "未选择动画",
                                style = MaterialTheme.typography.bodySmall,
                                color = TEXT_MUTED
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CARD_BG,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "位置",
                            style = MaterialTheme.typography.titleMedium,
                            color = ACCENT,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )

                        val position = Vector3f()
                        selectedBone.localTransform.getTranslation(position)

                        NumberSlider(
                            label = "X",
                            value = position.x,
                            onValueChange = { newValue ->
                                val delta = Vector3f(newValue - position.x, 0f, 0f)
                                onAction(
                                    com.example.glimmerseed.editorcore.editor.EditorAction.MoveBone(
                                        selectedBone.id,
                                        delta
                                    )
                                )
                            },
                            range = -200f..200f
                        )

                        NumberSlider(
                            label = "Y",
                            value = position.y,
                            onValueChange = { newValue ->
                                val delta = Vector3f(0f, newValue - position.y, 0f)
                                onAction(
                                    com.example.glimmerseed.editorcore.editor.EditorAction.MoveBone(
                                        selectedBone.id,
                                        delta
                                    )
                                )
                            },
                            range = -200f..200f
                        )

                        NumberSlider(
                            label = "Z",
                            value = position.z,
                            onValueChange = { newValue ->
                                val delta = Vector3f(0f, 0f, newValue - position.z)
                                onAction(
                                    com.example.glimmerseed.editorcore.editor.EditorAction.MoveBone(
                                        selectedBone.id,
                                        delta
                                    )
                                )
                            },
                            range = -200f..200f
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CARD_BG,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "旋转",
                            style = MaterialTheme.typography.titleMedium,
                            color = ACCENT,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )

                        val eulerAngles = selectedBone.localTransform.getEulerAnglesXYZ(Vector3f())
                        val rotationDegrees by remember(selectedBone.id) {
                            mutableFloatStateOf(Math.toDegrees(eulerAngles.x.toDouble()).toFloat())
                        }

                        NumberSlider(
                            label = "角度",
                            value = rotationDegrees,
                            onValueChange = { newValue ->
                                val deltaRadians = Math.toRadians((newValue - rotationDegrees).toDouble()).toFloat()
                                onAction(
                                    com.example.glimmerseed.editorcore.editor.EditorAction.RotateBone(
                                        selectedBone.id,
                                        org.joml.Quaternionf().rotateX(deltaRadians)
                                    )
                                )
                            },
                            range = -180f..180f
                        )
                    }
                }
            }
        } else {
            Text(
                text = "属性面板",
                style = MaterialTheme.typography.titleLarge,
                color = TEXT_PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CARD_BG,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFC2B2A1))
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "请选择一个骨骼来编辑其属性",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TEXT_MUTED
                    )
                }
            }
        }
    }
}

@Composable
fun KeyframeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    uiScale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height((36 * uiScale).dp)
            .background(
                color = if (enabled) KEYFRAME_COLOR else Color(0xFFE0E0E0),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else Color(0xFF9E9E9E),
                modifier = Modifier.size((16 * uiScale).dp)
            )
            Spacer(modifier = Modifier.width((4 * uiScale).dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) Color.White else Color(0xFF9E9E9E),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun NumberSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TEXT_SECONDARY,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
            Text(
                text = "%.1f".format(value),
                style = MaterialTheme.typography.bodySmall,
                color = TEXT_PRIMARY
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = SLIDER_ACTIVE,
                activeTrackColor = SLIDER_ACTIVE,
                inactiveTrackColor = SLIDER_INACTIVE
            )
        )
    }
}

private fun formatTime(time: Float): String {
    val totalSec = time.toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    val f = ((time - totalSec) * FRAMES_PER_SEC).toInt()
    return "$m:${s.toString().padStart(2, '0')}:${f.toString().padStart(2, '0')}"
}

private fun hasKeyframeAtCurrentTime(
    boneId: Int,
    animation: com.example.glimmerseed.editorcore.animation.AnimationClip?,
    time: Float
): Boolean {
    if (animation == null) return false
    val keyframes = animation.boneKeyframes[boneId] ?: return false
    return keyframes.any { Math.abs(it.time - time) < 0.01f }
}