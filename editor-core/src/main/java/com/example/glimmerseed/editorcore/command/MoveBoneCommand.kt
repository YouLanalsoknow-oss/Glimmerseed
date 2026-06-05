package com.example.glimmerseed.editorcore.command

import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Transform
import com.example.glimmerseed.editorcore.editor.EditorState
import org.joml.Matrix4f
import org.joml.Vector3f

/**
 * 移动骨骼命令
 */
class MoveBoneCommand(
    private val boneId: Int,
    private val delta: Vector3f
) : Command {
    
    // 保存原始变换矩阵用于撤销
    private var originalTransform: Matrix4f? = null
    
    override fun execute(state: EditorState): EditorState {
        val skeleton = state.skeleton ?: return state
        
        // 创建骨骼深拷贝
        val newSkeleton = Skeleton(
            bones = skeleton.bones.map { bone ->
                if (bone.id == boneId) {
                    // 保存原始变换
                    if (originalTransform == null) {
                        originalTransform = Matrix4f(bone.localTransform)
                    }
                    // 创建新骨骼并应用位移
                    val newBone = bone.clone()
                    // 获取当前变换
                    val currentTransform = Transform.fromMatrix4f(newBone.localTransform)
                    // 应用位移
                    val newTranslation = Vector3f(currentTransform.translation).add(delta)
                    val newTransformData = currentTransform.copy(translation = newTranslation)
                    // 更新骨骼变换
                    newBone.updateLocalTransform(newTransformData.toMatrix4f())
                    newBone
                } else {
                    bone.clone()
                }
            }
        )
        
        // 创建新的EditorState，因为Skeleton不是data class不能用copy
        return EditorState(
            skeleton = newSkeleton,
            selectedBoneId = state.selectedBoneId,
            currentTime = state.currentTime,
            currentAnimation = state.currentAnimation,
            isPlaying = state.isPlaying,
            viewportTranslation = state.viewportTranslation,
            viewportScale = state.viewportScale,
            viewportRotation = state.viewportRotation,
            manipulatorMode = state.manipulatorMode,
            showGrid = state.showGrid
        )
    }
    
    override fun undo(state: EditorState): EditorState {
        val skeleton = state.skeleton ?: return state
        val original = originalTransform ?: return state
        
        val newSkeleton = Skeleton(
            bones = skeleton.bones.map { bone ->
                if (bone.id == boneId) {
                    val newBone = bone.clone()
                    newBone.updateLocalTransform(original)
                    newBone
                } else {
                    bone.clone()
                }
            }
        )
        
        return EditorState(
            skeleton = newSkeleton,
            selectedBoneId = state.selectedBoneId,
            currentTime = state.currentTime,
            currentAnimation = state.currentAnimation,
            isPlaying = state.isPlaying,
            viewportTranslation = state.viewportTranslation,
            viewportScale = state.viewportScale,
            viewportRotation = state.viewportRotation,
            manipulatorMode = state.manipulatorMode,
            showGrid = state.showGrid
        )
    }
}
