package com.example.glimmerseed.editorcore.skin

import kotlinx.serialization.Serializable

/**
 * 单个骨骼的权重信息
 */
@Serializable
data class BoneWeight(
    /**
     * 骨骼ID
     */
    val boneId: Int,
    
    /**
     * 权重值（0-1）
     */
    val weight: Float
)

/**
 * 单个顶点的权重信息
 * 支持最多4个骨骼影响一个顶点
 */
@Serializable
data class VertexWeights(
    /**
     * 顶点索引
     */
    val vertexIndex: Int,
    
    /**
     * 影响此顶点的骨骼权重列表（最多4个）
     */
    val boneWeights: List<BoneWeight> = emptyList()
) {
    init {
        require(boneWeights.size <= 4) { "A vertex can be influenced by at most 4 bones" }
    }
    
    /**
     * 归一化权重，使总和为1
     */
    fun normalized(): VertexWeights {
        if (boneWeights.isEmpty()) return this
        
        val totalWeight = boneWeights.sumOf { it.weight.toDouble() }.toFloat()
        if (totalWeight <= 0f) return this
        
        val normalizedWeights = boneWeights.map { 
            BoneWeight(it.boneId, it.weight / totalWeight)
        }
        
        return copy(boneWeights = normalizedWeights)
    }
}

/**
 * 蒙皮权重数据
 * 管理整个模型的顶点权重
 */
@Serializable
data class SkinWeights(
    /**
     * 所有顶点的权重数据
     */
    val vertexWeights: List<VertexWeights> = emptyList()
) {
    /**
     * 获取指定顶点的权重
     */
    fun getVertexWeights(vertexIndex: Int): VertexWeights? {
        return vertexWeights.find { it.vertexIndex == vertexIndex }
    }
    
    /**
     * 更新指定顶点的权重
     */
    fun updateVertexWeights(vertexIndex: Int, weights: VertexWeights): SkinWeights {
        val newList = vertexWeights.toMutableList()
        val index = newList.indexOfFirst { it.vertexIndex == vertexIndex }
        if (index >= 0) {
            newList[index] = weights
        } else {
            newList.add(weights)
        }
        return copy(vertexWeights = newList)
    }
}
