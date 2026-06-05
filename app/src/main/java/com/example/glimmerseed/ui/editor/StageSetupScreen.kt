package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.editorcore.coord.NormalizedRect
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.editorcore.editor.EditorState
import com.example.glimmerseed.editorcore.editor.EditorViewModel
import com.example.glimmerseed.editorcore.editor.PanelEditingMode
import com.example.glimmerseed.editorcore.panel.*
import com.example.glimmerseed.editorcore.stage.PanelSlot
import com.example.glimmerseed.ui.common.AppIcons

private val BG_COLOR = Color(0xE6EDE6DD)
private val CARD_BG = Color(0xCCF5F0E8)
private val BORDER_COLOR = Color(0xFFC2B2A1)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF5D4037)
private val TEXT_MUTED = Color(0xFF795548)
private val ACCENT_COLOR = Color(0xFF8D6E63)
private val SELECTED = Color(0xFFD6CBBE)
private val ACTIVE_GREEN = Color(0xFF4CAF50)
private val INACTIVE_RED = Color(0xFFE53935)

@Composable
fun StageSetupScreen(
    editorViewModel: EditorViewModel,
    onClose: () -> Unit,
    onEditPanel: (String) -> Unit,
    onCreatePanel: () -> Unit
) {
    val editorState by editorViewModel.state.collectAsState()
    val panelEditing = editorState.panelEditing

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BG_COLOR
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StageSetupHeader(
                panelCount = panelEditing.panels.size,
                onClose = onClose,
                onCreatePanel = onCreatePanel
            )

            if (panelEditing.panels.isEmpty()) {
                StageEmptyState(onCreatePanel = onCreatePanel)
            } else {
                PanelListSection(
                    panels = panelEditing.panels,
                    slots = panelEditing.slots,
                    selectedPanelId = panelEditing.selectedPanelId,
                    editingMode = panelEditing.editingMode,
                    onSelectPanel = { panelId ->
                        editorViewModel.dispatch(EditorAction.SelectPanel(panelId))
                    },
                    onToggleActive = { panelId ->
                        val existing = panelEditing.slots[panelId] ?: return@PanelListSection
                        editorViewModel.dispatch(
                            EditorAction.UpdatePanelSlot(
                                panelId = panelId,
                                slot = existing.copy(active = !existing.active)
                            )
                        )
                    },
                    onMoveUp = { index ->
                        if (index <= 0) return@PanelListSection
                        val panels = panelEditing.panels.toMutableList()
                        val temp = panels[index]
                        panels[index] = panels[index - 1]
                        panels[index - 1] = temp
                        val zPanels = panels.mapIndexed { i, p -> p.id to i }
                        editorViewModel.dispatch(EditorAction.RemovePanel(temp.id))
                        zPanels.forEach { (id, z) ->
                            val slot = panelEditing.slots[id]
                            if (slot != null) {
                                editorViewModel.dispatch(
                                    EditorAction.UpdatePanelSlot(
                                        panelId = id,
                                        slot = slot.copy(zOrder = z)
                                    )
                                )
                            }
                        }
                        editorViewModel.dispatch(EditorAction.AddPanel(temp, panelEditing.slots[temp.id]!!))
                    },
                    onMoveDown = { index ->
                        if (index >= panelEditing.panels.size - 1) return@PanelListSection
                        val panels = panelEditing.panels.toMutableList()
                        val temp = panels[index]
                        panels[index] = panels[index + 1]
                        panels[index + 1] = temp
                        val zPanels = panels.mapIndexed { i, p -> p.id to i }
                        editorViewModel.dispatch(EditorAction.RemovePanel(temp.id))
                        zPanels.forEach { (id, z) ->
                            val slot = panelEditing.slots[id]
                            if (slot != null) {
                                editorViewModel.dispatch(
                                    EditorAction.UpdatePanelSlot(
                                        panelId = id,
                                        slot = slot.copy(zOrder = z)
                                    )
                                )
                            }
                        }
                        editorViewModel.dispatch(EditorAction.AddPanel(temp, panelEditing.slots[temp.id]!!))
                    },
                    onEditPanel = onEditPanel,
                    onDeletePanel = { panelId ->
                        editorViewModel.dispatch(EditorAction.RemovePanel(panelId))
                    },
                    onSetEditingMode = { mode ->
                        val currentState = editorState.copy(
                            panelEditing = panelEditing.copy(editingMode = mode)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun StageSetupHeader(
    panelCount: Int,
    onClose: () -> Unit,
    onCreatePanel: () -> Unit
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
                imageVector = AppIcons.Star,
                contentDescription = null,
                tint = ACCENT_COLOR,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "舞台设置",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TEXT_PRIMARY
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = ACCENT_COLOR,
                shape = CircleShape
            ) {
                Text(
                    text = "$panelCount",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCreatePanel,
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    AppIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("新建面板", fontSize = 13.sp)
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
}

@Composable
private fun StageEmptyState(onCreatePanel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = AppIcons.Star,
                contentDescription = null,
                tint = TEXT_MUTED.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无面板",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TEXT_MUTED
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击下方按钮创建第一个面板",
                fontSize = 13.sp,
                color = TEXT_MUTED.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreatePanel,
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT_COLOR),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(AppIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建面板", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun PanelListSection(
    panels: List<PanelData>,
    slots: Map<String, PanelSlot>,
    selectedPanelId: String?,
    editingMode: PanelEditingMode,
    onSelectPanel: (String) -> Unit,
    onToggleActive: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onEditPanel: (String) -> Unit,
    onDeletePanel: (String) -> Unit,
    onSetEditingMode: (PanelEditingMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CARD_BG, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Z轴", fontSize = 11.sp, color = TEXT_MUTED, modifier = Modifier.width(32.dp))
            Text("面板名称", fontSize = 11.sp, color = TEXT_MUTED, modifier = Modifier.weight(1f))
            Text("激活", fontSize = 11.sp, color = TEXT_MUTED, modifier = Modifier.width(40.dp))
            Text("操作", fontSize = 11.sp, color = TEXT_MUTED, modifier = Modifier.width(140.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(panels.sortedByDescending { slots[it.id]?.zOrder ?: 0 }) { index, panel ->
                val slot = slots[panel.id]
                val isSelected = panel.id == selectedPanelId
                val isActive = slot?.active ?: true
                val zOrder = slot?.zOrder ?: index

                PanelListItem(
                    index = index,
                    totalCount = panels.size,
                    panel = panel,
                    zOrder = zOrder,
                    isActive = isActive,
                    isSelected = isSelected,
                    onSelect = { onSelectPanel(panel.id) },
                    onToggleActive = { onToggleActive(panel.id) },
                    onMoveUp = { onMoveUp(index) },
                    onMoveDown = { onMoveDown(index) },
                    onEdit = { onEditPanel(panel.id) },
                    onDelete = { onDeletePanel(panel.id) }
                )
            }
        }
    }
}

@Composable
private fun PanelListItem(
    index: Int,
    totalCount: Int,
    panel: PanelData,
    zOrder: Int,
    isActive: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleActive: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        color = if (isSelected) SELECTED else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isSelected) ACCENT_COLOR else BORDER_COLOR.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${zOrder}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TEXT_PRIMARY,
                modifier = Modifier.width(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = panel.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_PRIMARY,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = visualTypeLabel(panel.visual.type),
                    fontSize = 10.sp,
                    color = TEXT_MUTED
                )
            }

            Switch(
                checked = isActive,
                onCheckedChange = { onToggleActive() },
                modifier = Modifier.width(40.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ACTIVE_GREEN,
                    uncheckedThumbColor = INACTIVE_RED,
                    checkedTrackColor = ACTIVE_GREEN.copy(alpha = 0.3f),
                    uncheckedTrackColor = INACTIVE_RED.copy(alpha = 0.3f)
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.width(140.dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        AppIcons.KeyboardArrowUp,
                        contentDescription = "上移",
                        tint = if (index > 0) TEXT_SECONDARY else TEXT_MUTED.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        AppIcons.KeyboardArrowDown,
                        contentDescription = "下移",
                        tint = if (index < totalCount - 1) TEXT_SECONDARY else TEXT_MUTED.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        AppIcons.Edit,
                        contentDescription = "编辑",
                        tint = ACCENT_COLOR,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = "删除",
                        tint = INACTIVE_RED,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun visualTypeLabel(type: VisualType): String = when (type) {
    VisualType.FRAME_ANIMATION -> "帧动画"
    VisualType.LAYER_RENDERING -> "图层渲染"
    VisualType.SKELETAL_ANIMATION -> "骨骼动画"
}