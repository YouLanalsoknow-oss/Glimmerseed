package com.example.glimmerseed.renderengine.opengl

import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 骨骼矩阵Uniform Buffer Object
 * 使用std140布局，最多支持100个骨骼矩阵
 */
class BoneMatricesUBO(
    private val maxBones: Int = 100
) {
    companion object {
        private const val BONE_MATRIX_SIZE = 16 // 每个矩阵16个浮点数
        private const val BINDING_POINT = 0
    }

    private var uboId: Int = GLES32.GL_NONE
    private val matrixBuffer = ByteBuffer.allocateDirect(maxBones * BONE_MATRIX_SIZE * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    private val boneMatrixArray = FloatArray(maxBones * BONE_MATRIX_SIZE)

    init {
        // 创建UBO
        val uboIds = IntArray(1)
        GLES32.glGenBuffers(1, uboIds, 0)
        uboId = uboIds[0]

        // 分配UBO内存
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, uboId)
        GLES32.glBufferData(
            GLES32.GL_UNIFORM_BUFFER,
            maxBones * BONE_MATRIX_SIZE * 4,
            null,
            GLES32.GL_DYNAMIC_DRAW
        )
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, GLES32.GL_NONE)

        // 绑定到指定的binding point
        bind()
    }

    /**
     * 更新骨骼矩阵
     * @param boneIndex 骨骼索引
     * @param matrix 4x4矩阵数组（16个浮点数）
     */
    fun updateBoneMatrix(boneIndex: Int, matrix: FloatArray) {
        if (boneIndex >= maxBones) return

        val offset = boneIndex * BONE_MATRIX_SIZE
        System.arraycopy(matrix, 0, boneMatrixArray, offset, BONE_MATRIX_SIZE)
    }

    /**
     * 批量更新所有骨骼矩阵
     * @param matrices 矩阵数组
     */
    fun updateBones(matrices: List<FloatArray>) {
        for ((index, matrix) in matrices.withIndex()) {
            if (index < maxBones) {
                updateBoneMatrix(index, matrix)
            }
        }
    }

    /**
     * 上传矩阵数据到GPU
     */
    fun uploadToGPU() {
        matrixBuffer.position(0)
        matrixBuffer.put(boneMatrixArray)
        matrixBuffer.position(0)

        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, uboId)
        GLES32.glBufferSubData(GLES32.GL_UNIFORM_BUFFER, 0, boneMatrixArray.size * 4, matrixBuffer)
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, GLES32.GL_NONE)
    }

    /**
     * 绑定UBO
     */
    fun bind() {
        GLES32.glBindBufferBase(GLES32.GL_UNIFORM_BUFFER, BINDING_POINT, uboId)
    }

    /**
     * 解绑UBO
     */
    fun unbind() {
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER, GLES32.GL_NONE)
    }

    /**
     * 销毁UBO
     */
    fun destroy() {
        if (uboId != GLES32.GL_NONE) {
            GLES32.glDeleteBuffers(1, intArrayOf(uboId), 0)
            uboId = GLES32.GL_NONE
        }
    }
}
