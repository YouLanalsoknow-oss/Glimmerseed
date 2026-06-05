package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun MoreMenu(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onLoad: () -> Unit,
    onExport: () -> Unit,
    onExportGlimmerseed: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "菜单",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                MenuItem(
                    text = "保存项目",
                    onClick = { onSave(); onDismiss() }
                )
                
                MenuItem(
                    text = "打开项目",
                    onClick = { onLoad(); onDismiss() }
                )
                
                MenuItem(
                    text = "导出动画",
                    onClick = { onExport(); onDismiss() }
                )
                
                MenuItem(
                    text = "导出 Glimmerseed",
                    onClick = { onExportGlimmerseed(); onDismiss() }
                )
            }
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
