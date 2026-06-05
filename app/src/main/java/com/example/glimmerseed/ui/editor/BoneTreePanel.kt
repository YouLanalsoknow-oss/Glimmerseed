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
import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.ui.common.AppIcons

private val TEXT_COLOR = Color(0xFF3E2723)
private val TEXT_MUTED = Color(0xFF795548)
private val BONE_TEXT = Color(0xFF1565C0)
private val BONE_SELECTED = Color(0xFFE65100)
private val PANEL_BG = Color(0xFFF5F0E8)
private val HEADER_BG = Color(0xFFEDE6DD)

/**
 * 骨骼树面板 - 字符超链接文本形式
 * 
 * 特点：
 * - 纯文本链接形式，无卡片背景
 * - 文件夹使用展开/折叠符号（▼ / ▶）
 * - 骨骼使用纯文本名称
 * - 选中骨骼使用高亮文本
 */
@Composable
fun BoneTreePanel(
    editorState: com.example.glimmerseed.editorcore.editor.EditorState,
    onAction: (com.example.glimmerseed.editorcore.editor.EditorAction) -> Unit,
    uiScale: Float = 1f
) {
    var expandedBones by remember { mutableStateOf(setOf<Int?>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PANEL_BG)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HEADER_BG)
                .padding(
                    horizontal = (12 * uiScale).dp,
                    vertical = (6 * uiScale).dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            Icon(
                imageVector = AppIcons.List,
                contentDescription = null,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size((14 * uiScale).dp)
            )
            Text(
                text = "骨骼树",
                fontSize = (12 * uiScale).sp,
                fontWeight = FontWeight.Medium,
                color = TEXT_COLOR
            )
            val selectedBone = editorState.selectedBoneId
            if (selectedBone != null) {
                Text(
                    text = "·",
                    fontSize = (12 * uiScale).sp,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    text = "#$selectedBone",
                    fontSize = (12 * uiScale).sp,
                    fontWeight = FontWeight.Medium,
                    color = BONE_SELECTED
                )
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth().height((1 * uiScale).dp),
            color = Color(0xFFD7CCC8)
        )

        // 骨骼树内容
        val skeleton = editorState.skeleton
        if (skeleton != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding((8 * uiScale).dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy((2 * uiScale).dp)
            ) {
                BoneTreeRecursive(
                    bones = skeleton.bones,
                    selectedBoneId = editorState.selectedBoneId,
                    parentBoneId = null,
                    expandedBones = expandedBones,
                    onToggleExpand = { boneId ->
                        expandedBones = if (expandedBones.contains(boneId)) {
                            expandedBones - boneId
                        } else {
                            expandedBones + boneId
                        }
                    },
                    onSelectBone = { boneId ->
                        onAction(com.example.glimmerseed.editorcore.editor.EditorAction.SelectBone(boneId))
                    },
                    uiScale = uiScale
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无骨骼数据",
                    fontSize = (11 * uiScale).sp,
                    color = TEXT_MUTED
                )
            }
        }
    }
}

@Composable
fun BoneTreeRecursive(
    bones: List<Bone>,
    selectedBoneId: Int?,
    parentBoneId: Int?,
    expandedBones: Set<Int?>,
    onToggleExpand: (Int?) -> Unit,
    onSelectBone: (Int) -> Unit,
    uiScale: Float
) {
    val childBones = bones.filter { it.parentId == parentBoneId }

    if (childBones.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            for (bone in childBones) {
                val hasChildren = bones.any { it.parentId == bone.id }
                val isExpanded = expandedBones.contains(bone.id)
                val isSelected = bone.id == selectedBoneId
                val depth = getBoneDepth(bones, bone.id)

                // 骨骼项 - 字符超链接文本
                val annotatedText = buildAnnotatedString {
                    // 缩进
                    if (depth > 0) {
                        append("  ".repeat(depth))
                    }

                    // 展开/折叠符号
                    if (hasChildren) {
                        withStyle(
                            SpanStyle(
                                color = TEXT_MUTED,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(if (isExpanded) "▼ " else "▶ ")
                        }
                    } else {
                        append("  ")
                    }

                    // 骨骼名称
                    withStyle(
                        SpanStyle(
                            color = if (isSelected) BONE_SELECTED else BONE_TEXT,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    ) {
                        append(bone.name)
                    }

                    // ID
                    withStyle(
                        SpanStyle(
                            color = TEXT_MUTED.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(" #${bone.id}")
                    }
                }

                Text(
                    text = annotatedText,
                    fontSize = (11 * uiScale).sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (hasChildren) {
                                onToggleExpand(bone.id)
                            }
                            onSelectBone(bone.id)
                        }
                        .padding(horizontal = (4 * uiScale).dp, vertical = (2 * uiScale).dp)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Color(0xFFFFECB3).copy(alpha = 0.5f),
                                    RoundedCornerShape((2 * uiScale).dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = (4 * uiScale).dp)
                )

                // 递归渲染子骨骼
                if (hasChildren && isExpanded) {
                    BoneTreeRecursive(
                        bones = bones,
                        selectedBoneId = selectedBoneId,
                        parentBoneId = bone.id,
                        expandedBones = expandedBones,
                        onToggleExpand = onToggleExpand,
                        onSelectBone = onSelectBone,
                        uiScale = uiScale
                    )
                }
            }
        }
    }
}

private fun getBoneDepth(
    bones: List<Bone>,
    boneId: Int
): Int {
    var depth = 0
    var currentBoneId: Int? = boneId

    while (currentBoneId != null) {
        val bone = bones.find { it.id == currentBoneId } ?: break
        currentBoneId = bone.parentId
        depth++
    }

    return depth - 1
}
