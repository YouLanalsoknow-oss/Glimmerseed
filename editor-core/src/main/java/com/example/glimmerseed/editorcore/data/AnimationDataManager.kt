package com.example.glimmerseed.editorcore.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 动画数据管理器
 * 负责管理主编辑器与悬浮窗之间的数据同步
 */
class AnimationDataManager {
    companion object {
        /** 默认快照发送间隔 (ms) */
        const val DEFAULT_SNAPSHOT_INTERVAL = 16L
    }

    private val _dataSnapshotFlow = MutableSharedFlow<AnimationDataSnapshot>(
        replay = 1,
        extraBufferCapacity = 2
    )
    val dataSnapshotFlow: SharedFlow<AnimationDataSnapshot> = _dataSnapshotFlow.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var isAutoSending = false
    private var lastSnapshot: AnimationDataSnapshot? = null

    /**
     * 发送数据快照
     */
    fun sendSnapshot(snapshot: AnimationDataSnapshot) {
        lastSnapshot = snapshot
        trySendIfDifferent(snapshot)
    }

    /**
     * 自动发送数据快照
     * @param getSnapshot 获取最新快照的函数
     * @param interval 发送间隔 (ms)
     */
    fun startAutoSending(
        getSnapshot: () -> AnimationDataSnapshot,
        interval: Long = DEFAULT_SNAPSHOT_INTERVAL
    ) {
        if (isAutoSending) return
        
        isAutoSending = true
        
        scope.launch {
            while (isAutoSending) {
                try {
                    val snapshot = getSnapshot()
                    sendSnapshot(snapshot)
                } catch (e: Exception) {
                    // 忽略发送异常，继续
                }
                delay(interval)
            }
        }
    }

    /**
     * 停止自动发送
     */
    fun stopAutoSending() {
        isAutoSending = false
    }

    private fun trySendIfDifferent(snapshot: AnimationDataSnapshot) {
        try {
            _dataSnapshotFlow.tryEmit(snapshot)
        } catch (e: Exception) {
            // 忽略发送错误
        }
    }
}
