package com.example.glimmerseed.editorcore.animation

import org.junit.Test
import org.junit.Assert.*
import org.joml.Vector3f
import org.joml.Quaternionf

class AnimationClipTest {

    @Test
    fun `create empty animation clip`() {
        val clip = AnimationClip(
            name = "Test Clip",
            duration = 5f
        )
        
        assertEquals("Test Clip", clip.name)
        assertEquals(5f, clip.duration, 0.001f)
        assertTrue(clip.boneKeyframes.isEmpty())
    }

    @Test
    fun `animation clip with keyframes`() {
        val keyframes = listOf(
            Keyframe(time = 0f, translation = Vector3f(0f, 0f, 0f)),
            Keyframe(time = 1f, translation = Vector3f(1f, 0f, 0f))
        )
        
        val clip = AnimationClip(
            name = "Test Clip",
            duration = 5f,
            boneKeyframes = mapOf(0 to keyframes)
        )
        
        assertEquals(1, clip.boneKeyframes.size)
        assertEquals(2, clip.boneKeyframes[0]?.size)
    }

    @Test
    fun `get bone transform at time`() {
        val keyframes = listOf(
            Keyframe(time = 0f, translation = Vector3f(0f, 0f, 0f)),
            Keyframe(time = 2f, translation = Vector3f(2f, 0f, 0f))
        )
        
        val clip = AnimationClip(
            name = "Test Clip",
            duration = 5f,
            boneKeyframes = mapOf(0 to keyframes)
        )
        
        val transform = clip.getBoneTransformAtTime(0, 1f)
        assertNotNull(transform)
    }
}
