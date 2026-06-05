package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glimmerseed.ui.common.AppIcons

@Composable
fun ToolbarIconButton(
    iconResId: Any,
    contentDescription: String,
    label: String,
    onClick: () -> Unit,
    onShowTooltip: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    uiScale: Float = 1f
) {
    Surface(
        modifier = modifier.size((36 * uiScale).dp).clickable(enabled = enabled, onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape((8 * uiScale).dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // 这里简化实现，实际项目中可以用 painterResource 加载图标
            Text(
                text = label.take(2),
                color = if (enabled) EditorColors.TEXT_PRIMARY else EditorColors.TEXT_MUTED.copy(alpha = 0.38f),
                fontSize = (12 * uiScale).sp
            )
        }
    }
}

@Composable
fun PermissionRequestDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要悬浮窗权限") },
        text = { Text("为了能够打开舞台预览，请授予悬浮窗权限。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
