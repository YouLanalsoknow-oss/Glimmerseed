package com.example.glimmerseed.editorcore.animation

import kotlinx.serialization.Serializable

/**
 * 纹理资产
 * 管理动画中使用的纹理数据
 */
@Serializable
data class TextureAsset(
    /**
     * 纹理唯一标识符
     */
    val id: String,

    /**
     * 纹理名称（不含路径）
     */
    val name: String,

    /**
     * 纹理宽度（像素）
     */
    val width: Int,

    /**
     * 纹理高度（像素）
     */
    val height: Int,

    /**
     * 纹理格式
     */
    val format: TextureFormat = TextureFormat.RGBA8888,

    /**
     * 纹理数据（可选，用于运行时）
     * 如果为null，则从filePath加载
     */
    val data: ByteArray? = null,

    /**
     * 纹理文件路径（可选）
     * 如果data不为null，则此字段可能被忽略
     */
    val filePath: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextureAsset

        if (id != other.id) return false
        if (name != other.name) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (format != other.format) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (filePath != other.filePath) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + format.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + (filePath?.hashCode() ?: 0)
        return result
    }

    companion object {
        /**
         * 创建空纹理
         */
        fun empty(id: String, name: String): TextureAsset {
            return TextureAsset(
                id = id,
                name = name,
                width = 0,
                height = 0
            )
        }

        /**
         * 计算纹理字节大小
         */
        fun calculateSize(width: Int, height: Int, format: TextureFormat): Int {
            return when (format) {
                TextureFormat.RGBA8888 -> width * height * 4
                TextureFormat.RGB888 -> width * height * 3
                TextureFormat.RGBA4444 -> width * height * 2
                TextureFormat.RGBA5551 -> width * height * 2
                TextureFormat.LUMINANCE_ALPHA -> width * height * 2
                TextureFormat.LUMINANCE -> width * height
                TextureFormat.ALPHA -> width * height
            }
        }
    }
}

/**
 * 纹理格式枚举
 */
@Serializable
enum class TextureFormat {
    RGBA8888,
    RGB888,
    RGBA4444,
    RGBA5551,
    LUMINANCE_ALPHA,
    LUMINANCE,
    ALPHA
}
