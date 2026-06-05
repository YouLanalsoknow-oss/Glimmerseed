package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.editorcore.editor.EditorViewModel
import com.example.glimmerseed.editorcore.panel.*
import com.example.glimmerseed.ui.common.AppIcons

private val BG_COLOR = Color(0xE6EDE6DD)
private val CARD_BG = Color(0xCCF5F0E8)
private val BORDER_COLOR = Color(0xFFC2B2A1)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF5D4037)
private val TEXT_MUTED = Color(0xFF795548)
private val ACCENT_COLOR = Color(0xFF8D6E63)
private val SELECTED = Color(0xFFD6CBBE)

@Composable
fun PanelEditorScreen(
    panelData: PanelData,
    editorViewModel: EditorViewModel,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("视觉层", "交互层", "行为层")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BG_COLOR
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PanelEditorHeader(
                panelName = panelData.name,
                onClose = onClose
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CARD_BG,
                contentColor = TEXT_PRIMARY,
                divider = { Divider(color = BORDER_COLOR, thickness = 0.5.dp) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) TEXT_PRIMARY else TEXT_SECONDARY
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> VisualLayerEditor(panelData.visual, editorViewModel, panelData.id)
                1 -> InteractionLayerEditor(panelData.interaction, editorViewModel, panelData.id)
                2 -> BehaviorLayerEditor(panelData.behavior, editorViewModel, panelData.id)
            }
        }
    }
}

@Composable
private fun PanelEditorHeader(
    panelName: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CARD_BG)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = AppIcons.Edit,
                contentDescription = null,
                tint = ACCENT_COLOR,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = panelName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TEXT_PRIMARY
            )
        }

        TextButton(onClick = onClose) {
            Icon(
                AppIcons.Close,
                contentDescription = null,
                tint = TEXT_SECONDARY,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ===================== 视觉层编辑器 =====================

@Composable
private fun VisualLayerEditor(
    visualData: VisualLayerData,
    editorViewModel: EditorViewModel,
    panelId: String
) {
    var opacity by remember { mutableFloatStateOf(visualData.opacity) }
    var visible by remember { mutableStateOf(visualData.visible) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "基础属性") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("可见", fontSize = 13.sp, color = TEXT_PRIMARY)
                Switch(
                    checked = visible,
                    onCheckedChange = { visible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ACCENT_COLOR,
                        checkedTrackColor = ACCENT_COLOR.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("不透明度", fontSize = 13.sp, color = TEXT_PRIMARY)
                    Text("${(opacity * 100).toInt()}%", fontSize = 12.sp, color = TEXT_MUTED)
                }
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = ACCENT_COLOR,
                        activeTrackColor = ACCENT_COLOR
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = BORDER_COLOR.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        AppIcons.Info,
                        contentDescription = null,
                        tint = TEXT_MUTED,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "类型: ${visualTypeLabel(visualData.type)}",
                        fontSize = 12.sp,
                        color = TEXT_MUTED
                    )
                }
            }
        }

        SectionCard(title = "图层列表 (${visualData.layers.size})") {
            if (visualData.layers.isEmpty()) {
                Text(
                    "暂无图层",
                    fontSize = 12.sp,
                    color = TEXT_MUTED.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                visualData.layers.forEachIndexed { index, layer ->
                    LayerItem(index = index, layer = layer)
                }
            }
        }
    }
}

@Composable
private fun LayerItem(index: Int, layer: LayerData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = SELECTED.copy(alpha = 0.3f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BORDER_COLOR.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "图层 ${index + 1}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = TEXT_PRIMARY
                )
                Text(
                    blendModeLabel(layer.blending),
                    fontSize = 10.sp,
                    color = TEXT_MUTED
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip("位置", "(${(layer.posX * 100).toInt()}, ${(layer.posY * 100).toInt()})")
                InfoChip("旋转", "${layer.rotation}°")
                InfoChip("缩放", "${(layer.scaleX * 100).toInt()}%")
                InfoChip("不透明度", "${(layer.opacity * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = TEXT_MUTED)
        Text(value, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TEXT_PRIMARY)
    }
}

// ===================== 交互层编辑器 =====================

@Composable
private fun InteractionLayerEditor(
    interactionData: InteractionLayerData,
    editorViewModel: EditorViewModel,
    panelId: String
) {
    var touchMode by remember { mutableStateOf(interactionData.touchMode) }
    var showModeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "触摸模式") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SELECTED.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BORDER_COLOR.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("当前模式", fontSize = 11.sp, color = TEXT_MUTED)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showModeMenu = true },
                            color = CARD_BG,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = touchModeLabel(touchMode),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TEXT_PRIMARY
                                )
                                Icon(
                                    AppIcons.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TEXT_MUTED,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showModeMenu,
                            onDismissRequest = { showModeMenu = false }
                        ) {
                            TouchMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                touchModeLabel(mode),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TEXT_PRIMARY
                                            )
                                            Text(
                                                touchModeDescription(mode),
                                                fontSize = 10.sp,
                                                color = TEXT_MUTED
                                            )
                                        }
                                    },
                                    onClick = {
                                        touchMode = mode
                                        showModeMenu = false
                                        editorViewModel.dispatch(
                                            EditorAction.UpdatePanelTouchMode(panelId, mode)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        touchModeDescription(touchMode),
                        fontSize = 11.sp,
                        color = TEXT_MUTED,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (touchMode == TouchMode.REGION_BASED) {
            SectionCard(title = "触摸区域 (${interactionData.hitRegions.size})") {
                if (interactionData.hitRegions.isEmpty()) {
                    Text(
                        "未配置触摸区域\nREGION_BASED模式下需要定义至少一个区域",
                        fontSize = 12.sp,
                        color = TEXT_MUTED.copy(alpha = 0.6f),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    interactionData.hitRegions.forEachIndexed { index, region ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            color = SELECTED.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp, BORDER_COLOR.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "区域 ${index + 1}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = TEXT_PRIMARY
                                    )
                                    Text(
                                        touchModeLabel(region.mode),
                                        fontSize = 10.sp,
                                        color = ACCENT_COLOR
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val rect = region.normalizedRect
                                Text(
                                    "位置: (${(rect.x * 100).toInt()}%, ${(rect.y * 100).toInt()}%)",
                                    fontSize = 10.sp,
                                    color = TEXT_MUTED
                                )
                                Text(
                                    "尺寸: ${(rect.width * 100).toInt()}% x ${(rect.height * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    color = TEXT_MUTED
                                )
                                if (region.gestureTypes.isNotEmpty()) {
                                    Text(
                                        "手势: ${region.gestureTypes.joinToString(", ") { gestureLabel(it) }}",
                                        fontSize = 10.sp,
                                        color = TEXT_MUTED
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================== 行为层编辑器 =====================

@Composable
private fun BehaviorLayerEditor(
    behaviorData: BehaviorLayerData,
    editorViewModel: EditorViewModel,
    panelId: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "手势事件处理器 (${behaviorData.gestureHandlers.size})") {
            if (behaviorData.gestureHandlers.isEmpty()) {
                EmptyHandlerHint("未配置手势事件处理器")
            } else {
                behaviorData.gestureHandlers.forEachIndexed { index, handler ->
                    HandlerItem(
                        label = gestureLabel(handler.gestureType),
                        detail = behaviorActionLabel(handler.action),
                        index = index
                    )
                }
            }
        }

        SectionCard(title = "系统事件处理器 (${behaviorData.systemEventHandlers.size})") {
            if (behaviorData.systemEventHandlers.isEmpty()) {
                EmptyHandlerHint("未配置系统事件处理器")
            } else {
                behaviorData.systemEventHandlers.forEachIndexed { index, handler ->
                    HandlerItem(
                        label = systemEventLabel(handler.eventType),
                        detail = behaviorActionLabel(handler.action),
                        index = index
                    )
                }
            }
        }

        SectionCard(title = "面板间事件处理器 (${behaviorData.panelEventHandlers.size})") {
            if (behaviorData.panelEventHandlers.isEmpty()) {
                EmptyHandlerHint("未配置面板间事件处理器")
            } else {
                behaviorData.panelEventHandlers.forEachIndexed { index, handler ->
                    HandlerItem(
                        label = handler.eventName,
                        detail = behaviorActionLabel(handler.action),
                        index = index
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHandlerHint(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        color = TEXT_MUTED.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun HandlerItem(label: String, detail: String, index: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        color = SELECTED.copy(alpha = 0.3f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BORDER_COLOR.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TEXT_PRIMARY)
                Text(detail, fontSize = 10.sp, color = TEXT_MUTED, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(
                AppIcons.ArrowForward,
                contentDescription = null,
                tint = TEXT_MUTED,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ===================== 通用组件 =====================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = CARD_BG,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BORDER_COLOR.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TEXT_PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// ===================== 标签辅助函数 =====================

private fun visualTypeLabel(type: VisualType): String = when (type) {
    VisualType.FRAME_ANIMATION -> "帧动画"
    VisualType.LAYER_RENDERING -> "图层渲染"
    VisualType.SKELETAL_ANIMATION -> "骨骼动画"
}

private fun touchModeLabel(mode: TouchMode): String = when (mode) {
    TouchMode.PASSTHROUGH -> "全局穿透"
    TouchMode.BLOCKING -> "全局拦截"
    TouchMode.REGION_BASED -> "区域配置"
}

private fun touchModeDescription(mode: TouchMode): String = when (mode) {
    TouchMode.PASSTHROUGH -> "触摸事件穿透面板，不拦截任何交互"
    TouchMode.BLOCKING -> "触摸事件被面板完全拦截，不穿透"
    TouchMode.REGION_BASED -> "仅配置的触摸区域拦截事件，其余区域穿透"
}

private fun blendModeLabel(mode: BlendingMode): String = when (mode) {
    BlendingMode.NORMAL -> "普通"
    BlendingMode.ADDITIVE -> "叠加"
    BlendingMode.MULTIPLY -> "正片叠底"
}

private fun gestureLabel(type: GestureType): String = when (type) {
    GestureType.TAP -> "点击"
    GestureType.LONG_PRESS -> "长按"
    GestureType.SWIPE_LEFT -> "左滑"
    GestureType.SWIPE_RIGHT -> "右滑"
    GestureType.SWIPE_UP -> "上滑"
    GestureType.SWIPE_DOWN -> "下滑"
}

private fun systemEventLabel(type: SystemEventType): String = when (type) {
    SystemEventType.SCREEN_OFF -> "息屏"
    SystemEventType.SCREEN_ON -> "亮屏"
    SystemEventType.VOLUME_UP -> "音量+"
    SystemEventType.VOLUME_DOWN -> "音量-"
    SystemEventType.NOTIFICATION -> "通知"
}

private fun behaviorActionLabel(action: BehaviorAction): String = when (action) {
    is BehaviorAction.ToggleVisibility -> "切换可见性"
    is BehaviorAction.PlayAnimation -> "播放动画"
    is BehaviorAction.SendEvent -> "发送事件: ${action.eventName}"
    is BehaviorAction.SetTouchMode -> "设置触摸模式"
    is BehaviorAction.NoOp -> "无操作"
}