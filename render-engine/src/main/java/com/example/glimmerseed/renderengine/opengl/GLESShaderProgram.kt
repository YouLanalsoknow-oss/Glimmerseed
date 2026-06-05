package com.example.glimmerseed.renderengine.opengl

import android.opengl.GLES32
import com.example.glimmerseed.renderengine.ShaderProgram
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * OpenGL ES 3.2着色器程序实现
 * 封装顶点着色器和片段着色器的编译、链接和使用
 * 支持Uniform变量和Uniform Buffer Object
 */
class GLESShaderProgram(
    vertexShaderCode: String,
    fragmentShaderCode: String
) : ShaderProgram {

    override var programId: Int = GLES32.GL_NONE
        private set

    private val uniformLocationCache = mutableMapOf<String, Int>()

    init {
        // 编译着色器
        val vertexShaderId = compileShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShaderId = compileShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 创建程序并链接着色器
        programId = GLES32.glCreateProgram()
        GLES32.glAttachShader(programId, vertexShaderId)
        GLES32.glAttachShader(programId, fragmentShaderId)
        GLES32.glLinkProgram(programId)

        // 验证链接结果
        val linkStatus = IntArray(1)
        GLES32.glGetProgramiv(programId, GLES32.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == GLES32.GL_FALSE) {
            val errorLog = GLES32.glGetProgramInfoLog(programId)
            GLES32.glDeleteProgram(programId)
            GLES32.glDeleteShader(vertexShaderId)
            GLES32.glDeleteShader(fragmentShaderId)
            throw RuntimeException("Shader program link error: $errorLog")
        }

        // 删除临时着色器
        GLES32.glDeleteShader(vertexShaderId)
        GLES32.glDeleteShader(fragmentShaderId)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shaderId = GLES32.glCreateShader(type)
        GLES32.glShaderSource(shaderId, source)
        GLES32.glCompileShader(shaderId)

        val compileStatus = IntArray(1)
        GLES32.glGetShaderiv(shaderId, GLES32.GL_COMPILE_STATUS, compileStatus, 0)

        if (compileStatus[0] == GLES32.GL_FALSE) {
            val errorLog = GLES32.glGetShaderInfoLog(shaderId)
            GLES32.glDeleteShader(shaderId)
            val shaderType = if (type == GLES32.GL_VERTEX_SHADER) "Vertex" else "Fragment"
            throw RuntimeException("$shaderType shader compile error: $errorLog")
        }

        return shaderId
    }

    override fun bind() {
        GLES32.glUseProgram(programId)
    }

    override fun unbind() {
        GLES32.glUseProgram(GLES32.GL_NONE)
    }

    private fun getUniformLocation(name: String): Int {
        return uniformLocationCache.getOrPut(name) {
            GLES32.glGetUniformLocation(programId, name)
        }
    }

    override fun setUniform(name: String, value: Float) {
        val location = getUniformLocation(name)
        if (location >= 0) {
            GLES32.glUniform1f(location, value)
        }
    }

    override fun setUniform(name: String, value: Int) {
        val location = getUniformLocation(name)
        if (location >= 0) {
            GLES32.glUniform1i(location, value)
        }
    }

    override fun setUniform(name: String, value: FloatArray) {
        val location = getUniformLocation(name)
        if (location >= 0) {
            when (value.size) {
                2 -> GLES32.glUniform2fv(location, 1, value, 0)
                3 -> GLES32.glUniform3fv(location, 1, value, 0)
                4 -> GLES32.glUniform4fv(location, 1, value, 0)
                else -> GLES32.glUniform4fv(location, 1, value, 0)
            }
        }
    }

    override fun setUniform(name: String, value: IntArray) {
        val location = getUniformLocation(name)
        if (location >= 0) {
            when (value.size) {
                2 -> GLES32.glUniform2iv(location, 1, value, 0)
                3 -> GLES32.glUniform3iv(location, 1, value, 0)
                4 -> GLES32.glUniform4iv(location, 1, value, 0)
            }
        }
    }

    override fun setUniformMatrix4(name: String, matrix: FloatArray) {
        val location = getUniformLocation(name)
        if (location >= 0) {
            GLES32.glUniformMatrix4fv(location, 1, false, matrix, 0)
        }
    }

    /**
     * 获取Uniform Buffer Binding Point
     */
    fun getUniformBlockBinding(name: String): Int {
        return GLES32.glGetUniformBlockIndex(programId, name)
    }

    /**
     * 绑定Uniform Buffer到指定的Binding Point
     */
    fun bindUniformBlock(blockName: String, bindingPoint: Int) {
        val index = getUniformBlockBinding(blockName)
        if (index != GLES32.GL_INVALID_INDEX) {
            GLES32.glUniformBlockBinding(programId, index, bindingPoint)
        }
    }

    /**
     * 销毁着色器程序
     */
    fun destroy() {
        if (programId != GLES32.GL_NONE) {
            GLES32.glDeleteProgram(programId)
            programId = GLES32.GL_NONE
        }
        uniformLocationCache.clear()
    }
}
