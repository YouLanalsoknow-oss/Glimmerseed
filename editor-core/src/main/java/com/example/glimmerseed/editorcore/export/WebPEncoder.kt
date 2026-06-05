package com.example.glimmerseed.editorcore.export

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.glimmerseed.editorcore.animation.TextureAsset
import com.example.glimmerseed.editorcore.animation.TextureFormat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * WebP 纹理编码器
 * 支持 PNG、JPG、WebP 格式的编码
 */
object WebPEncoder {

    /**
     * WebP 编码质量级别
     */
    enum class Quality(val percentage: Int) {
        LOW(50),
        MEDIUM(75),
        HIGH(90),
        ULTRA(100)
    }

    /**
     * 将 TextureAsset 编码为字节数组
     *
     * @param texture 纹理资产
     * @param format 输出格式（PNG/JPG/WebP）
     * @param quality 编码质量（仅对 JPG 和 WebP 有效）
     * @return 编码后的字节数组，失败返回 null
     */
    fun encodeTexture(
        texture: TextureAsset,
        format: OutputFormat = OutputFormat.WEBP,
        quality: Quality = Quality.HIGH
    ): ByteArray? {
        return try {
            if (texture.data != null && texture.data.isNotEmpty()) {
                decodeAndReencode(texture.data, format, quality)
            } else if (texture.filePath != null) {
                val file = File(texture.filePath)
                if (file.exists()) {
                    encodeFromFile(file, format, quality)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 将 TextureAsset 编码并保存到文件
     *
     * @param texture 纹理资产
     * @param outputFile 输出文件
     * @param format 输出格式
     * @param quality 编码质量
     * @return 成功返回 true
     */
    fun encodeAndSave(
        texture: TextureAsset,
        outputFile: File,
        format: OutputFormat = OutputFormat.WEBP,
        quality: Quality = Quality.HIGH
    ): Boolean {
        return try {
            val data = encodeTexture(texture, format, quality)
            if (data != null) {
                FileOutputStream(outputFile).use { fos ->
                    fos.write(data)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 将 RGBA 字节数组编码为 WebP
     *
     * @param rgbaData RGBA 格式的字节数组
     * @param width 纹理宽度
     * @param height 纹理高度
     * @param format 输出格式
     * @param quality 编码质量
     * @return 编码后的字节数组
     */
    fun encodeFromRgba(
        rgbaData: ByteArray,
        width: Int,
        height: Int,
        format: OutputFormat = OutputFormat.WEBP,
        quality: Quality = Quality.HIGH
    ): ByteArray? {
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(
                IntArray(width * height) { i ->
                    val r = rgbaData[i * 4].toInt() and 0xFF
                    val g = rgbaData[i * 4 + 1].toInt() and 0xFF
                    val b = rgbaData[i * 4 + 2].toInt() and 0xFF
                    val a = rgbaData[i * 4 + 3].toInt() and 0xFF
                    (a shl 24) or (r shl 16) or (g shl 8) or b
                },
                0,
                width,
                0,
                0,
                width,
                height
            )

            val outputStream = ByteArrayOutputStream()
            val compressFormat = when (format) {
                OutputFormat.PNG -> Bitmap.CompressFormat.PNG
                OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
                OutputFormat.WEBP -> Bitmap.CompressFormat.WEBP
            }

            bitmap.compress(compressFormat, quality.percentage, outputStream)
            bitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun encodeFromFile(file: File, format: OutputFormat, quality: Quality): ByteArray? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null

        val outputStream = ByteArrayOutputStream()
        val compressFormat = when (format) {
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.WEBP -> Bitmap.CompressFormat.WEBP
        }

        bitmap.compress(compressFormat, quality.percentage, outputStream)
        bitmap.recycle()

        return outputStream.toByteArray()
    }

    private fun decodeAndReencode(data: ByteArray, format: OutputFormat, quality: Quality): ByteArray? {
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size) ?: return null

        val outputStream = ByteArrayOutputStream()
        val compressFormat = when (format) {
            OutputFormat.PNG -> Bitmap.CompressFormat.PNG
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.WEBP -> Bitmap.CompressFormat.WEBP
        }

        bitmap.compress(compressFormat, quality.percentage, outputStream)
        bitmap.recycle()

        return outputStream.toByteArray()
    }

    /**
     * 从字节数组解码为 Bitmap
     *
     * @param data 图像数据
     * @return 解码后的 Bitmap，失败返回 null
     */
    fun decodeToBitmap(data: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(data, 0, data.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查设备是否支持指定格式的编码
     *
     * @param format 输出格式
     * @return 支持返回 true
     */
    fun isFormatSupported(format: OutputFormat): Boolean {
        return when (format) {
            OutputFormat.PNG -> true
            OutputFormat.JPEG -> true
            OutputFormat.WEBP -> true
        }
    }

    /**
     * 输出格式枚举
     */
    enum class OutputFormat(val extension: String, val mimeType: String) {
        PNG("png", "image/png"),
        JPEG("jpg", "image/jpeg"),
        WEBP("webp", "image/webp")
    }

    /**
     * 从文件扩展名推断格式
     *
     * @param fileName 文件名
     * @return 推断的格式，未知返回 PNG
     */
    fun formatFromExtension(fileName: String): OutputFormat {
        return when {
            fileName.endsWith(".webp", ignoreCase = true) -> OutputFormat.WEBP
            fileName.endsWith(".jpg", ignoreCase = true) -> OutputFormat.JPEG
            fileName.endsWith(".jpeg", ignoreCase = true) -> OutputFormat.JPEG
            fileName.endsWith(".png", ignoreCase = true) -> OutputFormat.PNG
            else -> OutputFormat.PNG
        }
    }
}
