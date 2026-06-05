package com.example.glimmerseed.editorcore.data

import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * 动画数据快照
 * 包含动画的完整只读状态，用于主编辑器与悬浮窗之间的同步
 */
data class AnimationDataSnapshot(
    /** 时间戳 */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** 当前播放时间 */
    val currentTime: Float = 0f,
    
    /** 是否正在播放 */
    val isPlaying: Boolean = false,
    
    /** 骨骼快照列表 */
    val boneSnapshots: List<BoneSnapshot> = emptyList(),
    
    /** 网格快照列表 */
    val meshSnapshots: List<MeshSnapshot> = emptyList(),
    
    /** 纹理快照（可选） */
    val textureSnapshot: TextureSnapshot? = null,
    
    /** 视口变换 */
    val viewportTransform: ViewportTransform = ViewportTransform()
) {
    /**
     * 骨骼快照
     */
    data class BoneSnapshot(
        /** 骨骼ID */
        val boneId: Int,
        
        /** 骨骼名称 */
        val boneName: String,
        
        /** 局部变换矩阵 */
        val localTransform: Matrix4fSnapshot,
        
        /** 世界变换矩阵 */
        val worldTransform: Matrix4fSnapshot,
        
        /** 父骨骼ID */
        val parentId: Int?
    )
    
    /**
     * 网格快照
     */
    data class MeshSnapshot(
        /** 顶点数据 */
        val vertices: List<VertexSnapshot>,
        
        /** 索引数据 */
        val indices: List<Int>
    )
    
    /**
     * 顶点快照
     */
    data class VertexSnapshot(
        /** 位置向量 */
        val position: Vector3f,
        
        /** 纹理坐标 */
        val texCoord: Vector2f,
        
        /** 影响该顶点的骨骼ID（最多4个） */
        val boneIDs: IntArray,
        
        /** 对应的骨骼权重（和为1.0） */
        val boneWeights: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            
            other as VertexSnapshot
            
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
     * 纹理快照
     */
    data class TextureSnapshot(
        /** 纹理文件路径 */
        val filePath: String?,
        
        /** 纹理宽度 */
        val width: Int,
        
        /** 纹理高度 */
        val height: Int
    )
    
    /**
     * 矩阵4x4快照
     */
    data class Matrix4fSnapshot(
        val m00: Float, val m01: Float, val m02: Float, val m03: Float,
        val m10: Float, val m11: Float, val m12: Float, val m13: Float,
        val m20: Float, val m21: Float, val m22: Float, val m23: Float,
        val m30: Float, val m31: Float, val m32: Float, val m33: Float
    ) {
        companion object {
            /**
             * 从Matrix4f创建快照
             */
            fun fromMatrix4f(matrix: Matrix4f): Matrix4fSnapshot {
                return Matrix4fSnapshot(
                    m00 = matrix.m00(), m01 = matrix.m01(), m02 = matrix.m02(), m03 = matrix.m03(),
                    m10 = matrix.m10(), m11 = matrix.m11(), m12 = matrix.m12(), m13 = matrix.m13(),
                    m20 = matrix.m20(), m21 = matrix.m21(), m22 = matrix.m22(), m23 = matrix.m23(),
                    m30 = matrix.m30(), m31 = matrix.m31(), m32 = matrix.m32(), m33 = matrix.m33()
                )
            }
        }
        
        /**
         * 转换为Matrix4f
         */
        fun toMatrix4f(): Matrix4f {
            return Matrix4f(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
            )
        }
    }
    
    /**
     * 视口变换
     */
    data class ViewportTransform(
        val translationX: Float = 0f,
        val translationY: Float = 0f,
        val scale: Float = 1f
    )
}

/**
 * 从Skeleton创建快照
 */
fun createAnimationDataSnapshot(
    skeleton: Skeleton,
    mesh: Mesh?,
    currentTime: Float,
    isPlaying: Boolean,
    viewportTranslationX: Float,
    viewportTranslationY: Float,
    viewportScale: Float
): AnimationDataSnapshot {
    // 更新所有骨骼的世界变换
    skeleton.updateAllWorldTransforms()
    
    val boneSnapshots = skeleton.bones.map { bone ->
        // 获取父骨骼
        val parentBone = bone.parentId?.let { skeleton.getBoneById(it) }
        
        // 计算世界变换
        val worldTransform = skeleton.getBoneWorldTransform(bone.id)
        
        AnimationDataSnapshot.BoneSnapshot(
            boneId = bone.id,
            boneName = bone.name,
            localTransform = AnimationDataSnapshot.Matrix4fSnapshot.fromMatrix4f(bone.localTransform),
            worldTransform = AnimationDataSnapshot.Matrix4fSnapshot.fromMatrix4f(worldTransform),
            parentId = bone.parentId
        )
    }
    
    // 转换网格数据
    val meshSnapshots = mesh?.let { listOf(meshToSnapshot(it)) } ?: emptyList()
    
    return AnimationDataSnapshot(
        currentTime = currentTime,
        isPlaying = isPlaying,
        boneSnapshots = boneSnapshots,
        meshSnapshots = meshSnapshots,
        textureSnapshot = null, // 暂时不处理纹理
        viewportTransform = AnimationDataSnapshot.ViewportTransform(
            translationX = viewportTranslationX,
            translationY = viewportTranslationY,
            scale = viewportScale
        )
    )
}

/**
 * 把 Mesh 转换为 MeshSnapshot
 */
private fun meshToSnapshot(mesh: Mesh): AnimationDataSnapshot.MeshSnapshot {
    val vertexSnapshots = mesh.vertices.map { vertex ->
        AnimationDataSnapshot.VertexSnapshot(
            position = Vector3f(vertex.position),
            texCoord = Vector2f(vertex.texCoord),
            boneIDs = vertex.boneIDs.copyOf(),
            boneWeights = vertex.boneWeights.copyOf()
        )
    }
    
    return AnimationDataSnapshot.MeshSnapshot(
        vertices = vertexSnapshots,
        indices = mesh.indices
    )
}
