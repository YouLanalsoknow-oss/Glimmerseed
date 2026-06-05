package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.editorcore.animation.InterpolationConfig.PresetType
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.editorcore.editor.EditorViewModel
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurveEditorPanel(
    viewModel: EditorViewModel,
    onAction: (EditorAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Add, contentDescription = "添加关键帧")
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加关键帧")
            }

            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Delete, contentDescription = "删除关键帧")
                Spacer(modifier = Modifier.width(4.dp))
                Text("删除关键帧")
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = AppIcons.List,
                    contentDescription = "曲线",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "曲线编辑器",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.TrendingFlat, contentDescription = "线性")
                Spacer(modifier = Modifier.width(4.dp))
                Text("线性", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Timeline, contentDescription = "平滑")
                Spacer(modifier = Modifier.width(4.dp))
                Text("平滑", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.TrendingDown, contentDescription = "缓入")
                Spacer(modifier = Modifier.width(4.dp))
                Text("缓入", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.TrendingUp, contentDescription = "缓出")
                Spacer(modifier = Modifier.width(4.dp))
                Text("缓出", fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Bounce, contentDescription = "弹跳")
                Spacer(modifier = Modifier.width(4.dp))
                Text("弹跳", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Bounce, contentDescription = "回弹")
                Spacer(modifier = Modifier.width(4.dp))
                Text("回弹", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Bounce, contentDescription = "弹簧")
                Spacer(modifier = Modifier.width(4.dp))
                Text("弹簧", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Icon(AppIcons.Square, contentDescription = "阶跃")
                Spacer(modifier = Modifier.width(4.dp))
                Text("阶跃", fontSize = 12.sp)
            }
        }
    }
}