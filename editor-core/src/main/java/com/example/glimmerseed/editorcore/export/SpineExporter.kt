package com.example.glimmerseed.editorcore.export

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.TextureAsset
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Spine JSON骨骼数据结构
 */
@Serializable
data class SpineSkeletonJson(
    val skeleton: SkeletonInfo,
    val bones: List<BoneInfo>,
    val slots: List<SlotInfo> = emptyList(),
    val skins: Map<String, SkinInfo> = emptyMap(),
    val animations: Map<String, AnimationInfo> = emptyMap()
)

@Serializable
data class SkeletonInfo(
    val width: Float = 100f,
    val height: Float = 100f
)

@Serializable
data class BoneInfo(
    val name: String,
    val parent: String? = null,
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f
)

@Serializable
data class SlotInfo(
    val name: String,
    val bone: String,
    val attachment: String? = null
)

@Serializable
data class SkinInfo(
    val attachments: Map<String, Map<String, AttachmentInfo>> = emptyMap()
)

@Serializable
data class AttachmentInfo(
    val type: String = "region",
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f
)

@Serializable
data class AnimationInfo(
    val bones: Map<String, BoneTimelineInfo> = emptyMap()
)

@Serializable
data class BoneTimelineInfo(
    val rotate: List<TimelineKeyframe> = emptyList(),
    val translate: List<TimelineKeyframe> = emptyList(),
    val scale: List<TimelineKeyframe> = emptyList()
)

@Serializable
data class TimelineKeyframe(
    val time: Float,
    val x: Float = 0f,
    val y: Float = 0f,
    val angle: Float = 0f
)

/**
 * Spine导出器
 * 支持导出Spine JSON格式
 */
class SpineExporter : EngineExporter {
    
    private val json = Json {
        prettyPrint = true
    }
    
    override fun export(
        skeleton: Skeleton,
        animations: List<AnimationClip>,
        outputDir: File,
        options: ExportOptions,
        texture: TextureAsset?
    ): ExportResult {
        try {
            // 确保输出目录存在
            outputDir.mkdirs()

            // 转换骨骼数据
            val spineSkeleton = convertToSpineSkeleton(skeleton, animations, texture)

            // 写入JSON文件
            val jsonFileName = "skeleton_${System.currentTimeMillis()}.json"
            val jsonFile = File(outputDir, jsonFileName)
            jsonFile.writeText(json.encodeToString(spineSkeleton))

            val exportedFiles = mutableListOf<File>(jsonFile)

            // 生成并保存 atlas 文件
            if (texture != null && options.exportTextureAtlas) {
                val atlasFile = generateAtlasFile(outputDir, jsonFileName, texture)
                exportedFiles.add(atlasFile)

                // 保存纹理文件
                if (texture.data != null) {
                    val textureFile = saveTextureFile(outputDir, texture, options)
                    textureFile?.let { exportedFiles.add(it) }
                } else if (texture.filePath != null) {
                    // 复制纹理文件
                    val sourceFile = File(texture.filePath)
                    if (sourceFile.exists()) {
                        val destFile = File(outputDir, sourceFile.name)
                        sourceFile.copyTo(destFile, overwrite = true)
                        exportedFiles.add(destFile)
                    }
                }
            }

            return ExportResult(
                success = true,
                exportedFiles = exportedFiles
            )
        } catch (e: Exception) {
            return ExportResult(
                success = false,
                errorMessage = e.message
            )
        }
    }

    private fun generateAtlasFile(outputDir: File, skeletonJsonName: String, texture: TextureAsset): File {
        val baseName = skeletonJsonName.removeSuffix(".json")
        val atlasContent = buildSpineAtlas(baseName, texture)
        val atlasFile = File(outputDir, "$baseName.atlas")
        atlasFile.writeText(atlasContent)
        return atlasFile
    }

    private fun buildSpineAtlas(skeletonName: String, texture: TextureAsset): String {
        val textureFileName = "${skeletonName}.png"
        return buildString {
            appendLine("$skeletonName.atlas")
            appendLine()
            appendLine("$textureFileName")
            appendLine("  size: ${texture.width}, ${texture.height}")
            appendLine("  format: RGBA8888")
            appendLine("  filter: Linear, Linear")
            appendLine("  wrap: ClampToEdge")
            appendLine("  pma: false")
            appendLine()
            appendLine("${skeletonName}:")
            appendLine("  rotate: false")
            appendLine("  xy: 0, 0")
            appendLine("  size: ${texture.width}, ${texture.height}")
            appendLine("  orig: ${texture.width}, ${texture.height}")
            appendLine("  offset: 0, 0")
            appendLine("  pageBounds: 0, 0, ${texture.width}, ${texture.height}")
        }
    }

    private fun saveTextureFile(outputDir: File, texture: TextureAsset, options: ExportOptions): File? {
        return try {
            val extension = when {
                texture.filePath?.endsWith(".png", ignoreCase = true) == true -> "png"
                texture.filePath?.endsWith(".webp", ignoreCase = true) == true -> "webp"
                texture.filePath?.endsWith(".jpg", ignoreCase = true) == true -> "jpg"
                texture.filePath?.endsWith(".jpeg", ignoreCase = true) == true -> "jpeg"
                else -> "png"
            }

            val textureFileName = "texture_${texture.id}.$extension"
            val textureFile = File(outputDir, textureFileName)

            val dataToSave = if (texture.data != null && texture.data.isNotEmpty()) {
                texture.data
            } else if (texture.filePath != null) {
                val sourceFile = File(texture.filePath)
                if (sourceFile.exists()) {
                    sourceFile.readBytes()
                } else {
                    null
                }
            } else {
                null
            }

            if (dataToSave != null) {
                if (extension == "webp" && options.compressTextures) {
                    val format = WebPEncoder.formatFromExtension(textureFileName)
                    val encoded = WebPEncoder.encodeTexture(texture, format, WebPEncoder.Quality.HIGH)
                    if (encoded != null) {
                        textureFile.writeBytes(encoded)
                    } else {
                        textureFile.writeBytes(dataToSave)
                    }
                } else {
                    textureFile.writeBytes(dataToSave)
                }
            }

            textureFile
        } catch (e: Exception) {
            null
        }
    }

    private fun convertToSpineSkeleton(
        skeleton: Skeleton,
        animations: List<AnimationClip>,
        texture: TextureAsset?
    ): SpineSkeletonJson {
        // 转换骨骼
        val bones = skeleton.bones.map { bone ->
            val parentBone = bone.parentId?.let { id -> skeleton.bones.find { it.id == id } }
            val localPos = org.joml.Vector3f()
            bone.localTransform.getTranslation(localPos)
            
            BoneInfo(
                name = bone.name,
                parent = parentBone?.name,
                x = localPos.x,
                y = localPos.y
            )
        }
        
        // 转换动画（简化版）
        val animMap = mutableMapOf<String, AnimationInfo>()
        for (anim in animations) {
            val boneTimelines = mutableMapOf<String, BoneTimelineInfo>()
            // 简化的动画数据
            animMap[anim.name] = AnimationInfo(boneTimelines)
        }
        
        return SpineSkeletonJson(
            skeleton = SkeletonInfo(),
            bones = bones,
            animations = animMap
        )
    }
}
