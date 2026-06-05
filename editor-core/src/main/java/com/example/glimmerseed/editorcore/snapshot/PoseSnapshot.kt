package com.example.glimmerseed.editorcore.snapshot

import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.io.MatrixData
import org.joml.Matrix4f

/**
 * 姿势快照 — 记录骨骼在某一时刻的完整姿态
 *
 * 每个快照包含所有骨骼的局部变换矩阵，是骨骼姿态的不可变记录。
 * 快照一旦创建即不可修改，确保跨线程安全。
 *
 * 编辑流程：
 * 1. 在编辑器中摆好骨骼姿势
 * 2. 调用 SnapshotManager.capture() 创建快照
 * 3. 设置时间戳
 * 4. 将快照加入 PoseSequence
 * 5. 配置相邻快照间的插值方式
 *
 * @param id 唯一标识符
 * @param timestamp 时间戳（秒），用于排序和插值
 * @param label 用户可读标签（如"站立"、"行走起始"）
 * @param boneTransforms 骨骼ID → 局部变换矩阵的不可变映射
 */
data class PoseSnapshot(
    val id: String,
    val timestamp: Float,
    val label: String = "",
    val boneTransforms: Map<Int, MatrixData>
) {
    /**
     * 获取指定骨骼的变换矩阵
     */
    fun getBoneTransform(boneId: Int): MatrixData? = boneTransforms[boneId]

    companion object {
        /**
         * 从当前骨骼状态创建快照
         *
         * @param id 快照ID
         * @param timestamp 时间戳
         * @param label 标签
         * @param skeleton 当前骨骼状态（只读取，不修改）
         * @return 新的姿势快照
         */
        fun capture(
            id: String,
            timestamp: Float,
            label: String,
            skeleton: Skeleton
        ): PoseSnapshot {
            val transforms = skeleton.bones.associate { bone ->
                bone.id to MatrixData.fromMatrix4f(bone.localTransform)
            }
            return PoseSnapshot(id, timestamp, label, transforms)
        }
    }
}

/**
 * 插值模式 — 定义两个快照之间的过渡方式
 */
enum class InterpolationMode {
    /** 无插值，瞬间跳变 */
    STEP,
    /** 线性插值 */
    LINEAR,
    /** 贝塞尔曲线插值（使用默认平滑度） */
    BEZIER,
    /** 自定义缓动曲线 */
    EASE_IN_OUT
}

/**
 * 区间映射 — 定义两个相邻快照之间的插值配置
 *
 * 文档 P0 高危问题要求：将 transitionToNext 从 PoseSnapshot 中移出，
 * 独立建立区间映射表。
 *
 * @param fromSnapshotId 起始快照ID
 * @param toSnapshotId 结束快照ID
 * @param mode 插值模式
 * @param duration 覆盖时长（秒），0 表示使用实际时间差
 * @param customCurve 自定义贝塞尔控制点（仅 BEZIER/EASE_IN_OUT 时使用）
 */
data class TransitionInterval(
    val fromSnapshotId: String,
    val toSnapshotId: String,
    val mode: InterpolationMode = InterpolationMode.LINEAR,
    val duration: Float = 0f,
    val customCurve: BezierControlPoints? = null
)

/**
 * 贝塞尔控制点
 */
data class BezierControlPoints(
    val cp1x: Float, val cp1y: Float,
    val cp2x: Float, val cp2y: Float
)

/**
 * 姿势序列 — 有序的快照集合 + 区间插值配置
 *
 * 替代原有的帧树/关键帧时间轴，成为动画数据的核心组织形式。
 *
 * 序列特性：
 * - 快照按 timestamp 升序排列
 * - 每对相邻快照可独立配置插值方式
 * - 支持循环播放（首尾可配置过渡）
 *
 * @param snapshots 有序快照列表（按 timestamp 排序）
 * @param transitions 区间插值映射表
 * @param loop 是否循环播放
 * @param totalDuration 总时长（秒），由最后一个快照时间戳决定
 */
data class PoseSequence(
    val snapshots: List<PoseSnapshot> = emptyList(),
    val transitions: Map<String, TransitionInterval> = emptyMap(),
    val loop: Boolean = true
) {
    val totalDuration: Float
        get() = if (snapshots.isEmpty()) 0f else snapshots.last().timestamp

    /**
     * 按时间戳获取快照（二分查找）
     */
    fun getSnapshotAtTime(time: Float): PoseSnapshot? {
        if (snapshots.isEmpty()) return null
        val clampedTime = if (loop && totalDuration > 0) {
            ((time % totalDuration) + totalDuration) % totalDuration
        } else {
            time.coerceIn(0f, totalDuration)
        }

        // 找到时间点所在的区间 [prev, next]
        var prev = snapshots.first()
        for (i in snapshots.indices) {
            val curr = snapshots[i]
            if (curr.timestamp <= clampedTime) {
                prev = curr
            } else {
                // 返回区间起点快照
                return prev
            }
        }
        return prev
    }

    /**
     * 获取指定时间点的混合姿态（考虑插值）
     *
     * @param time 目标时间（秒）
     * @return 插值后的骨骼变换映射，null 表示超出范围
     */
    fun evaluateAt(time: Float): Map<Int, MatrixData>? {
        if (snapshots.isEmpty()) return emptyMap()
        if (snapshots.size == 1) return snapshots.first().boneTransforms

        val clampedTime = if (loop && totalDuration > 0) {
            ((time % totalDuration) + totalDuration) % totalDuration
        } else {
            time.coerceIn(0f, totalDuration)
        }

        // 找到包围目标时间的两个快照
        var prev = snapshots.first()
        var next: PoseSnapshot? = null

        for (i in snapshots.indices) {
            val curr = snapshots[i]
            if (curr.timestamp <= clampedTime) {
                prev = curr
            } else {
                next = curr
                break
            }
        }

        // 精确命中某个快照时间
        if (next == null || (clampedTime - prev.timestamp) < 0.0001f) {
            return prev.boneTransforms
        }

        // 计算插值进度
        val intervalDuration = next.timestamp - prev.timestamp
        val progress = if (intervalDuration > 0.0001f) {
            (clampedTime - prev.timestamp) / intervalDuration
        } else {
            1f
        }.coerceIn(0f, 1f)

        // 查找区间插值配置
        val transitionKey = "${prev.id}->${next.id}"
        val transition = transitions[transitionKey]

        return when (transition?.mode) {
            InterpolationMode.STEP -> prev.boneTransforms
            InterpolationMode.LINEAR -> interpolateLinear(prev, next, progress)
            InterpolationMode.BEZIER, InterpolationMode.EASE_IN_OUT -> {
                applyEasing(prev, next, progress, transition)
            }
            null -> interpolateLinear(prev, next, progress)
        }
    }

    private fun interpolateLinear(
        prev: PoseSnapshot, next: PoseSnapshot, t: Float
    ): Map<Int, MatrixData> {
        val result = mutableMapOf<Int, MatrixData>()
        val allBoneIds = (prev.boneTransforms.keys + next.boneTransforms.keys)

        for (boneId in allBoneIds) {
            val p = prev.boneTransforms[boneId]
            val n = next.boneTransforms[boneId]

            result[boneId] = when {
                p != null && n != null -> lerpMatrix(p, n, t)
                p != null -> p
                else -> n ?: continue
            }
        }
        return result
    }

    private fun applyEasing(
        prev: PoseSnapshot, next: PoseSnapshot, rawT: Float,
        transition: TransitionInterval?
    ): Map<Int, MatrixData> {
        // 使用简单的 ease-in-out 曲线（后续可替换为自定义贝塞尔）
        val easedT = when (transition?.mode) {
            InterpolationMode.EASE_IN_OUT -> {
                // smoothstep: 3t² - 2t³
                val tt = rawT.coerceIn(0f, 1f)
                tt * tt * (3f - 2f * tt)
            }
            InterpolationMode.BEZIER -> {
                // 简化版贝塞尔近似
                val tt = rawT.coerceIn(0f, 1f)
                tt * tt * (3f - 2f * tt)
            }
            else -> rawT.coerceIn(0f, 1f)
        }
        return interpolateLinear(prev, next, easedT)
    }

    private fun lerpMatrix(a: MatrixData, b: MatrixData, t: Float): MatrixData {
        return MatrixData(
            m00 = lerp(a.m00, b.m00, t), m01 = lerp(a.m01, b.m01, t),
            m02 = lerp(a.m02, b.m02, t), m03 = lerp(a.m03, b.m03, t),
            m10 = lerp(a.m10, b.m10, t), m11 = lerp(a.m11, b.m11, t),
            m12 = lerp(a.m12, b.m12, t), m13 = lerp(a.m13, b.m13, t),
            m20 = lerp(a.m20, b.m20, t), m21 = lerp(a.m21, b.m21, t),
            m22 = lerp(a.m22, b.m22, t), m23 = lerp(a.m23, b.m23, t),
            m30 = lerp(a.m30, b.m30, t), m31 = lerp(a.m31, b.m31, t),
            m32 = lerp(a.m32, b.m32, t), m33 = lerp(a.m33, b.m33, t)
        )
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
