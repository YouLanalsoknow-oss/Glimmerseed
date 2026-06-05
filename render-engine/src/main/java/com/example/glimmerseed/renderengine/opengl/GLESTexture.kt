package com.example.glimmerseed.renderengine.opengl

import android.graphics.Bitmap
import android.opengl.GLES32
import android.opengl.GLUtils
import com.example.glimmerseed.renderengine.Texture

/**
 * OpenGL ES 3.2纹理实现
 */
class GLESTexture(
    bitmap: Bitmap? = null
) : Texture {

    override var textureId: Int = GLES32.GL_NONE
        private set

    override var width: Int = 0
        private set

    override var height: Int = 0
        private set

    init {
        val textureIds = IntArray(1)
        GLES32.glGenTextures(1, textureIds, 0)
        textureId = textureIds[0]

        if (bitmap != null) {
            loadFromBitmap(bitmap)
        }
    }

    private fun loadFromBitmap(bitmap: Bitmap) {
        width = bitmap.width
        height = bitmap.height

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureId)

        // 设置纹理参数
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR_MIPMAP_LINEAR)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)

        // 上传纹理数据
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bitmap, 0)

        // 生成Mipmap
        GLES32.glGenerateMipmap(GLES32.GL_TEXTURE_2D)

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, GLES32.GL_NONE)
    }

    override fun bind(unit: Int) {
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + unit)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, textureId)
    }

    override fun unbind() {
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, GLES32.GL_NONE)
    }

    override fun destroy() {
        if (textureId != GLES32.GL_NONE) {
            GLES32.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = GLES32.GL_NONE
        }
    }
}
