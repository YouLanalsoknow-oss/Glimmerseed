package com.example.glimmerseed.ui.asset

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.glimmerseed.R
import com.example.glimmerseed.editorcore.asset.AssetManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetPreviewDialog(
    asset: AssetManager.AssetDisplayItem,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (asset.thumbnailPath != null) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse("file://${asset.thumbnailPath}")),
                    contentDescription = asset.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = asset.displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_close_24),
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}