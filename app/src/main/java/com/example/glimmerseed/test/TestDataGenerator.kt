package com.example.glimmerseed.test

import android.graphics.Bitmap
import android.graphics.Color
import com.example.glimmerseed.editorcore.animation.AnimationClip
import com.example.glimmerseed.editorcore.animation.Bone
import com.example.glimmerseed.editorcore.animation.Keyframe
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Vertex
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * 测试数据生成器
 */
object TestDataGenerator {

    /**
     * 生成一个简单的人形骨骼
     */
    fun generateHumanSkeleton(): Skeleton {
        val bones = mutableListOf<Bone>()

        // 根骨骼（躯干）
        val rootBone = Bone(
            id = 0,
            name = "Root",
            localTransform = Matrix4f().translate(0f, 0f, 0f)
        )
        bones.add(rootBone)

        // 上半身
        val spineBone = Bone(
            id = 1,
            name = "Spine",
            parentId = 0,
            localTransform = Matrix4f().translate(0f, 0.5f, 0f)
        )
        bones.add(spineBone)

        // 头部
        val headBone = Bone(
            id = 2,
            name = "Head",
            parentId = 1,
            localTransform = Matrix4f().translate(0f, 0.6f, 0f)
        )
        bones.add(headBone)

        // 左臂
        val leftShoulderBone = Bone(
            id = 3,
            name = "LeftShoulder",
            parentId = 1,
            localTransform = Matrix4f().translate(-0.4f, 0.3f, 0f)
        )
        bones.add(leftShoulderBone)

        val leftElbowBone = Bone(
            id = 4,
            name = "LeftElbow",
            parentId = 3,
            localTransform = Matrix4f().translate(-0.4f, 0f, 0f)
        )
        bones.add(leftElbowBone)

        // 右臂
        val rightShoulderBone = Bone(
            id = 5,
            name = "RightShoulder",
            parentId = 1,
            localTransform = Matrix4f().translate(0.4f, 0.3f, 0f)
        )
        bones.add(rightShoulderBone)

        val rightElbowBone = Bone(
            id = 6,
            name = "RightElbow",
            parentId = 5,
            localTransform = Matrix4f().translate(0.4f, 0f, 0f)
        )
        bones.add(rightElbowBone)

        return Skeleton(bones)
    }

    /**
     * 生成一个简单的测试Mesh
     */
    fun generateSimpleMesh(): Mesh {
        val vertices = mutableListOf<Vertex>()
        val indices = mutableListOf<Int>()

        // 简单的彩色方块
        val size = 0.5f

        // 顶点坐标
        val positions = listOf(
            Vector3f(-size, -size, 0f), // 0
            Vector3f(size, -size, 0f),  // 1
            Vector3f(size, size, 0f),   // 2
            Vector3f(-size, size, 0f)   // 3
        )

        // 纹理坐标
        val texCoords = listOf(
            Vector2f(0f, 1f),
            Vector2f(1f, 1f),
            Vector2f(1f, 0f),
            Vector2f(0f, 0f)
        )

        // 添加顶点（所有都绑定到根骨骼）
        for (i in positions.indices) {
            vertices.add(
                Vertex(
                    position = positions[i],
                    texCoord = texCoords[i],
                    boneIDs = intArrayOf(0, 0, 0, 0),
                    boneWeights = floatArrayOf(1f, 0f, 0f, 0f)
                )
            )
        }

        // 两个三角形组成一个正方形
        indices.addAll(listOf(0, 1, 2))
        indices.addAll(listOf(0, 2, 3))

        return Mesh(vertices, indices)
    }

    /**
     * 生成一个简单的测试纹理
     */
    fun generateTestTexture(): Bitmap {
        val width = 256
        val height = 256
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // 创建彩色格子图案
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = when {
                    (x + y) % 64 < 32 -> Color.rgb(128, 200, 255)
                    else -> Color.rgb(255, 180, 200)
                }
                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }

    /**
     * 生成一个简单的行走动画
     */
    fun generateWalkAnimation(): AnimationClip {
        val boneKeyframes = mutableMapOf<Int, List<Keyframe>>()

        // 头部轻微晃动
        boneKeyframes[2] = listOf(
            Keyframe(0f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.25f, Vector3f(0f, 0.05f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.5f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.75f, Vector3f(0f, -0.05f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(1f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f))
        )

        // 左臂摆动
        boneKeyframes[3] = listOf(
            Keyframe(0f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.25f, Vector3f(0f, 0f, 0.2f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.5f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.75f, Vector3f(0f, 0f, -0.2f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(1f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f))
        )

        // 右臂摆动（与左臂相反）
        boneKeyframes[5] = listOf(
            Keyframe(0f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.25f, Vector3f(0f, 0f, -0.2f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.5f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(0.75f, Vector3f(0f, 0f, 0.2f), Quaternionf(), Vector3f(1f, 1f, 1f)),
            Keyframe(1f, Vector3f(0f, 0f, 0f), Quaternionf(), Vector3f(1f, 1f, 1f))
        )

        return AnimationClip(
            name = "Walk",
            duration = 1f,
            boneKeyframes = boneKeyframes
        )
    }
}
