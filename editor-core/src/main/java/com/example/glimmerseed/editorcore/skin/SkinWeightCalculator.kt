package com.example.glimmerseed.editorcore.skin

import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Skeleton
import org.joml.Vector3f
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * 权重计算器
 * 提供自动计算骨骼权重的方法
 */
object SkinWeightCalculator {
    
    /**
     * 基于距离的自动权重计算
     * @param vertices 顶点位置列表
     * @param skeleton 骨骼
     * @param maxInfluenceBones 每个顶点最多受几个骨骼影响
     * @param falloffDistance 权重衰减距离
     */
    fun calculateDistanceBasedWeights(
        vertices: List<Vector3f>,
        skeleton: Skeleton,
        maxInfluenceBones: Int = 4,
        falloffDistance: Float = 100f
    ): SkinWeights {
        val vertexWeights = mutableListOf<VertexWeights>()
        
        for ((vertexIndex, vertex) in vertices.withIndex()) {
            // 计算每个骨骼到该顶点的距离
            val boneDistances = skeleton.bones.map { bone ->
                val bonePos = getBonePosition(bone, skeleton)
                val distance = vertex.distance(bonePos)
                bone to distance
            }
            
            // 选择最近的几个骨骼
            val sortedBones = boneDistances.sortedBy { it.second }
                .take(maxInfluenceBones)
            
            // 计算权重（基于距离的高斯衰减）
            val boneWeights = sortedBones.map { (bone, distance) ->
                val weight = if (distance < falloffDistance) {
                    calculateGaussianWeight(distance, falloffDistance)
                } else {
                    0f
                }
                BoneWeight(bone.id, weight)
            }.filter { it.weight > 0.001f }
            
            // 创建顶点权重
            val vw = VertexWeights(vertexIndex, boneWeights).normalized()
            vertexWeights.add(vw)
        }
        
        return SkinWeights(vertexWeights)
    }
    
    /**
     * 计算高斯权重
     */
    private fun calculateGaussianWeight(distance: Float, falloff: Float): Float {
        val normalizedDist = distance / falloff
        return exp(-(normalizedDist * normalizedDist) * 2.0f).toFloat()
    }
    
    /**
     * 获取骨骼的世界位置
     */
    private fun getBonePosition(bone: Bone, skeleton: Skeleton): Vector3f {
        val parentTransform = bone.parentId?.let { parentId ->
            val parent = skeleton.getBoneById(parentId)
            parent?.let { 
                val worldPos = Vector3f()
                skeleton.getBoneWorldTransform(parentId).getTranslation(worldPos)
                worldPos
            }
        } ?: Vector3f()
        
        val localPos = Vector3f()
        bone.localTransform.getTranslation(localPos)
        
        return Vector3f(parentTransform).add(localPos)
    }
    
    /**
     * 平滑权重（热扩散算法）
     */
    fun smoothWeights(
        weights: SkinWeights,
        vertices: List<Vector3f>,
        iterations: Int = 3,
        smoothFactor: Float = 0.5f
    ): SkinWeights {
        var currentWeights = weights
        
        repeat(iterations) {
            currentWeights = smoothWeightsOnce(currentWeights, vertices, smoothFactor)
        }
        
        return currentWeights
    }
    
    private fun smoothWeightsOnce(
        weights: SkinWeights,
        vertices: List<Vector3f>,
        smoothFactor: Float
    ): SkinWeights {
        // 简单实现：每个顶点取周围顶点的平均值
        // 实际项目中需要基于邻接关系
        return weights
    }
}
