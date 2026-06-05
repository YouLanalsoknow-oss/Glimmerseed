package com.example.glimmerseed.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.GlimmerseedTextField
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// 颜色定义
private val DIALOG_BG = Color(0xFFF5F0E8)
private val CARD_BG = Color(0xFFFFFFFF)
private val CARD_BG_SELECTED = Color(0xFFFFECB3)
private val BORDER = Color(0xFFD7CCC8)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF795548)
private val TEXT_MUTED = Color(0xFF9E9E9E)
private val ACCENT = Color(0xFF1565C0)
private val ACCENT_BG = Color(0xFFE3F2FD)
private val DANGER = Color(0xFFD32F2F)
private val DANGER_BG = Color(0xFFFFEBEE)

/**
 * 项目元数据
 */
data class ProjectMetadata(
    val id: String,
    val name: String,
    val path: String,
    val lastModified: Long,
    val sizeBytes: Long,
    val thumbnailPath: String? = null
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

/**
 * 项目模板类型
 */
enum class ProjectTemplate {
    EMPTY,      // 空白项目
    CHARACTER,  // 角色模板
    SCENE       // 场景模板
}

/**
 * 项目选择弹窗
 * 
 * 功能：
 * - 新建项目（输入名称、选择模板）
 * - 打开项目（显示项目列表）
 * - 项目元数据展示（名称、修改时间、大小、缩略图）
 */
@Composable
fun ProjectSelectionDialog(
    projectsDir: File,
    onNewProject: (name: String, template: ProjectTemplate) -> Unit,
    onOpenProject: (project: ProjectMetadata) -> Unit,
    onDismiss: () -> Unit,
    uiScale: Float = 1f
) {
    var selectedTab by remember { mutableStateOf(0) } // 0=打开, 1=新建
    var selectedProject by remember { mutableStateOf<ProjectMetadata?>(null) }
    var newProjectName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(ProjectTemplate.EMPTY) }
    
    // 加载项目列表
    val projects = remember(projectsDir) {
        loadProjects(projectsDir)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .width((400 * uiScale).dp)
                .heightIn(max = (500 * uiScale).dp),
            color = DIALOG_BG,
            shape = RoundedCornerShape((12 * uiScale).dp),
            border = BorderStroke(1.dp, BORDER)
        ) {
            Column(
                modifier = Modifier.padding((16 * uiScale).dp)
            ) {
                // 标题
                Text(
                    text = "项目选择",
                    fontSize = (18 * uiScale).sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_PRIMARY
                )
                
                Spacer(modifier = Modifier.height((16 * uiScale).dp))
                
                // Tab 切换
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    TabButton(
                        text = "打开项目",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        uiScale = uiScale
                    )
                    TabButton(
                        text = "新建项目",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        uiScale = uiScale
                    )
                }
                
                Spacer(modifier = Modifier.height((16 * uiScale).dp))
                
                // 内容区域
                when (selectedTab) {
                    0 -> {
                        // 打开项目
                        ProjectListPanel(
                            projects = projects,
                            selectedProject = selectedProject,
                            onProjectSelected = { selectedProject = it },
                            uiScale = uiScale
                        )
                    }
                    1 -> {
                        // 新建项目
                        NewProjectPanel(
                            projectName = newProjectName,
                            onProjectNameChange = { newProjectName = it },
                            selectedTemplate = selectedTemplate,
                            onTemplateSelected = { selectedTemplate = it },
                            uiScale = uiScale
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height((16 * uiScale).dp))
                
                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(horizontal = (8 * uiScale).dp)
                    ) {
                        Text(
                            text = "取消",
                            fontSize = (14 * uiScale).sp,
                            color = TEXT_SECONDARY
                        )
                    }
                    
                    Button(
                        onClick = {
                            when (selectedTab) {
                                0 -> selectedProject?.let { onOpenProject(it) }
                                1 -> {
                                    if (newProjectName.isNotBlank()) {
                                        onNewProject(newProjectName, selectedTemplate)
                                    }
                                }
                            }
                        },
                        enabled = when (selectedTab) {
                            0 -> selectedProject != null
                            1 -> newProjectName.isNotBlank()
                            else -> false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ACCENT,
                            disabledContainerColor = ACCENT.copy(alpha = 0.38f)
                        ),
                        shape = RoundedCornerShape((6 * uiScale).dp)
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> "打开"
                                1 -> "创建"
                                else -> ""
                            },
                            fontSize = (14 * uiScale).sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    uiScale: Float
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding((2 * uiScale).dp),
        color = if (selected) ACCENT_BG else Color.Transparent,
        shape = RoundedCornerShape((6 * uiScale).dp),
        border = if (selected) BorderStroke(1.dp, ACCENT) else null
    ) {
        Text(
            text = text,
            fontSize = (14 * uiScale).sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) ACCENT else TEXT_SECONDARY,
            modifier = Modifier.padding(
                horizontal = (16 * uiScale).dp,
                vertical = (8 * uiScale).dp
            )
        )
    }
}

@Composable
private fun ProjectListPanel(
    projects: List<ProjectMetadata>,
    selectedProject: ProjectMetadata?,
    onProjectSelected: (ProjectMetadata) -> Unit,
    uiScale: Float
) {
    if (projects.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((200 * uiScale).dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
            ) {
                Icon(
                        imageVector = AppIcons.Folder,
                        contentDescription = null,
                        tint = TEXT_MUTED,
                        modifier = Modifier.size((48 * uiScale).dp)
                    )
                Text(
                    text = "暂无项目",
                    fontSize = (14 * uiScale).sp,
                    color = TEXT_MUTED
                )
                Text(
                    text = "请新建一个项目开始创作",
                    fontSize = (12 * uiScale).sp,
                    color = TEXT_MUTED
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (280 * uiScale).dp),
            verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    selected = project == selectedProject,
                    onClick = { onProjectSelected(project) },
                    uiScale = uiScale
                )
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectMetadata,
    selected: Boolean,
    onClick: () -> Unit,
    uiScale: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) CARD_BG_SELECTED else CARD_BG,
        shape = RoundedCornerShape((8 * uiScale).dp),
        border = BorderStroke(1.dp, if (selected) ACCENT else BORDER)
    ) {
        Row(
            modifier = Modifier
                .padding((12 * uiScale).dp)
                .height((48 * uiScale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
        ) {
            // 缩略图占位
            Surface(
                modifier = Modifier
                    .size((48 * uiScale).dp),
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape((4 * uiScale).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppIcons.Image,
                        contentDescription = null,
                        tint = TEXT_MUTED,
                        modifier = Modifier.size((24 * uiScale).dp)
                    )
                }
            }
            
            // 项目信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
            ) {
                Text(
                    text = project.name,
                    fontSize = (14 * uiScale).sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_PRIMARY,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
                ) {
                    Text(
                        text = project.lastModifiedText,
                        fontSize = (11 * uiScale).sp,
                        color = TEXT_MUTED
                    )
                    Text(
                        text = project.sizeText,
                        fontSize = (11 * uiScale).sp,
                        color = TEXT_MUTED
                    )
                }
            }
            
            // 选中指示
            if (selected) {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    tint = ACCENT,
                    modifier = Modifier.size((20 * uiScale).dp)
                )
            }
        }
    }
}

@Composable
private fun NewProjectPanel(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    selectedTemplate: ProjectTemplate,
    onTemplateSelected: (ProjectTemplate) -> Unit,
    uiScale: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy((16 * uiScale).dp)
    ) {
        // 项目名称输入
        Column(
            verticalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
        ) {
            Text(
                text = "项目名称",
                fontSize = (12 * uiScale).sp,
                color = TEXT_SECONDARY
            )
            GlimmerseedTextField(
                value = projectName,
                onValueChange = onProjectNameChange,
                placeholder = "输入项目名称",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ACCENT,
                    unfocusedBorderColor = BORDER,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
        
        // 模板选择
        Column(
            verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            Text(
                text = "项目模板",
                fontSize = (12 * uiScale).sp,
                color = TEXT_SECONDARY
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
            ) {
                TemplateCard(
                    template = ProjectTemplate.EMPTY,
                    name = "空白项目",
                    description = "从零开始创建",
                    selected = selectedTemplate == ProjectTemplate.EMPTY,
                    onClick = { onTemplateSelected(ProjectTemplate.EMPTY) },
                    uiScale = uiScale,
                    modifier = Modifier.weight(1f)
                )
                TemplateCard(
                    template = ProjectTemplate.CHARACTER,
                    name = "角色模板",
                    description = "骨骼动画项目",
                    selected = selectedTemplate == ProjectTemplate.CHARACTER,
                    onClick = { onTemplateSelected(ProjectTemplate.CHARACTER) },
                    uiScale = uiScale,
                    modifier = Modifier.weight(1f)
                )
                TemplateCard(
                    template = ProjectTemplate.SCENE,
                    name = "场景模板",
                    description = "多图层场景",
                    selected = selectedTemplate == ProjectTemplate.SCENE,
                    onClick = { onTemplateSelected(ProjectTemplate.SCENE) },
                    uiScale = uiScale,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: ProjectTemplate,
    name: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    uiScale: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) ACCENT_BG else CARD_BG,
        shape = RoundedCornerShape((8 * uiScale).dp),
        border = BorderStroke(1.dp, if (selected) ACCENT else BORDER)
    ) {
        Column(
            modifier = Modifier
                .padding((12 * uiScale).dp)
                .heightIn(min = (80 * uiScale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
        ) {
            Icon(
                imageVector = when (template) {
                    ProjectTemplate.EMPTY -> AppIcons.Create
                    ProjectTemplate.CHARACTER -> AppIcons.Person
                    ProjectTemplate.SCENE -> AppIcons.Photo
                },
                contentDescription = null,
                tint = if (selected) ACCENT else TEXT_SECONDARY,
                modifier = Modifier.size((24 * uiScale).dp)
            )
            Text(
                text = name,
                fontSize = (12 * uiScale).sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) ACCENT else TEXT_PRIMARY
            )
            Text(
                text = description,
                fontSize = (10 * uiScale).sp,
                color = TEXT_MUTED,
                maxLines = 1
            )
        }
    }
}

/**
 * 加载项目列表
 */
private fun loadProjects(projectsDir: File): List<ProjectMetadata> {
    if (!projectsDir.exists()) {
        projectsDir.mkdirs()
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

/**
 * 计算目录大小
 */
private fun calculateDirSize(dir: File): Long {
    var size = 0L
    dir.walkTopDown().forEach {
        if (it.isFile) {
            size += it.length()
        }
    }
    return size
}
