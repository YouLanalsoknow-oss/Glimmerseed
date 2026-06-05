package com.example.glimmerseed.editorcore.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glimmerseed.editorcore.command.AddBoneCommand
import com.example.glimmerseed.editorcore.command.AddKeyframeCommand
import com.example.glimmerseed.editorcore.command.CommandExecutor
import com.example.glimmerseed.editorcore.command.DeleteBoneCommand
import com.example.glimmerseed.editorcore.command.DeleteKeyframeCommand
import com.example.glimmerseed.editorcore.command.HistoryManager
import com.example.glimmerseed.editorcore.command.MoveBoneCommand
import com.example.glimmerseed.editorcore.command.RotateBoneCommand
import com.example.glimmerseed.editorcore.command.ScaleBoneCommand
import com.example.glimmerseed.editorcore.command.SelectBoneCommand
import com.example.glimmerseed.editorcore.data.AnimationDataManager
import com.example.glimmerseed.editorcore.data.createAnimationDataSnapshot
import com.example.glimmerseed.editorcore.export.ExportEngineType
import com.example.glimmerseed.editorcore.export.ExportManager
import com.example.glimmerseed.editorcore.export.ExportOptions
import com.example.glimmerseed.editorcore.export.SpineExporter
import com.example.glimmerseed.editorcore.ik.IKConstraint
import com.example.glimmerseed.editorcore.ik.IKManager
import com.example.glimmerseed.editorcore.io.ProjectManager
import kotlinx.coroutines.launch
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * 编辑器ViewModel
 * 管理EditorState，提供dispatch方法处理所有编辑操作
 */
class EditorViewModel(
    private val historyManager: HistoryManager? = null
) : ViewModel() {
    // 内部状态Flow
    private val _state = MutableStateFlow(EditorState())
    // 公开只读状态Flow
    val state: StateFlow<EditorState> = _state.asStateFlow()

    // 命令执行器
    private val commandExecutor = CommandExecutor(historyManager = historyManager)
    
    // 动画数据管理器
    val animationDataManager = AnimationDataManager()
    
    // IK管理器
    val ikManager = IKManager()

    // 项目管理器
    val projectManager = ProjectManager()

    // 姿势快照管理器
    val snapshotManager = com.example.glimmerseed.editorcore.snapshot.SnapshotManager()

    private var isAutoSnapshotEnabled = false
    var currentProjectName: String = "Untitled"
    
    /**
     * 派发编辑操作
     * @param action 要执行的编辑操作
     */
    fun dispatch(action: EditorAction) {
        when (action) {
            is EditorAction.SelectBone -> handleSelectBone(action)
            is EditorAction.SetCurrentTime -> handleSetCurrentTime(action)
            is EditorAction.SetSelectedFrame -> handleSetSelectedFrame(action)
            is EditorAction.SetPlaying -> handleSetPlaying(action)
            is EditorAction.SetManipulatorMode -> handleSetManipulatorMode(action)
            is EditorAction.TranslateViewport -> handleTranslateViewport(action)
            is EditorAction.ScaleViewport -> handleScaleViewport(action)
            is EditorAction.RotateViewport -> handleRotateViewport(action)
            is EditorAction.ToggleGrid -> handleToggleGrid(action)
            is EditorAction.SetShowGrid -> handleSetShowGrid(action)
            is EditorAction.SetAnimation -> handleSetAnimation(action)
            is EditorAction.SetSkeleton -> handleSetSkeleton(action)
            is EditorAction.SetMesh -> handleSetMesh(action)
            is EditorAction.MoveBone -> handleMoveBone(action)
            is EditorAction.RotateBone -> handleRotateBone(action)
            is EditorAction.ScaleBone -> handleScaleBone(action)
            is EditorAction.AddKeyframe -> handleAddKeyframe(action)
            is EditorAction.DeleteKeyframe -> handleDeleteKeyframe(action)
            is EditorAction.ToggleOnionSkin -> handleToggleOnionSkin(action)
            is EditorAction.SetOnionSkinPreviousFrames -> handleSetOnionSkinPreviousFrames(action)
            is EditorAction.SetOnionSkinNextFrames -> handleSetOnionSkinNextFrames(action)
            is EditorAction.SetOnionSkinOpacity -> handleSetOnionSkinOpacity(action)
            is EditorAction.ToggleSkeletalPreview -> handleToggleSkeletalPreview(action)
            is EditorAction.AddPanel -> handleAddPanel(action)
            is EditorAction.RemovePanel -> handleRemovePanel(action)
            is EditorAction.SelectPanel -> handleSelectPanel(action)
            is EditorAction.UpdatePanelPosition -> handleUpdatePanelPosition(action)
            is EditorAction.UpdatePanelTouchMode -> handleUpdatePanelTouchMode(action)
            is EditorAction.UpdatePanelSlot -> handleUpdatePanelSlot(action)
            is EditorAction.SetStageEditing -> handleSetStageEditing(action)
            is EditorAction.AddBone -> handleAddBone(action)
            is EditorAction.DeleteBone -> handleDeleteBone(action)
            is EditorAction.DuplicateBone -> handleDuplicateBone(action)
            is EditorAction.MirrorBone -> handleMirrorBone(action)
            is EditorAction.RenameBone -> handleRenameBone(action)
            is EditorAction.AddIKConstraint -> handleAddIKConstraint(action)
            is EditorAction.RemoveIKConstraint -> handleRemoveIKConstraint(action)
            is EditorAction.UpdateIKConstraint -> handleUpdateIKConstraint(action)
            is EditorAction.SetIKTargetPosition -> handleSetIKTargetPosition(action)
            is EditorAction.ToggleIKEnabled -> handleToggleIKEnabled(action)
            EditorAction.SolveIK -> handleSolveIK()
            is EditorAction.SetSkinWeights -> handleSetSkinWeights(action)
            is EditorAction.ResetSkinWeights -> handleResetSkinWeights()
            is EditorAction.ToggleWeightPaintMode -> handleToggleWeightPaintMode()
            is EditorAction.SmoothSkinWeights -> handleSmoothSkinWeights()
            is EditorAction.PaintWeight -> handlePaintWeight(action)
            is EditorAction.SetVertexWeight -> handleSetVertexWeight(action)
            is EditorAction.CopyWeights -> handleCopyWeights()
            is EditorAction.PasteWeights -> handlePasteWeights()
            is EditorAction.MirrorWeights -> handleMirrorWeights()
            is EditorAction.ExportWeights -> handleExportWeights()
            is EditorAction.ImportWeights -> handleImportWeights()

            // 姿势快照操作
            is EditorAction.CaptureSnapshot -> handleCaptureSnapshot()
            is EditorAction.RemoveSnapshot -> handleRemoveSnapshot(action)
            is EditorAction.UpdateSnapshot -> handleUpdateSnapshot(action)
            is EditorAction.SetSnapshotTransition -> handleSetSnapshotTransition(action)
        }
    }
    
    /**
     * 撤销上一个操作
     */
    fun undo() {
        if (commandExecutor.canUndo()) {
            val newState = commandExecutor.undo(_state.value)
            _state.value = newState
        }
    }
    
    /**
     * 重做上一个撤销的操作
     */
    fun redo() {
        if (commandExecutor.canRedo()) {
            val newState = commandExecutor.redo(_state.value)
            _state.value = newState
        }
    }
    
    /**
     * 检查是否可以撤销
     */
    fun canUndo(): Boolean = commandExecutor.canUndo()
    
    /**
     * 检查是否可以重做
     */
    fun canRedo(): Boolean = commandExecutor.canRedo()
    
    // ==================== 操作处理方法 ====================
    
    private fun handleSelectBone(action: EditorAction.SelectBone) {
        val command = SelectBoneCommand(action.boneId, _state.value.selectedBoneId)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }
    
    private fun handleSetCurrentTime(action: EditorAction.SetCurrentTime) {
        _state.value = _state.value.copy(currentTime = action.time)
    }
    
    private fun handleSetSelectedFrame(action: EditorAction.SetSelectedFrame) {
        _state.value = _state.value.copy(selectedFrame = action.frame)
    }
    
    private fun handleSetPlaying(action: EditorAction.SetPlaying) {
        _state.value = _state.value.copy(isPlaying = action.isPlaying)
    }
    
    private fun handleSetManipulatorMode(action: EditorAction.SetManipulatorMode) {
        _state.value = _state.value.copy(manipulatorMode = action.mode)
    }
    
    private fun handleTranslateViewport(action: EditorAction.TranslateViewport) {
        val newTranslation = Vector2f(_state.value.viewportTranslation).add(action.delta)
        _state.value = _state.value.copy(viewportTranslation = newTranslation)
    }
    
    private fun handleScaleViewport(action: EditorAction.ScaleViewport) {
        val currentState = _state.value
        val oldScale = currentState.viewportScale
        val newScale = (oldScale * action.scale).coerceIn(0.1f, 10f)
        
        // 计算缩放中心调整
        val pivot = action.pivot
        val translation = Vector2f(currentState.viewportTranslation)
        val scaleDelta = newScale / oldScale
        
        // 保持缩放中心不变
        translation.x = pivot.x - (pivot.x - translation.x) * scaleDelta
        translation.y = pivot.y - (pivot.y - translation.y) * scaleDelta
        
        _state.value = currentState.copy(
            viewportScale = newScale,
            viewportTranslation = translation
        )
    }
    
    private fun handleRotateViewport(action: EditorAction.RotateViewport) {
        val newRotation = _state.value.viewportRotation + action.delta
        _state.value = _state.value.copy(viewportRotation = newRotation)
    }
    
    private fun handleToggleGrid(action: EditorAction.ToggleGrid) {
        _state.value = _state.value.copy(showGrid = action.show)
    }
    
    private fun handleSetShowGrid(action: EditorAction.SetShowGrid) {
        _state.value = _state.value.copy(showGrid = action.show)
    }
    
    private fun handleSetAnimation(action: EditorAction.SetAnimation) {
        _state.value = _state.value.copy(currentAnimation = action.animation)
    }
    
    private fun handleSetSkeleton(action: EditorAction.SetSkeleton) {
        _state.value = EditorState(
            skeleton = action.skeleton,
            mesh = _state.value.mesh,
            selectedBoneId = _state.value.selectedBoneId,
            currentTime = _state.value.currentTime,
            currentAnimation = _state.value.currentAnimation,
            isPlaying = _state.value.isPlaying,
            viewportTranslation = _state.value.viewportTranslation,
            viewportScale = _state.value.viewportScale,
            viewportRotation = _state.value.viewportRotation,
            manipulatorMode = _state.value.manipulatorMode,
            showGrid = _state.value.showGrid,
            ikConstraints = _state.value.ikConstraints,
            ikTargetPositions = _state.value.ikTargetPositions,
            ikEnabled = _state.value.ikEnabled,
            onionSkinEnabled = _state.value.onionSkinEnabled,
            onionSkinPreviousFrames = _state.value.onionSkinPreviousFrames,
            onionSkinNextFrames = _state.value.onionSkinNextFrames,
            onionSkinOpacity = _state.value.onionSkinOpacity
        )
    }
    
    private fun handleSetMesh(action: EditorAction.SetMesh) {
        _state.value = _state.value.copy(mesh = action.mesh)
    }

    private fun handleToggleOnionSkin(action: EditorAction.ToggleOnionSkin) {
        _state.value = _state.value.copy(onionSkinEnabled = action.enabled)
    }

    private fun handleSetOnionSkinPreviousFrames(action: EditorAction.SetOnionSkinPreviousFrames) {
        _state.value = _state.value.copy(onionSkinPreviousFrames = action.count.coerceIn(0, 10))
    }

    private fun handleSetOnionSkinNextFrames(action: EditorAction.SetOnionSkinNextFrames) {
        _state.value = _state.value.copy(onionSkinNextFrames = action.count.coerceIn(0, 10))
    }

    private fun handleSetOnionSkinOpacity(action: EditorAction.SetOnionSkinOpacity) {
        _state.value = _state.value.copy(onionSkinOpacity = action.opacity.coerceIn(0f, 1f))
    }

    private fun handleToggleSkeletalPreview(action: EditorAction.ToggleSkeletalPreview) {
        _state.value = _state.value.copy(showSkeletalPreview = action.enabled)
    }

    private fun handleAddPanel(action: EditorAction.AddPanel) {
        val current = _state.value.panelEditing
        val newPanels = current.panels + action.data
        val newSlots = current.slots + (action.data.id to action.slot)
        _state.value = _state.value.copy(
            panelEditing = current.copy(panels = newPanels, slots = newSlots)
        )
    }

    private fun handleRemovePanel(action: EditorAction.RemovePanel) {
        val current = _state.value.panelEditing
        val newPanels = current.panels.filter { it.id != action.panelId }
        val newSlots = current.slots - action.panelId
        _state.value = _state.value.copy(
            panelEditing = current.copy(
                panels = newPanels,
                slots = newSlots,
                selectedPanelId = if (current.selectedPanelId == action.panelId) null else current.selectedPanelId
            )
        )
    }

    private fun handleSelectPanel(action: EditorAction.SelectPanel) {
        val current = _state.value.panelEditing
        _state.value = _state.value.copy(
            panelEditing = current.copy(selectedPanelId = action.panelId)
        )
    }

    private fun handleUpdatePanelPosition(action: EditorAction.UpdatePanelPosition) {
        val current = _state.value.panelEditing
        val existing = current.slots[action.panelId] ?: return
        val updated = existing.copy(
            landscapeRect = action.landscapeRect,
            portraitRect = action.portraitRect
        )
        _state.value = _state.value.copy(
            panelEditing = current.copy(slots = current.slots + (action.panelId to updated))
        )
    }

    private fun handleUpdatePanelTouchMode(action: EditorAction.UpdatePanelTouchMode) {
        val current = _state.value.panelEditing
        val existing = current.slots[action.panelId] ?: return
        val updated = existing.copy(touchMode = action.touchMode)
        _state.value = _state.value.copy(
            panelEditing = current.copy(slots = current.slots + (action.panelId to updated))
        )
    }

    private fun handleUpdatePanelSlot(action: EditorAction.UpdatePanelSlot) {
        _state.value = _state.value.copy(
            panelEditing = _state.value.panelEditing.copy(
                slots = _state.value.panelEditing.slots + (action.panelId to action.slot)
            )
        )
    }

    private fun handleSetStageEditing(action: EditorAction.SetStageEditing) {
        if (!action.enabled) {
            _state.value = _state.value.copy(
                panelEditing = _state.value.panelEditing.copy(selectedPanelId = null)
            )
        }
    }
    
    private fun handleMoveBone(action: EditorAction.MoveBone) {
        val command = MoveBoneCommand(action.boneId, action.delta)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }

    private fun handleRotateBone(action: EditorAction.RotateBone) {
        val command = RotateBoneCommand(action.boneId, action.delta)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }

    private fun handleScaleBone(action: EditorAction.ScaleBone) {
        val command = ScaleBoneCommand(action.boneId, action.delta)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }

    private fun handleAddKeyframe(action: EditorAction.AddKeyframe) {
        val command = AddKeyframeCommand(action.boneId, action.time)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }

    private fun handleDeleteKeyframe(action: EditorAction.DeleteKeyframe) {
        val command = DeleteKeyframeCommand(action.boneId, action.time)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }
    
    private fun handleAddBone(action: EditorAction.AddBone) {
        val command = AddBoneCommand("Bone ${System.currentTimeMillis()}", action.parentId)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }
    
    private fun handleDeleteBone(action: EditorAction.DeleteBone) {
        val command = DeleteBoneCommand(action.boneId)
        val newState = commandExecutor.execute(command, _state.value)
        _state.value = newState
    }
    
    private fun handleDuplicateBone(action: EditorAction.DuplicateBone) {
        // TODO: 实现复制骨骼功能
    }
    
    private fun handleMirrorBone(action: EditorAction.MirrorBone) {
        // TODO: 实现镜像骨骼功能
    }
    
    private fun handleRenameBone(action: EditorAction.RenameBone) {
        // TODO: 实现重命名骨骼功能
    }
    
    private fun handleAddIKConstraint(action: EditorAction.AddIKConstraint) {
        ikManager.addConstraint(action.constraint)
        val newConstraints = _state.value.ikConstraints + action.constraint
        _state.value = _state.value.copy(ikConstraints = newConstraints)
    }
    
    private fun handleRemoveIKConstraint(action: EditorAction.RemoveIKConstraint) {
        ikManager.removeConstraint(action.constraintId)
        val newConstraints = _state.value.ikConstraints.filterNot { it.id == action.constraintId }
        _state.value = _state.value.copy(ikConstraints = newConstraints)
    }
    
    private fun handleUpdateIKConstraint(action: EditorAction.UpdateIKConstraint) {
        ikManager.updateConstraint(action.constraint)
        val index = _state.value.ikConstraints.indexOfFirst { it.id == action.constraint.id }
        if (index >= 0) {
            val newConstraints = _state.value.ikConstraints.toMutableList()
            newConstraints[index] = action.constraint
            _state.value = _state.value.copy(ikConstraints = newConstraints)
        }
    }
    
    private fun handleSetIKTargetPosition(action: EditorAction.SetIKTargetPosition) {
        updateIKTarget(action.boneId, action.position)
    }
    
    private fun handleToggleIKEnabled(action: EditorAction.ToggleIKEnabled) {
        _state.value = _state.value.copy(ikEnabled = action.enabled)
    }
    
    private fun handleSolveIK() {
        val currentState = _state.value
        if (currentState.ikEnabled && currentState.skeleton != null) {
            val solved = ikManager.solveAll(currentState.skeleton)
            // 将 IK 求解结果应用到骨骼（深拷贝后修改局部变换）
            val skel = currentState.skeleton.clone()
            for ((boneId, transform) in solved.boneTransforms) {
                val bone = skel.getBoneById(boneId) ?: continue
                bone.localTransform.set(transform)
            }
            _state.value = _state.value.copy(skeleton = skel)
        }
    }
    
    private fun handleSetSkinWeights(action: EditorAction.SetSkinWeights) {
        _state.value = _state.value.copy(skinWeights = action.weights)
    }
    
    private fun handleResetSkinWeights() {
        _state.value = _state.value.copy(skinWeights = com.example.glimmerseed.editorcore.skin.SkinWeights())
    }
    
    private fun handleToggleWeightPaintMode() {
        val current = _state.value.isWeightPaintMode
        _state.value = _state.value.copy(isWeightPaintMode = !current)
    }
    
    private fun handleSmoothSkinWeights() {
        val current = _state.value
        val mesh = current.mesh
        if (mesh != null) {
            val vertices = mesh.vertices.map { org.joml.Vector3f(it.position.x, it.position.y, it.position.z) }
            val smoothed = com.example.glimmerseed.editorcore.skin.SkinWeightCalculator.smoothWeights(
                weights = current.skinWeights,
                vertices = vertices,
                iterations = 3,
                smoothFactor = 0.5f
            )
            _state.value = current.copy(skinWeights = smoothed)
        }
    }
    
    private fun handlePaintWeight(action: EditorAction.PaintWeight) {
        val current = _state.value
        val existing = current.skinWeights.getVertexWeights(action.vertexIndex)
        val boneWeights = existing?.boneWeights?.toMutableList() ?: mutableListOf()
        
        val index = boneWeights.indexOfFirst { it.boneId == action.boneId }
        if (index >= 0) {
            val newWeight = (boneWeights[index].weight + action.weight).coerceIn(0f, 1f)
            boneWeights[index] = com.example.glimmerseed.editorcore.skin.BoneWeight(action.boneId, newWeight)
        } else if (boneWeights.size < 4) {
            boneWeights.add(com.example.glimmerseed.editorcore.skin.BoneWeight(action.boneId, action.weight))
        }
        
        val normalized = com.example.glimmerseed.editorcore.skin.VertexWeights(action.vertexIndex, boneWeights).normalized()
        val newWeights = current.skinWeights.updateVertexWeights(action.vertexIndex, normalized)
        _state.value = current.copy(skinWeights = newWeights)
    }
    
    private fun handleSetVertexWeight(action: EditorAction.SetVertexWeight) {
        val current = _state.value
        val existing = current.skinWeights.getVertexWeights(action.vertexIndex)
        val boneWeights = existing?.boneWeights?.toMutableList() ?: mutableListOf()
        
        val index = boneWeights.indexOfFirst { it.boneId == action.boneId }
        if (index >= 0) {
            boneWeights[index] = com.example.glimmerseed.editorcore.skin.BoneWeight(action.boneId, action.weight)
        } else if (boneWeights.size < 4) {
            boneWeights.add(com.example.glimmerseed.editorcore.skin.BoneWeight(action.boneId, action.weight))
        }
        
        val normalized = com.example.glimmerseed.editorcore.skin.VertexWeights(action.vertexIndex, boneWeights).normalized()
        val newWeights = current.skinWeights.updateVertexWeights(action.vertexIndex, normalized)
        _state.value = current.copy(skinWeights = newWeights)
    }
    
    private fun handleCopyWeights() {
    }
    
    private fun handlePasteWeights() {
    }
    
    private fun handleMirrorWeights() {
    }
    
    private fun handleExportWeights() {
    }
    
    private fun handleImportWeights() {
    }

    /**
     * 启用自动快照发送
     */
    fun enableAutoSnapshot() {
        if (isAutoSnapshotEnabled) return
        
        isAutoSnapshotEnabled = true
        
        viewModelScope.launch {
            animationDataManager.startAutoSending(
                getSnapshot = {
                    val currentState = _state.value
                    if (currentState.skeleton != null) {
                        createAnimationDataSnapshot(
                            skeleton = currentState.skeleton,
                            mesh = currentState.mesh,
                            currentTime = currentState.currentTime,
                            isPlaying = currentState.isPlaying,
                            viewportTranslationX = currentState.viewportTranslation.x,
                            viewportTranslationY = currentState.viewportTranslation.y,
                            viewportScale = currentState.viewportScale
                        )
                    } else {
                        com.example.glimmerseed.editorcore.data.AnimationDataSnapshot()
                    }
                }
            )
        }
    }
    
    /**
     * 禁用自动快照发送
     */
    fun disableAutoSnapshot() {
        isAutoSnapshotEnabled = false
        animationDataManager.stopAutoSending()
    }
    
    /**
     * 手动发送当前快照
     */
    fun sendManualSnapshot() {
        val currentState = _state.value
        if (currentState.skeleton != null) {
            val snapshot = createAnimationDataSnapshot(
                skeleton = currentState.skeleton,
                mesh = currentState.mesh,
                currentTime = currentState.currentTime,
                isPlaying = currentState.isPlaying,
                viewportTranslationX = currentState.viewportTranslation.x,
                viewportTranslationY = currentState.viewportTranslation.y,
                viewportScale = currentState.viewportScale
            )
            animationDataManager.sendSnapshot(snapshot)
        }
    }
    
    // ==================== IK相关方法 ====================
    
    /**
     * 添加IK约束
     */
    fun addIKConstraint(constraint: IKConstraint) {
        ikManager.addConstraint(constraint)
        val newConstraints = _state.value.ikConstraints + constraint
        _state.value = _state.value.copy(ikConstraints = newConstraints)
    }
    
    /**
     * 移除IK约束
     */
    fun removeIKConstraint(constraintId: Int) {
        ikManager.removeConstraint(constraintId)
        val newConstraints = _state.value.ikConstraints.filterNot { it.id == constraintId }
        _state.value = _state.value.copy(ikConstraints = newConstraints)
    }
    
    /**
     * 更新IK目标位置
     */
    fun updateIKTarget(boneId: Int, targetPosition: Vector3f) {
        val currentState = _state.value
        val newTargets = currentState.ikTargetPositions.toMutableMap()
        newTargets[boneId] = targetPosition
        
        _state.value = currentState.copy(ikTargetPositions = newTargets)
        
        // 求解IK
        if (currentState.ikEnabled && currentState.skeleton != null) {
            val solved = ikManager.solveAll(currentState.skeleton)
            val skel = currentState.skeleton.clone()
            for ((boneId, transform) in solved.boneTransforms) {
                val bone = skel.getBoneById(boneId) ?: continue
                bone.localTransform.set(transform)
            }
            _state.value = _state.value.copy(skeleton = skel)
        }
    }
    
    /**
     * 切换IK启用状态
     */
    fun toggleIK(enabled: Boolean) {
        _state.value = _state.value.copy(ikEnabled = enabled)
    }

    /**
     * 保存项目到文件
     */
    fun saveProject(file: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = _state.value.skeleton?.let { skeleton ->
                projectManager.saveProject(
                    file = file,
                    name = currentProjectName,
                    skeleton = skeleton,
                    animations = _state.value.currentAnimation?.let { listOf(it) } ?: emptyList()
                )
            } ?: false
            onComplete(success)
        }
    }

    /**
     * 导出项目为指定引擎格式
     */
    fun exportProject(
        outputDir: File,
        engineType: ExportEngineType = ExportEngineType.SPINE,
        options: ExportOptions = ExportOptions(
            engineType = engineType,
            exportTextureAtlas = true,
            compressTextures = true,
            frameRate = 30,
            exportAllAnimations = true,
            animationList = emptyList()
        ),
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val exportManager = ExportManager()
            val skeleton = _state.value.skeleton
            val animations = _state.value.currentAnimation?.let { listOf(it) } ?: emptyList()

            if (skeleton != null) {
                exportManager.export(
                    skeleton = skeleton,
                    animations = animations,
                    outputDir = outputDir,
                    options = options
                )
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    /**
     * 从文件加载项目
     */
    fun loadProject(file: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = projectManager.loadProject(file)
            when (result) {
                is ProjectManager.LoadResult.Success -> {
                    currentProjectName = result.name
                    dispatch(EditorAction.SetSkeleton(result.skeleton))
                    if (result.animations.isNotEmpty()) {
                        dispatch(EditorAction.SetAnimation(result.animations.first()))
                    }
                    onComplete(true)
                }
                is ProjectManager.LoadResult.Error -> {
                    onComplete(false)
                }
            }
        }
    }

    // ========== 姿势快照操作处理 ==========

    /**
     * 捕获当前骨骼姿态为快照
     */
    private fun handleCaptureSnapshot() {
        val skeleton = _state.value.skeleton ?: return
        val snapshot = snapshotManager.captureSnapshot(skeleton)
        // 快照管理器内部已更新序列，无需额外状态变更
    }

    /**
     * 删除指定快照
     */
    private fun handleRemoveSnapshot(action: EditorAction.RemoveSnapshot) {
        snapshotManager.removeSnapshot(action.snapshotId)
    }

    /**
     * 更新快照时间戳或标签
     */
    private fun handleUpdateSnapshot(action: EditorAction.UpdateSnapshot) {
        snapshotManager.updateSnapshot(action.snapshotId, action.timestamp, action.label)
    }

    /**
     * 设置两个快照之间的插值模式
     */
    private fun handleSetSnapshotTransition(action: EditorAction.SetSnapshotTransition) {
        snapshotManager.setTransition(action.fromId, action.toId, action.mode)
    }
}
