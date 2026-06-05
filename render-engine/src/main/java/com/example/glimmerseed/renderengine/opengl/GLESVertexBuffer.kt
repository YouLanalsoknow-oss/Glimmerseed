package com.example.glimmerseed.renderengine.opengl

import android.opengl.GLES32
import com.example.glimmerseed.renderengine.VertexBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * OpenGL ES 3.2顶点缓冲区实现
 * 使用VBO（顶点缓冲区对象）、IBO（索引缓冲区对象）和VAO（顶点数组对象）
 */
class GLESVertexBuffer(
    vertexData: FloatArray,
    indexData: IntArray
) : VertexBuffer {

    override var vertexCount: Int = vertexData.size / 13 // 13个浮点数每个顶点
        private set

    override var indexCount: Int = indexData.size
        private set

    // VAO, VBO, IBO 句柄
    private var vaoId: Int = GLES32.GL_NONE
    private var vboId: Int = GLES32.GL_NONE
    private var iboId: Int = GLES32.GL_NONE

    init {
        // 创建并绑定VAO
        val vaoArray = IntArray(1)
        GLES32.glGenVertexArrays(1, vaoArray, 0)
        vaoId = vaoArray[0]
        GLES32.glBindVertexArray(vaoId)

        // 创建VBO
        val vboArray = IntArray(1)
        GLES32.glGenBuffers(1, vboArray, 0)
        vboId = vboArray[0]
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboId)

        // 上传顶点数据
        val vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
            .position(0) as FloatBuffer
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, vertexData.size * 4, vertexBuffer, GLES32.GL_STATIC_DRAW)

        // 设置顶点属性指针
        val stride = 13 * 4 // 每个顶点13个浮点数

        // 位置 (0-2)
        GLES32.glEnableVertexAttribArray(0)
        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false, stride, 0)

        // 纹理坐标 (3-4)
        GLES32.glEnableVertexAttribArray(1)
        GLES32.glVertexAttribPointer(1, 2, GLES32.GL_FLOAT, false, stride, 3 * 4)

        // 骨骼ID (5-8)
        GLES32.glEnableVertexAttribArray(2)
        GLES32.glVertexAttribIPointer(2, 4, GLES32.GL_INT, stride, 5 * 4)

        // 骨骼权重 (9-12)
        GLES32.glEnableVertexAttribArray(3)
        GLES32.glVertexAttribPointer(3, 4, GLES32.GL_FLOAT, false, stride, 9 * 4)

        // 创建IBO
        val iboArray = IntArray(1)
        GLES32.glGenBuffers(1, iboArray, 0)
        iboId = iboArray[0]
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, iboId)

        // 上传索引数据
        val indexBuffer = ByteBuffer.allocateDirect(indexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(indexData)
            .position(0) as IntBuffer
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, indexData.size * 4, indexBuffer, GLES32.GL_STATIC_DRAW)

        // 解绑VAO
        GLES32.glBindVertexArray(GLES32.GL_NONE)
    }

    override fun bind() {
        GLES32.glBindVertexArray(vaoId)
    }

    override fun unbind() {
        GLES32.glBindVertexArray(GLES32.GL_NONE)
    }

    override fun updateData(data: FloatArray) {
        vertexCount = data.size / 13
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboId)

        val vertexBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(data)
            .position(0) as FloatBuffer

        GLES32.glBufferSubData(GLES32.GL_ARRAY_BUFFER, 0, data.size * 4, vertexBuffer)
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, GLES32.GL_NONE)
    }

    override fun updateIndices(data: IntArray) {
        indexCount = data.size
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, iboId)

        val indexBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(data)
            .position(0) as IntBuffer

        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0, data.size * 4, indexBuffer)
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, GLES32.GL_NONE)
    }

    /**
     * 销毁缓冲区，释放GPU资源
     */
    fun destroy() {
        if (iboId != GLES32.GL_NONE) {
            GLES32.glDeleteBuffers(1, intArrayOf(iboId), 0)
            iboId = GLES32.GL_NONE
        }
        if (vboId != GLES32.GL_NONE) {
            GLES32.glDeleteBuffers(1, intArrayOf(vboId), 0)
            vboId = GLES32.GL_NONE
        }
        if (vaoId != GLES32.GL_NONE) {
            GLES32.glDeleteVertexArrays(1, intArrayOf(vaoId), 0)
            vaoId = GLES32.GL_NONE
        }
    }
}
