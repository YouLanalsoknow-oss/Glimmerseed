package com.example.glimmerseed.data.asset

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.glimmerseed.editorcore.asset.AssetManager
import com.example.glimmerseed.editorcore.asset.AssetRef
import com.example.glimmerseed.editorcore.asset.AssetSource
import com.example.glimmerseed.editorcore.asset.AssetType
import com.example.glimmerseed.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class AppAssetManager(context: Context) : AssetManager(context) {

    private val assetDao = AppDatabase.getInstance(context).assetDao()
    private val thumbnailGenerator = ThumbnailGenerator(context)
    private val contextRef = context.applicationContext

    private val officialAssetsCache = ConcurrentHashMap<String, List<AssetDisplayItem>>()
    private var manifestLoaded = false

    override suspend fun importAsset(uri: String): AssetRef? {
        return withContext(Dispatchers.IO) {
            try {
                val fileUri = Uri.parse(uri)
                val inputStream = contextRef.contentResolver.openInputStream(fileUri)
                    ?: return@withContext null

                val tempFile = File(contextRef.cacheDir, "temp_asset_${System.currentTimeMillis()}")
                val outputStream = FileOutputStream(tempFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                val assetId = calculateSHA256(tempFile)
                val assetType = detectAssetType(tempFile)
                val fileName = getFileNameFromUri(fileUri)

                val userAssetsDir = getUserAssetsDir(assetType)
                val targetFile = File(userAssetsDir, "$assetId${getFileExtension(tempFile)}")

                if (!targetFile.exists()) {
                    tempFile.copyTo(targetFile)
                }
                tempFile.delete()

                val thumbnailPath = thumbnailGenerator.generateThumbnail(targetFile.absolutePath, assetId)

                val assetEntity = AssetEntity(
                    assetId = assetId,
                    type = assetType.name,
                    localPath = targetFile.absolutePath,
                    thumbnailPath = thumbnailPath,
                    createdAt = System.currentTimeMillis(),
                    sizeBytes = targetFile.length(),
                    displayName = fileName
                )

                assetDao.insert(assetEntity)

                AssetRef(
                    assetId = assetId,
                    type = assetType,
                    source = AssetSource.USER,
                    localPath = targetFile.absolutePath
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun resolveAsset(assetId: String, source: AssetSource): AssetRef? {
        return withContext(Dispatchers.IO) {
            when (source) {
                AssetSource.USER -> {
                    val entity = assetDao.getAssetById(assetId)
                    if (entity != null && File(entity.localPath).exists()) {
                        AssetRef(
                            assetId = entity.assetId,
                            type = AssetType.valueOf(entity.type),
                            source = AssetSource.USER,
                            localPath = entity.localPath
                        )
                    } else {
                        AssetRef.createPlaceholder(AssetType.IMAGE)
                    }
                }
                AssetSource.OFFICIAL -> {
                    loadOfficialManifestIfNeeded()
                    val entry = officialAssetsCache["all"]?.find { it.assetId == assetId }
                    if (entry != null) {
                        AssetRef(
                            assetId = entry.assetId,
                            type = entry.type,
                            source = AssetSource.OFFICIAL,
                            localPath = "file:///android_asset/official/${entry.assetId}"
                        )
                    } else {
                        AssetRef.createPlaceholder(AssetType.IMAGE)
                    }
                }
            }
        }
    }

    override suspend fun deleteAsset(assetId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val entity = assetDao.getAssetById(assetId)
                if (entity == null) {
                    return@withContext false
                }

                val file = File(entity.localPath)
                if (file.exists()) {
                    file.delete()
                }

                thumbnailGenerator.deleteThumbnail(assetId)
                assetDao.deleteById(assetId)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override suspend fun loadBitmap(assetRef: AssetRef): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                if (assetRef.source == AssetSource.OFFICIAL) {
                    val assetPath = assetRef.localPath.replace("file:///android_asset/", "")
                    val inputStream = contextRef.assets.open(assetPath)
                    BitmapFactory.decodeStream(inputStream)
                } else {
                    BitmapFactory.decodeFile(assetRef.localPath)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun getAssetThumbnail(assetId: String): Bitmap? {
        return thumbnailGenerator.loadThumbnail(assetId)
    }

    override fun observeAllAssets(): Flow<List<AssetDisplayItem>> {
        return combine(
            observeUserAssets(),
            flow { emit(getOfficialAssets()) }
        ) { userAssets, officialAssets ->
            officialAssets + userAssets
        }
    }

    override fun observeUserAssets(): Flow<List<AssetDisplayItem>> {
        return assetDao.getAllAssets().map { entities ->
            entities.map { entity ->
                AssetDisplayItem(
                    assetId = entity.assetId,
                    type = AssetType.valueOf(entity.type),
                    source = AssetSource.USER,
                    displayName = entity.displayName,
                    thumbnailPath = entity.thumbnailPath,
                    createdAt = entity.createdAt,
                    sizeBytes = entity.sizeBytes
                )
            }.sortedByDescending { item -> item.createdAt }
        }
    }

    override suspend fun getAssetById(assetId: String): AssetDisplayItem? {
        return withContext(Dispatchers.IO) {
            val userAsset = assetDao.getAssetById(assetId)
            if (userAsset != null) {
                AssetDisplayItem(
                    assetId = userAsset.assetId,
                    type = AssetType.valueOf(userAsset.type),
                    source = AssetSource.USER,
                    displayName = userAsset.displayName,
                    thumbnailPath = userAsset.thumbnailPath,
                    createdAt = userAsset.createdAt,
                    sizeBytes = userAsset.sizeBytes
                )
            } else {
                loadOfficialManifestIfNeeded()
                officialAssetsCache["all"]?.find { item -> item.assetId == assetId }
            }
        }
    }

    private suspend fun getOfficialAssets(): List<AssetDisplayItem> {
        loadOfficialManifestIfNeeded()
        return officialAssetsCache["all"] ?: emptyList()
    }

    private suspend fun loadOfficialManifestIfNeeded() {
        if (manifestLoaded) return

        withContext(Dispatchers.IO) {
            try {
                val inputStream = contextRef.assets.open("official/manifest.json")
                val json = inputStream.bufferedReader().readText()
                inputStream.close()

                // 简单的JSON解析（实际项目中应该使用Gson或Moshi）
                val items = parseManifestJson(json)
                officialAssetsCache["all"] = items
                manifestLoaded = true
            } catch (e: Exception) {
                officialAssetsCache["all"] = emptyList()
            }
        }
    }
    
    private fun parseManifestJson(json: String): List<AssetDisplayItem> {
        val items = mutableListOf<AssetDisplayItem>()
        // 简单的JSON解析实现
        val jsonClean = json.trim()
        if (!jsonClean.startsWith("[") || !jsonClean.endsWith("]")) {
            return items
        }
        
        val content = jsonClean.substring(1, jsonClean.length - 1).trim()
        if (content.isEmpty()) {
            return items
        }
        
        // 分割数组中的对象
        val objects = splitJsonArray(content)
        
        for (objStr in objects) {
            val obj = parseJsonObject(objStr)
            val assetId = obj["assetId"] ?: continue
            val typeStr = obj["type"] ?: "IMAGE"
            val displayName = obj["displayName"] ?: "Unknown"
            val thumbnailPath = obj["thumbnailPath"]
            val sizeBytes = obj["sizeBytes"]?.toLongOrNull() ?: 0L
            val createdAt = obj["createdAt"]?.toLongOrNull() ?: System.currentTimeMillis()
            
            val type = try {
                AssetType.valueOf(typeStr)
            } catch (e: Exception) {
                AssetType.IMAGE
            }
            
            items.add(
                AssetDisplayItem(
                    assetId = assetId,
                    type = type,
                    source = AssetSource.OFFICIAL,
                    displayName = displayName,
                    thumbnailPath = thumbnailPath,
                    createdAt = createdAt,
                    sizeBytes = sizeBytes
                )
            )
        }
        
        return items
    }
    
    private fun splitJsonArray(content: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()
        var inString = false
        var escapeNext = false
        
        for (char in content) {
            when {
                escapeNext -> {
                    current.append(char)
                    escapeNext = false
                }
                char == '\\' -> {
                    current.append(char)
                    escapeNext = true
                }
                char == '"' -> {
                    current.append(char)
                    inString = !inString
                }
                inString -> {
                    current.append(char)
                }
                char == '{' -> {
                    depth++
                    current.append(char)
                }
                char == '}' -> {
                    current.append(char)
                    depth--
                    if (depth == 0) {
                        result.add(current.toString().trim())
                        current = StringBuilder()
                    }
                }
                char == ',' && depth == 0 -> {
                    // 跳过数组分割逗号
                }
                else -> {
                    if (current.isNotEmpty() || !char.isWhitespace()) {
                        current.append(char)
                    }
                }
            }
        }
        
        if (current.isNotEmpty()) {
            val trimmed = current.toString().trim()
            if (trimmed.isNotEmpty()) {
                result.add(trimmed)
            }
        }
        
        return result
    }
    
    private fun parseJsonObject(objStr: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val clean = objStr.trim()
        
        if (!clean.startsWith("{") || !clean.endsWith("}")) {
            return result
        }
        
        val content = clean.substring(1, clean.length - 1).trim()
        if (content.isEmpty()) {
            return result
        }
        
        // 简单的键值对解析
        val pairs = splitJsonPairs(content)
        for (pair in pairs) {
            val colonIndex = pair.indexOf(':')
            if (colonIndex > 0) {
                val key = pair.substring(0, colonIndex).trim().trim('"')
                val value = pair.substring(colonIndex + 1).trim().trim('"')
                result[key] = value
            }
        }
        
        return result
    }
    
    private fun splitJsonPairs(content: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inString = false
        var escapeNext = false
        var depth = 0
        
        for (char in content) {
            when {
                escapeNext -> {
                    current.append(char)
                    escapeNext = false
                }
                char == '\\' -> {
                    current.append(char)
                    escapeNext = true
                }
                char == '"' -> {
                    current.append(char)
                    inString = !inString
                }
                inString -> {
                    current.append(char)
                }
                char == '{' || char == '[' -> {
                    depth++
                    current.append(char)
                }
                char == '}' || char == ']' -> {
                    depth--
                    current.append(char)
                }
                char == ',' && depth == 0 -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }
        
        if (current.isNotEmpty()) {
            result.add(current.toString().trim())
        }
        
        return result
    }

    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun detectAssetType(file: File): AssetType {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp") -> AssetType.IMAGE
            name.endsWith(".glimmerseed") -> AssetType.SKELETON_PROJECT
            name.endsWith(".clip") -> AssetType.ANIMATION_CLIP
            name.endsWith(".mp3") || name.endsWith(".wav") -> AssetType.AUDIO
            name.endsWith(".ttf") || name.endsWith(".otf") -> AssetType.FONT
            else -> AssetType.IMAGE
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        return uri.lastPathSegment ?: "unknown"
    }

    private fun getFileExtension(file: File): String {
        val name = file.name
        val dotIndex = name.lastIndexOf('.')
        return if (dotIndex > 0) name.substring(dotIndex) else ""
    }

    private fun getUserAssetsDir(type: AssetType): File {
        val dirName = when (type) {
            AssetType.IMAGE -> "images"
            AssetType.SKELETON_PROJECT -> "projects"
            AssetType.ANIMATION_CLIP -> "clips"
            AssetType.AUDIO -> "audio"
            AssetType.FONT -> "fonts"
        }
        val dir = File(contextRef.filesDir, "assets/user/$dirName")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}