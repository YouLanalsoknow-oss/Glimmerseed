package com.example.glimmerseed.ui.editor

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.glimmerseed.editorcore.editor.EditorAction
import com.example.glimmerseed.editorcore.editor.EditorViewModel
import com.example.glimmerseed.editorcore.editor.TimelineViewModel
import com.example.glimmerseed.floatingpreviewbase.service.StageService
import com.example.glimmerseed.floatingpreviewbase.FloatingWindowPermissionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditorActivity : ComponentActivity() {

    private val editorViewModel: EditorViewModel by lazy {
        ViewModelProvider(this, EditorViewModelFactory(applicationContext))[EditorViewModel::class.java]
    }
    private val timelineViewModel: TimelineViewModel by viewModels()
    private lateinit var permissionManager: FloatingWindowPermissionManager
    private var showPermissionDialog by mutableStateOf(false)

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

        permissionManager = FloatingWindowPermissionManager(this)

        setContent {
            androidx.compose.material3.MaterialTheme {
                if (showPermissionDialog) {
                    PermissionRequestDialog(
                        onDismiss = { showPermissionDialog = false },
                        onConfirm = {
                            requestOverlayPermission()
                            showPermissionDialog = false
                        }
                    )
                }

                EditorScreen(
                    editorViewModel = editorViewModel,
                    timelineViewModel = timelineViewModel,
                    onToggleStage = { toggleStage() },
                    onRequestPermission = { requestOverlayPermission() },
                    onFeedGlimmerseedToStage = { /* 暂时留空 */ }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionManager.refreshPermissionState()
        if (permissionManager.checkPermission()) {
            StageService.startService(this)
        }
    }

    private fun toggleStage() {
        val hasPermission = permissionManager.checkPermission()
        if (hasPermission) {
            StageService.startService(this)
            lifecycleScope.launch {
                delay(500L)
                feedEditorToStage()
            }
        } else {
            showPermissionDialog = true
        }
    }

    private fun feedEditorToStage() {
        val renderer = StageService.getPanelRenderer() ?: return
        val state = kotlinx.coroutines.runBlocking { editorViewModel.state.first() }
        renderer.setEditorPreview(
            skeleton = state.skeleton ?: return,
            clip = state.currentAnimation,
            mesh = state.mesh,
            texture = null
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        StageService.getPanelRenderer()?.stopEditorPreview()
    }

    private fun requestOverlayPermission() {
        permissionManager.requestPermission()?.let { intent ->
            startActivity(intent)
        }
    }
}
