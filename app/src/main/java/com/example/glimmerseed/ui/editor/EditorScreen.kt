package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.glimmerseed.editorcore.editor.EditorViewModel
import com.example.glimmerseed.editorcore.editor.TimelineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    editorViewModel: EditorViewModel,
    timelineViewModel: TimelineViewModel,
    onToggleStage: () -> Unit = {},
    onRequestPermission: () -> Unit = {},
    onFeedGlimmerseedToStage: (Any) -> Unit = {}
) {
    val context = LocalContext.current
    val editorState by editorViewModel.state.collectAsState()
    
    var showMoreMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Glimmerseed 编辑器") },
                actions = {
                    IconButton(onClick = { onToggleStage() }) {
                        Text("舞台")
                    }
                    IconButton(onClick = { showMoreMenu = true }) {
                        Text("更多")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(EditorColors.BEIGE_BG)
        ) {
            // 这里是编辑器主要内容区域的占位符
            // 实际项目中会添加完整的编辑器布局
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    "编辑器已加载",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EditorColors.TEXT_PRIMARY
                )
            }
            
            if (showMoreMenu) {
                MoreMenu(
                    onDismiss = { showMoreMenu = false },
                    onSave = { /* 保存功能 */ },
                    onLoad = { /* 加载功能 */ },
                    onExport = { /* 导出功能 */ }
                )
            }
        }
    }
}
