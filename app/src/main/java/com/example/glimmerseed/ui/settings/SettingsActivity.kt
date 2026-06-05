package com.example.glimmerseed.ui.settings

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.glimmerseed.R
import com.example.glimmerseed.app.data.SettingsDataStore
import com.example.glimmerseed.ui.common.AppIcons
import com.example.glimmerseed.ui.common.GlimmerseedTextField
import com.example.glimmerseed.network.AiClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

// 编辑器同款暖色调
private val BG = Color(0xFFEDE6DD)
private val SIDEBAR_BG = Color(0xFFE0D5C8)
private val CARD_BG = Color(0xFFF5F0E8)
private val ACCENT = Color(0xFF8D6E63)
private val TEXT_PRIMARY = Color(0xFF3E2723)
private val TEXT_SECONDARY = Color(0xFF5D4037)
private val TEXT_MUTED = Color(0xFF795548)
private val BORDER = Color(0xFFC2B2A1)
private val SELECTED_ITEM = Color(0xFFD6CBBE)
private val AVATAR_BG = Color(0xFFD6CBBE)

private enum class SettingsCategory(val label: String) {
    PROFILE("用户资料"),
    AI_CONFIG("AI 配置"),
    ABOUT("关于")
}

class SettingsActivity : ComponentActivity() {
    private var isTesting = false
    private val _hasAvatar = mutableStateOf(false)

    private val avatarPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            saveAvatarAndUpload(it)
            _hasAvatar.value = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _hasAvatar.value = getFileStreamPath("avatar.jpg").exists()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = ACCENT,
                    background = BG,
                    surface = CARD_BG,
                    onPrimary = Color.White,
                    onBackground = TEXT_PRIMARY,
                    onSurface = TEXT_PRIMARY
                )
            ) {
                SettingsScreen(
                    hasAvatar = _hasAvatar.value,
                    onPickAvatar = { avatarPickerLauncher.launch("image/*") },
                    onLogout = { logoutAndFinish() },
                    onFinish = { finish() }
                )
            }
        }
    }

    private fun saveAvatarAndUpload(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val outputStream = openFileOutput("avatar.jpg", MODE_PRIVATE)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show()

            // 异步上传到云端
            lifecycleScope.launch {
                try {
                    uploadAvatarToCloud()
                } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    private suspend fun uploadAvatarToCloud() {
        val settings = SettingsDataStore.getInstance()
        val token = settings.getTokenBlocking()
        if (token.isEmpty()) return

        val avatarFile = getFileStreamPath("avatar.jpg")
        if (!avatarFile.exists()) return

        withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL("http://8.134.80.158:8080/api/user/avatar")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.doOutput = true
                connection.connectTimeout = 15000

                val boundary = "Boundary-${System.currentTimeMillis()}"
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                val bytes = avatarFile.readBytes()
                val body = buildString {
                    append("--$boundary\r\n")
                    append("Content-Disposition: form-data; name=\"avatar\"; filename=\"avatar.jpg\"\r\n")
                    append("Content-Type: image/jpeg\r\n\r\n")
                }.toByteArray()

                connection.outputStream.use { out ->
                    out.write(body)
                    out.write(bytes)
                    out.write("\r\n--$boundary--\r\n".toByteArray())
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                if (responseCode == 200) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SettingsActivity, "头像已同步到云端", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun logoutAndFinish() {
        SettingsDataStore.getInstance().clearTokenBlocking()
        // 删除本地头像文件
        try {
            deleteFile("avatar.jpg")
        } catch (_: Exception) { }
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        isTesting = false
    }
}

@Composable
fun SettingsScreen(
    hasAvatar: Boolean,
    onPickAvatar: () -> Unit,
    onLogout: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember { SettingsDataStore.getInstance() }
    var selectedCategory by remember { mutableStateOf(SettingsCategory.PROFILE) }
    val isLoggedIn = remember { settings.isLoggedInBlocking() }
    val username = remember { settings.getUsernameBlocking() }
    val email = remember { settings.getEmailBlocking() }

    // AI 配置状态
    var apiKey by remember { mutableStateOf(settings.getApiKeyBlocking()) }
    var modelId by remember { mutableStateOf(settings.getModelIdBlocking()) }
    var apiUrl by remember { mutableStateOf(settings.getApiUrlBlocking()) }
    var format by remember { mutableStateOf(settings.getFormatBlocking()) }
    var statusText by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    // 关于信息
    val deviceId = remember { com.example.glimmerseed.network.DeviceIdManager.getOrCreate(context) }

    Column(modifier = Modifier.fillMaxSize().background(BG)) {
        // 顶栏
        Surface(
            color = SIDEBAR_BG,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("设置", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)
                TextButton(onClick = onFinish) {
                    Text("关闭", color = ACCENT, fontSize = 14.sp)
                }
            }
        }

        // 左右分栏
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧边栏
            Surface(
                modifier = Modifier.width(100.dp).fillMaxHeight(),
                color = SIDEBAR_BG
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SettingsCategory.entries.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = category },
                            color = if (isSelected) SELECTED_ITEM else Color.Transparent
                        ) {
                            Text(
                                text = category.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = TEXT_PRIMARY
                            )
                        }
                    }
                }
            }

            // 分隔线
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(BORDER))

            // 右侧内容
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                when (selectedCategory) {
                    SettingsCategory.PROFILE -> ProfileContent(
                        isLoggedIn = isLoggedIn,
                        username = username,
                        email = email,
                        hasAvatar = hasAvatar,
                        onPickAvatar = onPickAvatar,
                        onLogout = onLogout
                    )
                    SettingsCategory.AI_CONFIG -> AIConfigContent(
                        apiKey = apiKey,
                        modelId = modelId,
                        apiUrl = apiUrl,
                        format = format,
                        statusText = statusText,
                        isTesting = isTesting,
                        onApiKeyChange = { apiKey = it },
                        onModelIdChange = { modelId = it },
                        onApiUrlChange = { apiUrl = it },
                        onFormatChange = { format = it },
                        onSave = {
                            if (apiKey.isEmpty()) {
                                statusText = "请填写 API Key"
                                return@AIConfigContent
                            }
                            if (apiUrl.isEmpty()) {
                                statusText = "请填写请求地址"
                                return@AIConfigContent
                            }
                            settings.saveApiKeyBlocking(apiKey)
                            settings.saveModelIdBlocking(modelId.ifEmpty { SettingsDataStore.DEFAULT_MODEL_ID })
                            settings.saveApiUrlBlocking(apiUrl)
                            settings.saveFormatBlocking(format)
                            statusText = "✅ 设置已保存"
                            Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                        },
                        onTest = {
                            isTesting = true
                            statusText = "⏳ 正在测试连接..."
                            (context as? SettingsActivity)?.let { activity ->
                                activity.lifecycleScope.launch {
                                    val result = AiClient.sendMessage("测试宠物", "温柔", "你好，请回复\"连接成功\"")
                                    isTesting = false
                                    result.onSuccess { reply ->
                                        statusText = "✅ 连接成功！回复: ${reply.take(50)}"
                                        Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show()
                                    }.onFailure { e ->
                                        statusText = "❌ 连接失败: ${e.message}"
                                        Toast.makeText(context, "连接失败: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    )
                    SettingsCategory.ABOUT -> AboutContent(deviceId = deviceId)
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    isLoggedIn: Boolean,
    username: String,
    email: String,
    hasAvatar: Boolean,
    onPickAvatar: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("用户资料", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)

        // 头像预览
        Surface(
            modifier = Modifier.size(100.dp).clickable { if (isLoggedIn) onPickAvatar() },
            shape = CircleShape,
            color = AVATAR_BG,
            border = BorderStroke(2.dp, if (isLoggedIn) ACCENT else BORDER)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLoggedIn && hasAvatar) {
                    val bitmap = try {
                        android.graphics.BitmapFactory.decodeFile(
                            context.getFileStreamPath("avatar.jpg").absolutePath
                        )
                    } catch (_: Exception) { null }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(AppIcons.Person, null, tint = TEXT_MUTED, modifier = Modifier.size(48.dp))
                    }
                } else {
                    Icon(AppIcons.Person, null, tint = TEXT_MUTED, modifier = Modifier.size(48.dp))
                }
            }
        }
        if (isLoggedIn) {
            Text("点击更换头像", fontSize = 12.sp, color = TEXT_MUTED)
        }

        // 用户信息
        Surface(color = CARD_BG, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BORDER)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("用户名", fontSize = 14.sp, color = TEXT_MUTED)
                    Text(username.ifEmpty { "未设置" }, fontSize = 14.sp, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                }
                HorizontalDivider(color = BORDER)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("邮箱", fontSize = 14.sp, color = TEXT_MUTED)
                    Text(email.ifEmpty { "未设置" }, fontSize = 14.sp, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
                }
                HorizontalDivider(color = BORDER)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("登录状态", fontSize = 14.sp, color = TEXT_MUTED)
                    Text(if (isLoggedIn) "已登录" else "未登录", fontSize = 14.sp,
                        color = if (isLoggedIn) Color(0xFF4CAF50) else TEXT_MUTED,
                        fontWeight = FontWeight.Medium)
                }
            }
        }

        if (isLoggedIn) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe94560)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("退出登录", color = Color.White, fontSize = 14.sp)
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Text("登录后可自定义头像", fontSize = 13.sp, color = TEXT_MUTED)
        }
    }
}

@Composable
fun AIConfigContent(
    apiKey: String, modelId: String, apiUrl: String, format: String,
    statusText: String, isTesting: Boolean,
    onApiKeyChange: (String) -> Unit,
    onModelIdChange: (String) -> Unit,
    onApiUrlChange: (String) -> Unit,
    onFormatChange: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI 配置", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)

        GlimmerseedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            placeholder = "API Key",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ACCENT,
                unfocusedBorderColor = BORDER,
                focusedLabelColor = ACCENT,
                unfocusedLabelColor = TEXT_MUTED,
                cursorColor = ACCENT
            )
        )

        GlimmerseedTextField(
            value = modelId,
            onValueChange = onModelIdChange,
            placeholder = "模型 ID",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ACCENT,
                unfocusedBorderColor = BORDER,
                focusedLabelColor = ACCENT,
                unfocusedLabelColor = TEXT_MUTED,
                cursorColor = ACCENT
            )
        )

        GlimmerseedTextField(
            value = apiUrl,
            onValueChange = onApiUrlChange,
            placeholder = "请求地址",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ACCENT,
                unfocusedBorderColor = BORDER,
                focusedLabelColor = ACCENT,
                unfocusedLabelColor = TEXT_MUTED,
                cursorColor = ACCENT
            )
        )

        GlimmerseedTextField(
            value = format,
            onValueChange = onFormatChange,
            placeholder = "API 格式",
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ACCENT,
                unfocusedBorderColor = BORDER,
                focusedLabelColor = ACCENT,
                unfocusedLabelColor = TEXT_MUTED,
                cursorColor = ACCENT
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存设置", fontSize = 14.sp)
        }

        OutlinedButton(
            onClick = onTest,
            enabled = !isTesting,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, ACCENT)
        ) {
            Text(if (isTesting) "测试中..." else "测试连接", fontSize = 14.sp, color = ACCENT)
        }

        if (statusText.isNotEmpty()) {
            Text(statusText, fontSize = 13.sp, color = if (statusText.startsWith("✅") || statusText.startsWith("⏳")) TEXT_SECONDARY else Color(0xFFe94560))
        }
    }
}

@Composable
fun AboutContent(deviceId: String) {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (_: Exception) { null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("关于", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TEXT_PRIMARY)

        Surface(color = CARD_BG, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BORDER)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow("应用名称", "Glimmerseed")
                InfoRow("版本号", packageInfo?.versionName ?: "未知")
                InfoRow("版本代码", "${packageInfo?.versionCode ?: 0}")
                HorizontalDivider(color = BORDER)
                InfoRow("设备 ID", deviceId.ifEmpty { "未生成" })
                HorizontalDivider(color = BORDER)
                InfoRow("服务器", "http://8.134.80.158:8080")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, color = TEXT_MUTED)
        Text(value, fontSize = 14.sp, color = TEXT_PRIMARY, fontWeight = FontWeight.Medium)
    }
}