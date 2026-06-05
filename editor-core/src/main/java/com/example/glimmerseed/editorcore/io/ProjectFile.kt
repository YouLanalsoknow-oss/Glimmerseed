package com.example.glimmerseed.editorcore.io

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.ik.IKConstraint
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.stage.StageData
import kotlinx.serialization.Serializable
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * 项目文件格式
 *
 * 版本历史:
 *   v1 - 初始版本: skeleton + animations + resources
 *   v2 - + panels + stage（v3号段被跳过，新增字段直接加入v2）
 *   v3 - + ikConstraints（当前版本）
 */
@Serializable
data class ProjectFile(
    val version: Int = 3,
    val name: String = "Untitled",
    val skeleton: SkeletonData = SkeletonData(),
    val animations: List<AnimationData> = emptyList(),
    val resources: List<ResourceData> = emptyList(),
    val panels: List<PanelData> = emptyList(),
    val stage: StageData? = null,
    val ikConstraints: List<IKConstraint> = emptyList()
) {
    companion object {
        /**
     * v1→v2 迁移函数
     * v2 新增 panels 和 stage 字段
     * 历史说明：v3号段曾被预留但实际跳过，panels/stage字段直接加入了v2
     */
    fun migrateV1ToV2(projectFile: ProjectFile): ProjectFile {
        return projectFile.copy(
            version = 2,
            panels = emptyList(),
            stage = null
        )
    }

    /**
     * v2→v3 迁移函数
     * v3 新增 ikConstraints 字段，默认值为 emptyList()
     * Kotlinx 序列化在 ignoreUnknownKeys=true 时，
     * v2 文件加载后 ikConstraints 自动为 emptyList()
     */
    fun migrateV2ToV3(projectFile: ProjectFile): ProjectFile {
        return projectFile.copy(version = 3)
    }

    /**
     * 统一版本迁移入口
     * 根据当前版本自动执行所有必要的迁移步骤
     */
    fun migrateToLatest(projectFile: ProjectFile): ProjectFile {
        var result = projectFile
        when (result.version) {
            1 -> {
                result = migrateV1ToV2(result)
                result = migrateV2ToV3(result)
            }
            2 -> {
                result = migrateV2ToV3(result)
            }
            3 -> {
                // 当前最新版本，无需迁移
            }
        }
        return result
    }
}
}

/**
 * 骨骼数据（可序列化）
 */
@Serializable
data class SkeletonData(
    val bones: List<BoneData> = emptyList()
) {
    fun toSkeleton(): Skeleton {
        val boneMap = mutableMapOf<Int, Bone>()
        
        bones.forEach { boneData ->
            val bone = Bone(
                id = boneData.id,
                name = boneData.name,
                parentId = boneData.parentId,
                localTransform = Matrix4f().set(boneData.localTransform.toMatrix4f())
            )
            boneMap[bone.id] = bone
        }
        
        val skeleton = Skeleton(bones = boneMap.values.toList())
        skeleton.updateAllWorldTransforms()
        
        return skeleton
    }
    
    companion object {
        fun fromSkeleton(skeleton: Skeleton): SkeletonData {
            return SkeletonData(
                bones = skeleton.bones.map { bone ->
                    BoneData(
                        id = bone.id,
                        name = bone.name,
                        parentId = bone.parentId,
                        localTransform = MatrixData.fromMatrix4f(bone.localTransform)
                    )
                }
            )
        }
    }
}

/**
 * 单个骨骼数据
 */
@Serializable
data class BoneData(
    val id: Int,
    val name: String,
    val parentId: Int? = null,
    val localTransform: MatrixData
)

/**
 * 矩阵数据
 */
@Serializable
data class MatrixData(
    val m00: Float, val m01: Float, val m02: Float, val m03: Float,
    val m10: Float, val m11: Float, val m12: Float, val m13: Float,
    val m20: Float, val m21: Float, val m22: Float, val m23: Float,
    val m30: Float, val m31: Float, val m32: Float, val m33: Float
) {
    fun toMatrix4f(): Matrix4f {
        return Matrix4f(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        )
    }
    
    companion object {
        fun fromMatrix4f(matrix: Matrix4f): MatrixData {
            return MatrixData(
                m00 = matrix.m00(), m01 = matrix.m01(), m02 = matrix.m02(), m03 = matrix.m03(),
                m10 = matrix.m10(), m11 = matrix.m11(), m12 = matrix.m12(), m13 = matrix.m13(),
                m20 = matrix.m20(), m21 = matrix.m21(), m22 = matrix.m22(), m23 = matrix.m23(),
                m30 = matrix.m30(), m31 = matrix.m31(), m32 = matrix.m32(), m33 = matrix.m33()
            )
        }
    }
}

/**
 * 动画数据
 */
@Serializable
data class AnimationData(
    val name: String,
    val duration: Float,
    val boneTracks: List<BoneTrackData> = emptyList()
)

/**
 * 骨骼轨迹数据
 */
@Serializable
data class BoneTrackData(
    val boneId: Int,
    val keyframes: List<KeyframeData> = emptyList()
)

/**
 * 关键帧数据
 */
@Serializable
data class KeyframeData(
    val time: Float,
    val position: Vector3Data = Vector3Data(0f, 0f, 0f),
    val rotation: Vector3Data = Vector3Data(0f, 0f, 0f),
    val scale: Vector3Data = Vector3Data(1f, 1f, 1f)
)

/**
 * 向量数据
 */
@Serializable
data class Vector3Data(
    val x: Float,
    val y: Float,
    val z: Float
) {
    fun toVector3f(): Vector3f = Vector3f(x, y, z)
    
    companion object {
        fun fromVector3f(v: Vector3f): Vector3Data = Vector3Data(v.x, v.y, v.z)
    }
}

/**
 * 资源数据
 */
@Serializable
data class ResourceData(
    val id: String,
    val type: String,
    val path: String,
    val name: String
)