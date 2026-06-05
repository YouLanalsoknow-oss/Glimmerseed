package com.example.glimmerseed.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import com.example.glimmerseed.R
import com.example.glimmerseed.app.data.SettingsDataStore
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.GlimmerseedTextField
import com.example.glimmerseed.ui.editor.EditorActivity
import com.example.glimmerseed.ui.settings.SettingsActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
// 编辑器同款暖色调
private val PRIMARY_BG = Color(0xFFEDE6DD)
private val SECONDARY_BG = Color(0xAAEDE6DD) // 半透明白雾，对话框/卡片用 70% 透明度
private val CARD_BG = Color(0xFFF5F0E8)
private val ACCENT = Color(0xFF8D6E63)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF5D4037)
private val TEXT_MUTED = Color(0xFF795548)
private val BORDER = Color(0xFFC2B2A1)
private val AVATAR_BG = Color(0xFFD6CBBE)

data class ProjectMetadata(
    val id: String,
    val name: String,
    val path: String,
    val lastModified: Long,
    val sizeBytes: Long
) {
    val lastModifiedText: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

    val sizeText: String
        get() {
            return when {
                sizeBytes < 1024 -> "$sizeBytes B"
                sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
                else -> "${sizeBytes / 1024 / 1024} MB"
            }
        }
}

class MainActivity : AppCompatActivity() {
    private var userPopup: PopupWindow? = null

    // 在 Activity 级别维护登录状态，onResume 时刷新
    private val _isLoggedIn = mutableStateOf(false)
    private val _hasAvatar = mutableStateOf(false)

    private val loginLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (SettingsDataStore.getInstance().isLoggedInBlocking()) {
                Toast.makeText(this, "登录成功！欢迎回来～", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = ACCENT,
                    background = PRIMARY_BG,
                    surface = CARD_BG,
                    onPrimary = Color.White,
                    onBackground = TEXT_PRIMARY,
                    onSurface = TEXT_PRIMARY
                )
            ) {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        _isLoggedIn.value = SettingsDataStore.getInstance().isLoggedInBlocking()
        _hasAvatar.value = getFileStreamPath("avatar.jpg").exists()
        // 登录后自动下载云端头像
        if (_isLoggedIn.value && !_hasAvatar.value) {
            downloadAvatarFromCloud()
        }
    }

    private fun downloadAvatarFromCloud() {
        val settings = SettingsDataStore.getInstance()
        val avatarUrl = settings.getAvatarUrlBlocking()
        if (avatarUrl.isEmpty()) return

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val url = java.net.URL(avatarUrl)
                val connection = url.openConnection() as? java.net.HttpURLConnection ?: return@launch
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer ${settings.getTokenBlocking()}")

                if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    return@launch
                }

                val outputFile = getFileStreamPath("avatar.jpg")
                connection.inputStream.use { input ->
                    java.io.FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
                connection.disconnect()
                runOnUiThread { _hasAvatar.value = true }
            } catch (_: Exception) { }
        }
    }

    private fun getProjectsDirectory(): File {
        return File(getExternalFilesDir(null), "projects").apply {
            mkdirs()
        }
    }

    private fun loadProjects(): List<ProjectMetadata> {
        val projectsDir = getProjectsDirectory()
        if (!projectsDir.exists()) {
            return emptyList()
        }

        return projectsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { dir ->
                val projectFile = File(dir, "project.json")
                if (projectFile.exists()) {
                    ProjectMetadata(
                        id = dir.name,
                        name = dir.name,
                        path = dir.absolutePath,
                        lastModified = dir.lastModified(),
                        sizeBytes = calculateDirSize(dir)
                    )
                } else null
            }
            ?.sortedByDescending { it.lastModified }
            ?: emptyList()
    }

    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        dir.walkTopDown().forEach {
            if (it.isFile) {
                size += it.length()
            }
        }
        return size
    }

    private fun createNewProject(name: String) {
        val projectsDir = getProjectsDirectory()
        val projectDir = File(projectsDir, name)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
            // 创建合法的 v3 ProjectFile（含初始骨骼数据）
            val testSkeleton = com.example.glimmerseed.test.TestDataGenerator.generateHumanSkeleton()
            val testMesh = com.example.glimmerseed.test.TestDataGenerator.generateSimpleMesh()
            val testAnimation = com.example.glimmerseed.test.TestDataGenerator.generateWalkAnimation()
            val projectFile = com.example.glimmerseed.editorcore.io.ProjectFile(
                version = 3,
                name = name,
                skeleton = com.example.glimmerseed.editorcore.io.SkeletonData.fromSkeleton(testSkeleton),
                animations = listOf(
                    com.example.glimmerseed.editorcore.io.AnimationData(
                        name = testAnimation.name,
                        duration = testAnimation.duration
                    )
                )
            )
            val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
            val jsonString = json.encodeToString(com.example.glimmerseed.editorcore.io.ProjectFile.serializer(), projectFile)
            File(projectDir, "project.json").writeText(jsonString)
        }
    }

    private fun openProject(project: ProjectMetadata) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra("projectPath", project.path)
            putExtra("projectName", project.name)
        }
        startActivity(intent)
    }

    private fun deleteProject(project: ProjectMetadata) {
        AlertDialog.Builder(this)
            .setTitle("删除项目")
            .setMessage("确定要删除「${project.name}」吗？此操作不可恢复。")
            .setPositiveButton("删除") { _, _ ->
                val dir = File(project.path)
                if (dir.exists() && dir.isDirectory) {
                    dir.deleteRecursively()
                }
                Toast.makeText(this, "已删除项目", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun renameProject(project: ProjectMetadata) {
        // 使用系统文件选择器保存为新名称
        val contract = ActivityResultContracts.CreateDocument("application/json")
        val launcher = registerForActivityResult(contract) { uri ->
            if (uri != null) {
                val oldDir = File(project.path)
                val newName = java.io.File(uri.path ?: return@registerForActivityResult).nameWithoutExtension
                val newDir = File(oldDir.parentFile, newName)
                if (oldDir.renameTo(newDir)) {
                    Toast.makeText(this, "已重命名为 $newName", Toast.LENGTH_SHORT).show()
                    recreate()
                } else {
                    Toast.makeText(this, "重命名失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
        launcher.launch("${project.name}.json")
    }

    private fun goToLogin() {
        loginLauncher.launch(Intent(this, com.example.glimmerseed.ui.auth.LoginActivity::class.java))
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("退出登录")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("退出") { _, _ ->
                SettingsDataStore.getInstance().clearTokenBlocking()
                Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        val projects by remember { mutableStateOf(loadProjects()) }
        var showNewProjectDialog by remember { mutableStateOf(false) }
        var newProjectName by remember { mutableStateOf("") }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 背景图（drawable-nodpi 无密度缩放，保持 4K 原始画质）
            val wallpaperBitmap = remember {
                try {
                    android.graphics.BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.main_wallpaper
                    )?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }

            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                wallpaperBitmap?.let { bitmap ->
                    val imgWidth = bitmap.width.toFloat()
                    val imgHeight = bitmap.height.toFloat()
                    val scale = max(size.width / imgWidth, size.height / imgHeight)
                    val drawW = imgWidth * scale
                    val drawH = imgHeight * scale
                    val offsetX = (size.width - drawW) / 2f
                    val offsetY = (size.height - drawH) / 2f
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(offsetX.toInt(), offsetY.toInt()),
                        dstSize = IntSize(drawW.toInt(), drawH.toInt())
                    )
                }
            }

            // 内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 顶部栏：用户头像 + 标题
                TopBar()

                // 项目列表或空状态
                if (projects.isEmpty()) {
                    EmptyState(
                        onNewProject = { showNewProjectDialog = true }
                    )
                } else {
                    ProjectList(
                        projects = projects,
                        onProjectClick = { project ->
                            openProject(project)
                        },
                        onNewProject = { showNewProjectDialog = true },
                        onDelete = { deleteProject(it) },
                        onRename = { renameProject(it) }
                    )
                }
            }

            // 新建项目弹窗
            if (showNewProjectDialog) {
                NewProjectDialog(
                    onDismiss = {
                        showNewProjectDialog = false
                        newProjectName = ""
                    },
                    onCreate = { name ->
                        createNewProject(name)
                        showNewProjectDialog = false
                        newProjectName = ""
                        recreate()
                    }
                ) { newProjectName = it }
            }
        }
    }

    @Composable
    fun TopBar() {
        val context = LocalContext.current

        val activity = context as? MainActivity
        val isLoggedIn by activity?._isLoggedIn ?: remember { mutableStateOf(false) }
        val hasAvatar by activity?._hasAvatar ?: remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像区域
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (isLoggedIn) {
                            openSettings()
                        } else {
                            goToLogin()
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    2.dp,
                    if (isLoggedIn) Color(0xFF8D6E63) else Color(0xFFC2B2A1)
                ),
                color = AVATAR_BG
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoggedIn && hasAvatar) {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(
                            getFileStreamPath("avatar.jpg").absolutePath
                        )?.asImageBitmap()
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = AppIcons.Person,
                                contentDescription = null,
                                tint = Color(0xFF5D4037),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = AppIcons.Person,
                            contentDescription = null,
                            tint = Color(0xFF5D4037),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun EmptyState(onNewProject: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = AppIcons.Folder,
                    contentDescription = null,
                    tint = TEXT_MUTED,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "还没有项目",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_PRIMARY
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击下方按钮创建你的第一个项目",
                    fontSize = 14.sp,
                    color = TEXT_SECONDARY
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNewProject,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ACCENT,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "新建项目",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun ProjectList(
        projects: List<ProjectMetadata>,
        onProjectClick: (ProjectMetadata) -> Unit,
        onNewProject: () -> Unit,
        onDelete: (ProjectMetadata) -> Unit,
        onRename: (ProjectMetadata) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onProjectClick(project) },
                        onDelete = { deleteProject(it) },
                        onRename = { renameProject(it) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onNewProject,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ACCENT,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "新建项目",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ProjectCard(
        project: ProjectMetadata,
        onClick: () -> Unit,
        onDelete: (ProjectMetadata) -> Unit,
        onRename: (ProjectMetadata) -> Unit
    ) {
        var showMenu by remember { mutableStateOf(false) }

        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { showMenu = true }
                    ),
                color = CARD_BG,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BORDER)
            ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SECONDARY_BG, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.Folder,
                            contentDescription = null,
                            tint = ACCENT,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = project.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = TEXT_PRIMARY,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = project.lastModifiedText,
                                fontSize = 12.sp,
                                color = TEXT_SECONDARY
                            )
                            Text(
                                text = project.sizeText,
                                fontSize = 12.sp,
                                color = TEXT_SECONDARY
                            )
                        }
                    }
                }

                Icon(
                    imageVector = AppIcons.ArrowDropRight,
                    contentDescription = null,
                    tint = TEXT_SECONDARY,
                    modifier = Modifier.size(20.dp)
                )
            }
            }

            // 长按弹出菜单
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("重命名", color = TEXT_PRIMARY) },
                    onClick = {
                        showMenu = false
                        onRename(project)
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除", color = Color(0xFFE57373)) },
                    onClick = {
                        showMenu = false
                        onDelete(project)
                    }
                )
            }
        }
    }

    @Composable
    fun NewProjectDialog(
        onDismiss: () -> Unit,
        onCreate: (String) -> Unit,
        onNameChange: (String) -> Unit
    ) {
        var projectName by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp),
                color = SECONDARY_BG,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BORDER)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "新建项目",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_PRIMARY
                    )

                    GlimmerseedTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            onNameChange(it)
                        },
                        placeholder = "输入项目名称",
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ACCENT,
                            unfocusedBorderColor = Color(0x44ffffff),
                            focusedContainerColor = PRIMARY_BG,
                            unfocusedContainerColor = PRIMARY_BG,
                            focusedLabelColor = ACCENT,
                            unfocusedLabelColor = TEXT_SECONDARY,
                            cursorColor = ACCENT
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = TEXT_SECONDARY
                            )
                        ) {
                            Text(text = "取消")
                        }
                        Button(
                            onClick = {
                                if (projectName.isNotBlank()) {
                                    onCreate(projectName)
                                }
                            },
                            enabled = projectName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ACCENT,
                                contentColor = Color.White,
                                disabledContainerColor = ACCENT.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "创建")
                        }
                    }
                }
            }
        }
    }
}