package com.example.glimmerseed.editorcore.io

import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.ik.IKConstraint
import com.example.glimmerseed.editorcore.panel.PanelData
import com.example.glimmerseed.editorcore.stage.StageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProjectManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun saveProject(
        file: File,
        name: String,
        skeleton: Skeleton,
        animations: List<AnimationClip> = emptyList(),
        panels: List<PanelData> = emptyList(),
        stage: StageData? = null,
        ikConstraints: List<IKConstraint> = emptyList()
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val projectFile = ProjectFile(
                version = 3,
                name = name,
                skeleton = SkeletonData.fromSkeleton(skeleton),
                animations = animations.map { it.toAnimationData() },
                panels = panels,
                stage = stage,
                ikConstraints = ikConstraints
            )

            val jsonString = json.encodeToString(projectFile)
            file.writeText(jsonString)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun loadProject(file: File): LoadResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val jsonString = file.readText()
            val rawProjectFile = json.decodeFromString<ProjectFile>(jsonString)

            val projectFile = if (rawProjectFile.version < 3) {
                ProjectFile.migrateV2ToV3(rawProjectFile)
            } else {
                rawProjectFile
            }

            val skeleton = projectFile.skeleton.toSkeleton()

            LoadResult.Success(
                name = projectFile.name,
                skeleton = skeleton,
                animations = projectFile.animations.map { it.toAnimationClip() },
                panels = projectFile.panels,
                stage = projectFile.stage,
                ikConstraints = projectFile.ikConstraints
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    sealed class LoadResult {
        data class Success(
            val name: String,
            val skeleton: Skeleton,
            val animations: List<AnimationClip>,
            val panels: List<PanelData> = emptyList(),
            val stage: StageData? = null,
            val ikConstraints: List<IKConstraint> = emptyList()
        ) : LoadResult()

        data class Error(val exception: Exception) : LoadResult()
    }
}

/**
 * AnimationClip 转 AnimationData 的扩展方法（简化版）
 */
fun AnimationClip.toAnimationData(): AnimationData {
    return AnimationData(
        name = name,
        duration = duration,
        boneTracks = emptyList()
    )
}

/**
 * AnimationData 转 AnimationClip 的扩展方法（简化版）
 */
fun AnimationData.toAnimationClip(): AnimationClip {
    return AnimationClip(
        name = name,
        duration = duration,
        boneKeyframes = emptyMap()
    )
}