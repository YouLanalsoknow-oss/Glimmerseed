package com.example.glimmerseed.editorcore.animation

import org.junit.Test
import org.junit.Assert.*
import org.joml.Matrix4f

class SkeletonTest {

    @Test
    fun `create empty skeleton`() {
        val skeleton = Skeleton(bones = emptyList())
        assertTrue(skeleton.bones.isEmpty())
        assertNull(skeleton.rootBoneId)
    }

    @Test
    fun `create skeleton with root bone`() {
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        assertEquals(1, skeleton.bones.size)
        assertEquals(0, skeleton.rootBoneId)
        assertNotNull(skeleton.getBoneById(0))
    }

    @Test
    fun `create skeleton with hierarchy`() {
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val childBone = Bone(id = 1, name = "child", parentId = 0)
        val skeleton = Skeleton(bones = listOf(rootBone, childBone))
        
        assertEquals(2, skeleton.bones.size)
        assertEquals(0, skeleton.rootBoneId)
        assertEquals(0, skeleton.getBoneById(1)?.parentId)
    }

    @Test
    fun `clone skeleton`() {
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        val clonedSkeleton = skeleton.clone()
        assertEquals(1, clonedSkeleton.bones.size)
        assertEquals(rootBone.id, clonedSkeleton.bones[0].id)
    }
}
