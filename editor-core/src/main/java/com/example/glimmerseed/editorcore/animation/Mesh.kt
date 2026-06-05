package com.example.glimmerseed.editorcore.animation

import org.joml.Vector2f
import org.joml.Vector3f

/**
 * 网格顶点数据
 * @param position 位置向量
 * @param texCoord 纹理坐标
 * @param boneIDs 影响该顶点的骨骼ID（最多4个）
 * @param boneWeights 对应的骨骼权重（和为1.0）
 */
data class Vertex(
    val position: Vector3f,
    val texCoord: Vector2f,
    val boneIDs: IntArray = intArrayOf(0, 0, 0, 0),
    val boneWeights: FloatArray = floatArrayOf(1f, 0f, 0f, 0f)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vertex

        if (position != other.position) return false
        if (texCoord != other.texCoord) return false
        if (!boneIDs.contentEquals(other.boneIDs)) return false
        if (!boneWeights.contentEquals(other.boneWeights)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + texCoord.hashCode()
        result = 31 * result + boneIDs.contentHashCode()
        result = 31 * result + boneWeights.contentHashCode()
        return result
    }
}

/**
 * 网格数据类，独立于渲染API
 * @param vertices 顶点列表
 * @param indices 索引列表
 */
data class Mesh(
    val vertices: List<Vertex>,
    val indices: List<Int>
) {
    companion object {
        /**
         * 顶点布局大小（浮点数数量）
         * 3位置 + 2纹理 + 4骨骼ID + 4权重 = 13个浮点数
         */
        const val VERTEX_SIZE = 3 + 2 + 4 + 4
    }

    /**
     * 将顶点数据转换为浮点数组，用于GPU上传
     * @return 打包后的顶点数据
     */
    fun toFloatArray(): FloatArray {
        val data = FloatArray(vertices.size * VERTEX_SIZE)
        for (i in vertices.indices) {
            val vertex = vertices[i]
            val offset = i * VERTEX_SIZE

            // 位置 (3)
            data[offset] = vertex.position.x
            data[offset + 1] = vertex.position.y
            data[offset + 2] = vertex.position.z

            // 纹理坐标 (2)
            data[offset + 3] = vertex.texCoord.x
            data[offset + 4] = vertex.texCoord.y

            // 骨骼ID (4)
            data[offset + 5] = vertex.boneIDs[0].toFloat()
            data[offset + 6] = vertex.boneIDs[1].toFloat()
            data[offset + 7] = vertex.boneIDs[2].toFloat()
            data[offset + 8] = vertex.boneIDs[3].toFloat()

            // 骨骼权重 (4)
            data[offset + 9] = vertex.boneWeights[0]
            data[offset + 10] = vertex.boneWeights[1]
            data[offset + 11] = vertex.boneWeights[2]
            data[offset + 12] = vertex.boneWeights[3]
        }
        return data
    }

    /**
     * 将索引数据转换为Int数组
     */
    fun toIndexArray(): IntArray = indices.toIntArray()
}
