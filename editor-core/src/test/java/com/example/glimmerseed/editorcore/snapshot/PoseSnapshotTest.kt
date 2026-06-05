package com.example.glimmerseed.editorcore.snapshot

import org.junit.Test
import org.junit.Assert.*
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Bone
import org.joml.Matrix4f

class PoseSnapshotTest {

    @Test
    fun `capture snapshot from skeleton`() {
        // Create a simple skeleton for testing
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        rootBone.localTransform.translation(1f, 2f, 3f)
        
        val skeleton = Skeleton(bones = listOf(rootBone))

        // Capture snapshot
        val snapshot = PoseSnapshot.capture("test-snapshot", 0f, "Test Snapshot", skeleton)

        // Verify snapshot
        assertNotNull(snapshot)
        assertEquals("test-snapshot", snapshot.id)
        assertEquals(0f, snapshot.timestamp, 0.001f)
        assertEquals("Test Snapshot", snapshot.label)
        assertTrue(snapshot.boneTransforms.containsKey(0))
    }

    @Test
    fun `create pose sequence with transitions`() {
        // Create two snapshots
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        val snapshot1 = PoseSnapshot.capture("snap1", 0f, "Snapshot 1", skeleton)
        val snapshot2 = PoseSnapshot.capture("snap2", 1f, "Snapshot 2", skeleton)
        
        // Create pose sequence
        val sequence = PoseSequence(
            snapshots = listOf(snapshot1, snapshot2),
            transitions = mapOf(
                "snap1->snap2" to TransitionInterval(
                    fromSnapshotId = "snap1",
                    toSnapshotId = "snap2",
                    mode = InterpolationMode.LINEAR,
                    duration = 0.5f
                )
            )
        )
        
        // Verify sequence
        assertEquals(2, sequence.snapshots.size)
        assertEquals(1, sequence.transitions.size)
        assertEquals("snap1", sequence.transitions["snap1->snap2"]!!.fromSnapshotId)
        assertEquals("snap2", sequence.transitions["snap1->snap2"]!!.toSnapshotId)
        assertEquals(InterpolationMode.LINEAR, sequence.transitions["snap1->snap2"]!!.mode)
    }

    @Test
    fun `evaluate pose at time`() {
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        // Create snapshots at different times
        val snapshot1 = PoseSnapshot.capture("snap1", 0f, "Start", skeleton)
        val snapshot2 = PoseSnapshot.capture("snap2", 2f, "End", skeleton)
        
        val sequence = PoseSequence(
            snapshots = listOf(snapshot1, snapshot2),
            transitions = emptyMap(),
            loop = false
        )
        
        // Test evaluation at start
        val eval1 = sequence.evaluateAt(0f)
        assertNotNull(eval1)
        
        // Test evaluation at end
        val eval2 = sequence.evaluateAt(2f)
        assertNotNull(eval2)
    }
}
