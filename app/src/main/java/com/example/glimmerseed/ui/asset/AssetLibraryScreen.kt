package com.example.glimmerseed.ui.asset

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import com.example.glimmerseed.GlimmerseedApplication
import com.example.glimmerseed.R
import com.example.glimmerseed.editorcore.asset.AssetManager
import com.example.glimmerseed.editorcore.asset.AssetRef
import com.example.glimmerseed.editorcore.asset.AssetSource
import com.example.glimmerseed.editorcore.asset.AssetType
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.GlimmerseedTextField
import kotlinx.coroutines.launch

// 与 EditorActivity 一致的颜色常量
private val PANEL_BG = Color(0xFFF5F0E8)
private val BORDER = Color(0xFFC2B2A1)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF5D4037)
private val TEXT_MUTED = Color(0xFF795548)
private val ACCENT = Color(0xFF8D6E63)

@Composable
fun AssetLibraryScreen(
    onSelectAsset: (AssetRef) -> Unit,
    onDismiss: () -> Unit,
    selectedType: AssetType? = null,
    visible: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val application = context.applicationContext as GlimmerseedApplication
    val assets by application.assetManager.observeAllAssets().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(selectedType) }
    var selectedAsset by remember { mutableStateOf<AssetManager.AssetDisplayItem?>(null) }

    val filteredAssets = assets.filter { asset ->
        val matchesSearch = asset.displayName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == null || asset.type == selectedFilter
        matchesSearch && matchesFilter
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                application.assetManager.importAsset(uri.toString())
            }
        }
    }

    // 侧栏滑入动画
    val offsetAnim by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "sidebar_slide"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 遮罩层（点击关闭）
        if (visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = offsetAnim }
                    .clickable(onClick = onDismiss)
                    .background(Color.Black.copy(alpha = 0.3f * offsetAnim))
            )
        }

        // 右侧半屏侧栏
        Surface(
            color = PANEL_BG,
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            shadowElevation = 12.dp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(IntrinsicSize.Max)
                .graphicsLayer {
                    translationX = size.width * (1f - offsetAnim)
                }
                .padding(start = 4.dp)
        ) {
            Column(modifier = Modifier.width(320.dp).fillMaxHeight()) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ACCENT.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "素材库",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TEXT_PRIMARY
                    )

                    Spacer(Modifier.weight(1f))

                    // 导入按钮
                    Surface(
                        onClick = { importLauncher.launch("*/*") },
                        color = ACCENT.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(AppIcons.Add, contentDescription = "导入", tint = ACCENT, modifier = Modifier.size(14.dp))
                            Text("导入", fontSize = 11.sp, color = ACCENT)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // 关闭按钮
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(AppIcons.Close, contentDescription = "关闭", tint = TEXT_MUTED, modifier = Modifier.size(18.dp))
                    }
                }

                Divider(color = BORDER)

                // 搜索框
                Column(modifier = Modifier.padding(8.dp)) {
                    GlimmerseedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "搜索素材",
                        leadingIcon = { Icon(AppIcons.Search, contentDescription = "搜索", tint = TEXT_MUTED, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {})
                    )
                }

                // 筛选标签
                FilterChips(selectedFilter) { selectedFilter = it }

                Divider(color = BORDER)

                // 素材网格
                if (filteredAssets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(AppIcons.List, contentDescription = "空状态", tint = TEXT_MUTED, modifier = Modifier.size(48.dp))
                            Text("暂无素材", fontSize = 13.sp, color = TEXT_MUTED)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAssets) { asset ->
                            AssetCard(
                                asset = asset,
                                onClick = { selectedAsset = asset },
                                onLongClick = { selectedAsset = asset }
                            )
                        }
                    }
                }
            }
        }
    }

    // 预览弹窗（保留原有功能）
    selectedAsset?.let { asset ->
        AssetPreviewDialog(
            asset = asset,
            onDismiss = { selectedAsset = null },
            onSelect = {
                onSelectAsset(AssetRef(
                    assetId = asset.assetId,
                    type = asset.type,
                    source = asset.source,
                    localPath = ""
                ))
                selectedAsset = null
            },
            onDelete = {
                scope.launch {
                    application.assetManager.deleteAsset(asset.assetId)
                }
                selectedAsset = null
            }
        )
    }
}

@Composable
private fun FilterChips(
    selectedType: AssetType?,
    onTypeSelected: (AssetType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChipItem(label = "全部", selected = selectedType == null, onClick = { onTypeSelected(null) })
        AssetType.values().forEach { type ->
            FilterChipItem(label = type.displayName, selected = selectedType == type, onClick = { onTypeSelected(type) })
        }
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ACCENT else Color.Transparent,
        border = if (!selected) androidx.compose.foundation.BorderStroke(0.5.dp, BORDER) else null
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color.White else TEXT_SECONDARY,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AssetCard(
    asset: AssetManager.AssetDisplayItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongClick() })
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (asset.thumbnailPath != null) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(asset.thumbnailPath),
                    contentDescription = asset.displayName,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    color = Color(0xFFF0EBE3)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(getDefaultIconForType(asset.type)),
                            contentDescription = asset.displayName,
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (asset.source == AssetSource.OFFICIAL) {
                Surface(
                    color = ACCENT,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Text("官方", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(2.dp))
                }
            }
        }
        Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = asset.displayName,
                fontSize = 11.sp,
                color = TEXT_PRIMARY,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = asset.type.displayName, fontSize = 9.sp, color = TEXT_MUTED)
        }
    }
}

@Composable
fun AssetPreviewDialog(
    asset: AssetManager.AssetDisplayItem,
    onDismiss: () -> Unit,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PANEL_BG),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, BORDER)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(asset.displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(AppIcons.Close, contentDescription = "关闭", tint = TEXT_MUTED, modifier = Modifier.size(16.dp))
                    }
                }

                if (asset.thumbnailPath != null) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(asset.thumbnailPath),
                        contentDescription = asset.displayName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("类型", asset.type.displayName)
                    InfoRow("大小", formatFileSize(asset.sizeBytes))
                    InfoRow("来源", if (asset.source == AssetSource.OFFICIAL) "官方" else "用户")
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (asset.source == AssetSource.USER) {
                        Surface(
                            onClick = onDelete,
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(AppIcons.Delete, contentDescription = "删除", tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                Text("删除", fontSize = 12.sp, color = Color(0xFFC62828))
                            }
                        }
                    }

                    Surface(
                        onClick = onSelect,
                        color = ACCENT,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(AppIcons.Check, contentDescription = "选择", tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("使用此素材", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = TEXT_MUTED)
        Text(value, fontSize = 12.sp, color = TEXT_PRIMARY)
    }
}

fun getDefaultIconForType(type: AssetType): Int {
    return when (type) {
        AssetType.IMAGE -> R.drawable.ic_image
        AssetType.SKELETON_PROJECT -> R.drawable.ic_skeleton
        AssetType.ANIMATION_CLIP -> R.drawable.ic_animation
        AssetType.AUDIO -> R.drawable.ic_image
        AssetType.FONT -> R.drawable.ic_image
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
    }
}

val AssetType.displayName: String
    get() = when (this) {
        AssetType.IMAGE -> "图片"
        AssetType.SKELETON_PROJECT -> "骨骼"
        AssetType.ANIMATION_CLIP -> "动画"
        AssetType.AUDIO -> "音频"
        AssetType.FONT -> "字体"
    }
