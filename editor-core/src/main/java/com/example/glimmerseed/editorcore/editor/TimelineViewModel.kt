package com.example.glimmerseed.editorcore.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Keyframe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.joml.Vector2f

/**
 * 时间轴ViewModel
 * 管理时间轴的显示状态和操作
 */
class TimelineViewModel : ViewModel() {
    /** 时间轴显示状态 */
    private val _timelineState = MutableStateFlow(TimelineState())
    val timelineState: StateFlow<TimelineState> = _timelineState.asStateFlow()
    
    /**
     * 设置当前编辑的动画
     */
    fun setAnimation(animation: AnimationClip?) {
        _timelineState.value = _timelineState.value.copy(
            animation = animation,
            // 重置缩放和平移
            viewportScale = 1f,
            viewportOffset = Vector2f(0f, 0f)
        )
    }
    
    /**
     * 缩放时间轴
     * @param scaleFactor 缩放因子
     * @param pivot 缩放中心点(屏幕坐标)
     */
    fun scale(scaleFactor: Float, pivot: Float) {
        val currentState = _timelineState.value
        val newScale = (currentState.viewportScale * scaleFactor).coerceIn(0.1f, 20f)
        
        // 调整偏移，保持缩放点在相同位置
        val offsetChange = pivot * (newScale - currentState.viewportScale) / currentState.viewportScale
        val newOffset = currentState.viewportOffset.x + offsetChange
        
        _timelineState.value = currentState.copy(
            viewportScale = newScale,
            viewportOffset = Vector2f(newOffset, currentState.viewportOffset.y)
        )
    }
    
    /**
     * 平移时间轴
     * @param deltaX 水平平移量
     * @param deltaY 垂直平移量
     */
    fun pan(deltaX: Float, deltaY: Float) {
        val currentState = _timelineState.value
        _timelineState.value = currentState.copy(
            viewportOffset = Vector2f(
                currentState.viewportOffset.x + deltaX,
                currentState.viewportOffset.y + deltaY
            )
        )
    }
    
    /**
     * 选择关键帧
     */
    fun selectKeyframe(boneId: Int, time: Float) {
        val currentState = _timelineState.value
        _timelineState.value = currentState.copy(
            selectedKeyframe = KeyframeInfo(boneId, time)
        )
    }
    
    /**
     * 清除选择
     */
    fun clearSelection() {
        _timelineState.value = _timelineState.value.copy(
            selectedKeyframe = null
        )
    }
}

/**
 * 时间轴显示状态
 */
data class TimelineState(
    /** 当前编辑的动画 */
    val animation: AnimationClip? = null,
    /** 选中的关键帧 */
    val selectedKeyframe: KeyframeInfo? = null,
    /** 时间轴缩放倍数 */
    val viewportScale: Float = 1f,
    /** 时间轴偏移 */
    val viewportOffset: Vector2f = Vector2f(0f, 0f),
    /** 轨道高度 */
    val trackHeight: Float = 36f
)

/**
 * 关键帧信息
 */
data class KeyframeInfo(
    val boneId: Int,
    val time: Float
)
