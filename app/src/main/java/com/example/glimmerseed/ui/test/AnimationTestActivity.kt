package com.example.glimmerseed.ui.test

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.glimmerseed.editorcore.animation.AnimationPlayer
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.renderengine.opengl.GLESRenderSurface
import com.example.glimmerseed.renderengine.opengl.GLESTexture
import com.example.glimmerseed.test.PerformanceMonitor
import com.example.glimmerseed.test.TestDataGenerator
import kotlinx.coroutines.flow.collectLatest

/**
 * 动画测试Activity
 */
class AnimationTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }

        val skeleton = TestDataGenerator.generateHumanSkeleton()
        val mesh = TestDataGenerator.generateSimpleMesh()
        val texture = TestDataGenerator.generateTestTexture()
        val glTexture = GLESTexture(texture)
        val animation = TestDataGenerator.generateWalkAnimation()
        val player = AnimationPlayer(skeleton)
        val monitor = PerformanceMonitor()

        setContent {
            MaterialTheme {
                AnimationTestScreen(
                    skeleton = skeleton,
                    mesh = mesh,
                    texture = glTexture,
                    animationPlayer = player,
                    performanceMonitor = monitor
                )
            }
        }
    }
}

@Composable
fun AnimationTestScreen(
    skeleton: Skeleton,
    mesh: com.example.glimmerseed.editorcore.animation.Mesh,
    texture: GLESTexture,
    animationPlayer: AnimationPlayer,
    performanceMonitor: PerformanceMonitor
) {
    val context = LocalContext.current
    val renderSurface = remember { GLESRenderSurface(context) }

    var currentSkeleton by remember { mutableStateOf(skeleton) }
    var isPlaying by remember { mutableStateOf(false) }
    var fps by remember { mutableStateOf(0) }

    // 初始化骨骼世界变换
    LaunchedEffect(Unit) {
        currentSkeleton.updateAllWorldTransforms()
        renderSurface.setData(currentSkeleton, mesh, texture)
    }

    // 监听动画更新
    LaunchedEffect(Unit) {
        animationPlayer.onAnimationUpdate.collectLatest { updatedSkeleton ->
            currentSkeleton = updatedSkeleton
            currentSkeleton.updateAllWorldTransforms()
            renderSurface.setData(currentSkeleton, mesh, texture)
            fps = performanceMonitor.fps
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 渲染区域
        AndroidView(
            factory = { renderSurface },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // FPS显示
        Text(
            text = "FPS: $fps",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!isPlaying) {
                        animationPlayer.play(TestDataGenerator.generateWalkAnimation())
                        isPlaying = true
                    } else {
                        animationPlayer.pause()
                        isPlaying = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isPlaying) "暂停" else "播放")
            }

            Button(
                onClick = {
                    animationPlayer.stop()
                    isPlaying = false
                    // 重置到原始骨骼
                    val freshSkeleton = TestDataGenerator.generateHumanSkeleton()
                    freshSkeleton.updateAllWorldTransforms()
                    currentSkeleton = freshSkeleton
                    renderSurface.setData(currentSkeleton, mesh, texture)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("停止")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 速度控制
        Text("播放速度: ${String.format("%.1f", animationPlayer.playbackSpeed)}x")
        Slider(
            value = animationPlayer.playbackSpeed,
            onValueChange = { animationPlayer.playbackSpeed = it },
            valueRange = 0.1f..3f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
