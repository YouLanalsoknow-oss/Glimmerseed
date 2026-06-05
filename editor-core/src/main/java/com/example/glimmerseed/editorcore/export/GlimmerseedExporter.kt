package com.example.glimmerseed.editorcore.export

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.io.AnimationData
import com.example.glimmerseed.editorcore.io.BoneData
import com.example.glimmerseed.editorcore.io.MatrixData
import com.example.glimmerseed.editorcore.io.ProjectFile
import com.example.glimmerseed.editorcore.io.ResourceData
import com.example.glimmerseed.editorcore.io.SkeletonData
import com.example.glimmerseed.editorcore.ik.IKConstraint
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.snapshot.PoseSequence
import com.example.glimmerseed.editorcore.snapshot.PoseSnapshot
import com.example.glimmerseed.editorcore.snapshot.TransitionInterval
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

/**
 * .glimmerseed 运行时文件导出器
 *
 * 将编辑器的完整状态（骨骼/动画/网格/面板/IK约束）导出为紧凑的运行时文件。
 * 格式：gzip 压缩的 JSON，包含所有播放所需的完整数据。
 *
 * 文件结构：
 * - GlimmerseedRuntimeFile（根对象）
 *   - header: 版本、名称、导出时间
 *   - skeleton: 骨骼层级和绑定姿态
 *   - animations: 所有动画剪辑（含关键帧轨道）
 *   - mesh: 网格数据（顶点+索引+权重）
 *   - panels: 面板配置
 *   - ikConstraints: IK约束列表
 */
object GlimmerseedExporter {

    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** 当前运行时格式版本 */
    const val RUNTIME_VERSION = 1

    /**
     * 导出为 .glimmerseed 运行时文件
     *
     * @param output 目标文件（建议使用 .glimmerseed 扩展名）
     * @param name 项目名称
     * @param skeleton 骨骼数据
     * @param animations 动画剪辑列表
     * @param mesh 网格数据（可选）
     * @param panels 面板配置列表
     * @param ikConstraints IK约束列表
     * @return 导出是否成功
     */
    suspend fun export(
        output: File,
        name: String,
        skeleton: Skeleton,
        animations: List<AnimationClip> = emptyList(),
        mesh: Mesh? = null,
        panels: List<PanelData> = emptyList(),
        ikConstraints: List<IKConstraint> = emptyList(),
        poseSequence: PoseSequence? = null
    ): ExportResult = kotlinx.coroutines.withContext(Dispatchers.IO) {
        return@withContext try {
            val runtimeFile = GlimmerseedRuntimeFile(
                version = RUNTIME_VERSION,
                name = name,
                exportedAtMs = System.currentTimeMillis(),
                skeleton = SkeletonData.fromSkeleton(skeleton),
                animations = animations.map { clip ->
                    RuntimeAnimationData(
                        name = clip.name,
                        duration = clip.duration,
                        boneTracks = clip.boneKeyframes.map { (boneId, keyframes) ->
                            RuntimeBoneTrackData(
                                boneId = boneId,
                                keyframes = keyframes.map { kf ->
                                    RuntimeKeyframeData(
                                        time = kf.time,
                                        positionX = kf.translation.x,
                                        positionY = kf.translation.y,
                                        positionZ = kf.translation.z,
                                        rotationX = kf.rotation.x,
                                        rotationY = kf.rotation.y,
                                        rotationZ = kf.rotation.z,
                                        scaleX = kf.scale.x,
                                        scaleY = kf.scale.y,
                                        scaleZ = kf.scale.z
                                    )
                                }
                            )
                        }
                    )
                },
                mesh = mesh?.let { m ->
                    RuntimeMeshData(
                        vertices = m.vertices.map { v ->
                            RuntimeVertexData(
                                posX = v.position.x, posY = v.position.y, posZ = v.position.z,
                                texU = v.texCoord.x, texV = v.texCoord.y,
                                boneIds = v.boneIDs.toList(),
                                boneWeights = v.boneWeights.toList()
                            )
                        },
                        indices = m.indices.toIntArray().toList()
                    )
                },
                panels = panels,
                ikConstraints = ikConstraints,
                poseSequence = poseSequence?.let { seq ->
                    RuntimePoseSequence(
                        snapshots = seq.snapshots.map { snap ->
                            RuntimePoseSnapshot(
                                id = snap.id,
                                timestamp = snap.timestamp,
                                label = snap.label,
                                boneTransforms = snap.boneTransforms.mapKeys { it.key.toString() }
                            )
                        },
                        transitions = seq.transitions.values.map { trans ->
                            RuntimeTransitionInterval(
                                fromSnapshotId = trans.fromSnapshotId,
                                toSnapshotId = trans.toSnapshotId,
                                mode = trans.mode.name,
                                duration = trans.duration
                            )
                        },
                        loop = seq.loop
                    )
                }
            )

            // gzip 压缩输出以减小体积
            FileOutputStream(output).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    gzos.bufferedWriter().use { writer ->
                        writer.write(json.encodeToString(runtimeFile))
                    }
                }
            }

            ExportResult(true, exportedFiles = listOf(output))
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult(false, errorMessage = e.message ?: "Unknown error")
        }
    }

    /**
     * 从 .glimmerseed 运行时文件加载
     *
     * @param file .glimmerseed 文件
     * @return 加载结果
     */
    suspend fun load(file: File): RuntimeLoadResult =
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            return@withContext try {
                val jsonString = file.inputStream().use { fis ->
                    if (file.name.endsWith(".glimmerseed")) {
                        // 尝试 gzip 解压
                        try {
                            java.util.zip.GZIPInputStream(fis).bufferedReader().readText()
                        } catch (_: Exception) {
                            fis.bufferedReader().readText()
                        }
                    } else {
                        fis.bufferedReader().readText()
                    }
                }

                val runtimeFile = json.decodeFromString<GlimmerseedRuntimeFile>(jsonString)

                RuntimeLoadResult.Success(runtimeFile)
            } catch (e: Exception) {
                e.printStackTrace()
                RuntimeLoadResult.Error(e.message ?: "Failed to load .glimmerseed file")
            }
        }
}

// ==================== 运行时数据结构 ====================

@Serializable
data class GlimmerseedRuntimeFile(
    val version: Int = GlimmerseedExporter.RUNTIME_VERSION,
    val name: String,
    val exportedAtMs: Long,
    val skeleton: SkeletonData,
    val animations: List<RuntimeAnimationData> = emptyList(),
    val mesh: RuntimeMeshData? = null,
    val panels: List<PanelData> = emptyList(),
    val resources: List<ResourceData> = emptyList(),
    val ikConstraints: List<IKConstraint> = emptyList(),
    /** 姿势快照序列（PoseSnapshot 系统） */
    val poseSequence: RuntimePoseSequence? = null
)

/**
 * 运行时姿势序列（可序列化版本）
 */
@Serializable
data class RuntimePoseSequence(
    val snapshots: List<RuntimePoseSnapshot> = emptyList(),
    val transitions: List<RuntimeTransitionInterval> = emptyList(),
    val loop: Boolean = true
)

@Serializable
data class RuntimePoseSnapshot(
    val id: String,
    val timestamp: Float,
    val label: String = "",
    val boneTransforms: Map<String, MatrixData>
)

@Serializable
data class RuntimeTransitionInterval(
    val fromSnapshotId: String,
    val toSnapshotId: String,
    val mode: String, // "STEP" | "LINEAR" | "BEZIER" | "EASE_IN_OUT"
    val duration: Float = 0f
)

@Serializable
data class RuntimeAnimationData(
    val name: String,
    val duration: Float,
    val boneTracks: List<RuntimeBoneTrackData> = emptyList()
)

@Serializable
data class RuntimeBoneTrackData(
    val boneId: Int,
    val keyframes: List<RuntimeKeyframeData> = emptyList()
)

@Serializable
data class RuntimeKeyframeData(
    val time: Float,
    val positionX: Float = 0f, val positionY: Float = 0f, val positionZ: Float = 0f,
    val rotationX: Float = 0f, val rotationY: Float = 0f, val rotationZ: Float = 0f,
    val scaleX: Float = 1f, val scaleY: Float = 1f, val scaleZ: Float = 1f
)

@Serializable
data class RuntimeMeshData(
    val vertices: List<RuntimeVertexData>,
    val indices: List<Int>
)

@Serializable
data class RuntimeVertexData(
    val posX: Float, val posY: Float, val posZ: Float,
    val texU: Float, val texV: Float,
    val boneIds: List<Int>,
    val boneWeights: List<Float>
)

// ==================== 结果类型 ====================

sealed class RuntimeLoadResult {
    data class Success(val data: GlimmerseedRuntimeFile) : RuntimeLoadResult()
    data class Error(val message: String) : RuntimeLoadResult()
}

// ==================== 反序列化扩展方法 ====================

/**
 * 将运行时动画数据恢复为 AnimationClip
 */
fun RuntimeAnimationData.toAnimationClip(skeleton: com.example.glimmerseed.editorcore.animation.Skeleton): com.example.glimmerseed.editorcore.animation.AnimationClip {
    val boneKeyframes = mutableMapOf<Int, MutableList<com.example.glimmerseed.editorcore.animation.Keyframe>>()

    for (track in boneTracks) {
        val keyframes = mutableListOf<com.example.glimmerseed.editorcore.animation.Keyframe>()
        for (kf in track.keyframes) {
            keyframes.add(
                com.example.glimmerseed.editorcore.animation.Keyframe(
                    time = kf.time,
                    translation = org.joml.Vector3f(kf.positionX, kf.positionY, kf.positionZ),
                    rotation = org.joml.Quaternionf().rotateXYZ(kf.rotationX, kf.rotationY, kf.rotationZ),
                    scale = org.joml.Vector3f(kf.scaleX, kf.scaleY, kf.scaleZ)
                )
            )
        }
        boneKeyframes[track.boneId] = keyframes
    }

    return com.example.glimmerseed.editorcore.animation.AnimationClip(
        name = name,
        duration = duration,
        boneKeyframes = boneKeyframes
    )
}

/**
 * 将运行时网格数据恢复为 Mesh
 */
fun RuntimeMeshData.toMesh(): com.example.glimmerseed.editorcore.animation.Mesh {
    return com.example.glimmerseed.editorcore.animation.Mesh(
        vertices = vertices.map { v ->
            com.example.glimmerseed.editorcore.animation.Vertex(
                position = org.joml.Vector3f(v.posX, v.posY, v.posZ),
                texCoord = org.joml.Vector2f(v.texU, v.texV),
                boneIDs = v.boneIds.toIntArray(),
                boneWeights = v.boneWeights.toFloatArray()
            )
        },
        indices = indices
    )
}

/**
 * 将运行时姿势序列恢复为 PoseSequence（可注入 SnapshotManager）
 */
fun RuntimePoseSequence.toPoseSequence(): com.example.glimmerseed.editorcore.snapshot.PoseSequence {
    val poseSnapshots = snapshots.map { rsnap ->
        com.example.glimmerseed.editorcore.snapshot.PoseSnapshot(
            id = rsnap.id,
            timestamp = rsnap.timestamp,
            label = rsnap.label,
            boneTransforms = rsnap.boneTransforms.mapKeys { it.key.toInt() }
        )
    }

    val transitionMap = mutableMapOf<String, com.example.glimmerseed.editorcore.snapshot.TransitionInterval>()
    for (rt in transitions) {
        val mode = try {
            com.example.glimmerseed.editorcore.snapshot.InterpolationMode.valueOf(rt.mode)
        } catch (_: Exception) {
            com.example.glimmerseed.editorcore.snapshot.InterpolationMode.LINEAR
        }
        transitionMap["${rt.fromSnapshotId}->${rt.toSnapshotId}"] =
            com.example.glimmerseed.editorcore.snapshot.TransitionInterval(
                fromSnapshotId = rt.fromSnapshotId,
                toSnapshotId = rt.toSnapshotId,
                mode = mode,
                duration = rt.duration
            )
    }

    return com.example.glimmerseed.editorcore.snapshot.PoseSequence(
        snapshots = poseSnapshots.sortedBy { it.timestamp },
        transitions = transitionMap,
        loop = loop
    )
}
