package com.example.glimmerseed.editorcore.export

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Skeleton
import java.io.File

/**
 * 导出目标引擎类型
 */
enum class ExportEngineType {
    UNITY,
    UNREAL,
    GODOT,
    SPINE,
    GLTF
}

/**
 * 导出选项
 */
data class ExportOptions(
    /**
     * 目标引擎类型
     */
    val engineType: ExportEngineType,
    
    /**
     * 是否导出纹理图集
     */
    val exportTextureAtlas: Boolean = true,
    
    /**
     * 是否压缩纹理
     */
    val compressTextures: Boolean = true,
    
    /**
     * 导出帧率
     */
    val frameRate: Int = 30,
    
    /**
     * 是否导出所有动画
     */
    val exportAllAnimations: Boolean = true,
    
    /**
     * 要导出的特定动画列表
     */
    val animationList: List<String> = emptyList()
)

/**
 * 引擎导出器接口
 */
interface EngineExporter {
    /**
     * 导出骨骼和动画
     */
    fun export(
        skeleton: Skeleton,
        animations: List<AnimationClip>,
        outputDir: File,
        options: ExportOptions,
        texture: com.example.glimmerseed.editorcore.animation.TextureAsset? = null
    ): ExportResult
}

/**
 * 导出结果
 */
data class ExportResult(
    /**
     * 是否成功
     */
    val success: Boolean,
    
    /**
     * 导出的文件列表
     */
    val exportedFiles: List<File> = emptyList(),
    
    /**
     * 错误信息（如果失败）
     */
    val errorMessage: String? = null
)

/**
 * 导出管理器
 */
class ExportManager {
    
    private val exporters = mutableMapOf<ExportEngineType, EngineExporter>()
    
    /**
     * 注册导出器
     */
    fun registerExporter(type: ExportEngineType, exporter: EngineExporter) {
        exporters[type] = exporter
    }
    
    /**
     * 执行导出
     */
    fun export(
        skeleton: Skeleton,
        animations: List<AnimationClip>,
        outputDir: File,
        options: ExportOptions,
        texture: com.example.glimmerseed.editorcore.animation.TextureAsset? = null
    ): ExportResult {
        val exporter = exporters[options.engineType]
            ?: return ExportResult(false, errorMessage = "Exporter not found for ${options.engineType}")

        return exporter.export(skeleton, animations, outputDir, options, texture)
    }
}
