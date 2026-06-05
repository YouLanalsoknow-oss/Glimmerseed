package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.glimmerseed.R
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.ui.common.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinEditorPanel(
    viewModel: com.example.glimmerseed.editorcore.editor.EditorViewModel,
    onAction: (EditorAction) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val skeleton = state.skeleton
    val mesh = state.mesh

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = {
                    skeleton?.let { skel ->
                        mesh?.let { msh ->
                            val vertices = msh.vertices.map { org.joml.Vector3f(it.position.x, it.position.y, it.position.z) }
                            val weights = com.example.glimmerseed.editorcore.skin.SkinWeightCalculator.calculateDistanceBasedWeights(
                                vertices = vertices,
                                skeleton = skel,
                                maxInfluenceBones = 4,
                                falloffDistance = 100f
                            )
                            onAction(EditorAction.SetSkinWeights(weights))
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Edit, contentDescription = "自动权重")
                Spacer(modifier = Modifier.width(4.dp))
                Text("自动权重")
            }

            ElevatedButton(
                onClick = {
                    onAction(EditorAction.ResetSkinWeights)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Delete, contentDescription = "重置权重")
                Spacer(modifier = Modifier.width(4.dp))
                Text("重置权重")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = {
                    onAction(EditorAction.ToggleWeightPaintMode)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (state.isWeightPaintMode)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(AppIcons.Edit, contentDescription = "权重绘制")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (state.isWeightPaintMode) "退出绘制" else "权重绘制")
            }

            ElevatedButton(
                onClick = {
                    onAction(EditorAction.SmoothSkinWeights)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Edit, contentDescription = "平滑权重")
                Spacer(modifier = Modifier.width(4.dp))
                Text("平滑权重")
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 8.dp))

        if (skeleton != null) {
            BoneWeightList(
                skeleton = skeleton,
                selectedBoneId = state.selectedBoneId,
                onBoneSelect = { boneId ->
                    onAction(EditorAction.SelectBone(boneId))
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "请先加载骨骼", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun BoneWeightList(
    skeleton: Skeleton,
    selectedBoneId: Int?,
    onBoneSelect: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(skeleton.bones) { bone ->
            val isSelected = selectedBoneId == bone.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                ),
                onClick = { onBoneSelect(bone.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skeleton),
                        contentDescription = "骨骼",
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bone.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun WeightPaintControls(
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    weightValue: Float,
    onWeightValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "画笔大小",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = brushSize,
            onValueChange = onBrushSizeChange,
            valueRange = 5f..50f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "${brushSize.toInt()}px",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "权重值",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = weightValue,
            onValueChange = onWeightValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "${"%.2f".format(weightValue)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun WeightDistributionBar(
    weights: com.example.glimmerseed.editorcore.skin.VertexWeights,
    skeleton: Skeleton
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weights.boneWeights.forEach { boneWeight ->
            val bone = skeleton.getBoneById(boneWeight.boneId)
            val color = getBoneColor(boneWeight.boneId)

            Surface(
                color = color,
                modifier = Modifier
                    .weight(boneWeight.weight)
                    .height(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (boneWeight.weight > 0.2f) {
                        Text(
                            text = bone?.name?.first()?.toString() ?: "?",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (weights.boneWeights.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "无权重",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getBoneColor(boneId: Int): Color {
    val colors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFF45B7D1),
        Color(0xFF96CEB4),
        Color(0xFFFFEAA7),
        Color(0xFFDDA0DD),
        Color(0xFFFF8C00),
        Color(0xFF98D8C8)
    )
    return colors[boneId % colors.size]
}
