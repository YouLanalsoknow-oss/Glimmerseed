package com.example.glimmerseed.editorcore.snapshot

import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.io.MatrixData
import java.util.UUID

/**
 * 姿势快照管理器
 *
 * 管理一个 PoseSequence 的完整生命周期：
 * - 捕获当前骨骼姿态为新快照
 * - 插入/删除/更新/排序快照
 * - 配置区间插值
 * - 导出为可序列化数据
 *
 * 线程安全：所有操作在调用线程执行，返回不可变对象。
 */
class SnapshotManager {

    private var sequence = PoseSequence()

    /** 当前序列（不可变视图） */
    val currentSequence: PoseSequence get() = sequence

    /**
     * 捕获当前骨骼状态为新的姿势快照
     *
     * @param skeleton 当前骨骼（只读取）
     * @param timestamp 时间戳，默认为序列末尾+1秒
     * @param label 可选标签
     * @return 新创建的快照
     */
    fun captureSnapshot(
        skeleton: Skeleton,
        timestamp: Float? = null,
        label: String? = null
    ): PoseSnapshot {
        val ts = timestamp ?: (sequence.totalDuration + 1f).coerceAtLeast(0f)
        val id = "snap_${UUID.randomUUID().toString().take(8)}"
        val snapshot = PoseSnapshot.capture(id, ts, label ?: "", skeleton)

        sequence = sequence.copy(
            snapshots = (sequence.snapshots + snapshot).sortedBy { it.timestamp }
        )

        // 自动为新区间创建默认插值配置
        setupDefaultTransition(snapshot)
        return snapshot
    }

    /**
     * 删除指定快照及其关联的区间配置
     */
    fun removeSnapshot(snapshotId: String) {
        val removed = sequence.snapshots.find { it.id == snapshotId } ?: return

        // 清理涉及此快照的所有区间过渡
        val cleanedTransitions = sequence.transitions.filterNot { (_, interval) ->
            interval.fromSnapshotId == snapshotId || interval.toSnapshotId == snapshotId
        }

        sequence = sequence.copy(
            snapshots = sequence.snapshots.filter { it.id != snapshotId },
            transitions = cleanedTransitions
        )
    }

    /**
     * 更新快照时间戳和标签
     */
    fun updateSnapshot(
        snapshotId: String,
        newTimestamp: Float? = null,
        newLabel: String? = null
    ) {
        val snapshots = sequence.snapshots.map { snap ->
            if (snap.id == snapshotId) {
                snap.copy(
                    timestamp = newTimestamp ?: snap.timestamp,
                    label = newLabel ?: snap.label
                )
            } else {
                snap
            }
        }.sortedBy { it.timestamp }

        sequence = sequence.copy(snapshots = snapshots)
    }

    /**
     * 设置两个快照之间的区间插值配置
     */
    fun setTransition(
        fromSnapshotId: String,
        toSnapshotId: String,
        mode: InterpolationMode,
        duration: Float = 0f
    ) {
        val key = "$fromSnapshotId->$toSnapshotId"
        val transition = TransitionInterval(fromSnapshotId, toSnapshotId, mode, duration)

        sequence = sequence.copy(
            transitions = sequence.transitions + (key to transition)
        )
    }

    /**
     * 在指定时间评估姿态（代理到 PoseSequence.evaluateAt）
     */
    fun evaluateAt(time: Float): Map<Int, MatrixData>? {
        return sequence.evaluateAt(time)
    }

    /**
     * 获取所有快照（按时间排序）
     */
    fun getAllSnapshots(): List<PoseSnapshot> = sequence.snapshots

    /**
     * 获取快照数量
     */
    val snapshotCount: Int get() = sequence.snapshots.size

    /**
     * 清空所有快照
     */
    fun clear() {
        sequence = PoseSequence()
    }

    /**
     * 替换整个序列（用于加载）
     */
    fun loadSequence(newSequence: PoseSequence) {
        sequence = newSequence
    }

    /**
     * 为新插入的快照自动建立默认区间过渡
     */
    private fun setupDefaultTransition(newSnapshot: PoseSnapshot) {
        val idx = sequence.snapshots.indexOf(newSnapshot)
        if (idx > 0) {
            val prev = sequence.snapshots[idx - 1]
            setTransition(prev.id, newSnapshot.id, InterpolationMode.LINEAR)
        }
        if (idx < sequence.snapshots.size - 1) {
            val next = sequence.snapshots[idx + 1]
            setTransition(newSnapshot.id, next.id, InterpolationMode.LINEAR)
        }
    }
}
