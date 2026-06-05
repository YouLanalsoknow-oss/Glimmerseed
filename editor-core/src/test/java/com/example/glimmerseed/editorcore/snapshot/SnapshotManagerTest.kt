package com.example.glimmerseed.editorcore.snapshot

import org.junit.Test
import org.junit.Assert.*
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Bone

class SnapshotManagerTest {

    @Test
    fun `capture first snapshot`() {
        val manager = SnapshotManager()
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        val snapshot = manager.captureSnapshot(skeleton, timestamp = 0f, label = "First")
        
        assertEquals(1, manager.snapshotCount)
        assertEquals("First", snapshot.label)
        assertEquals(0f, snapshot.timestamp, 0.001f)
    }

    @Test
    fun `capture multiple snapshots`() {
        val manager = SnapshotManager()
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        manager.captureSnapshot(skeleton, timestamp = 0f, label = "Start")
        manager.captureSnapshot(skeleton, timestamp = 1f, label = "End")
        
        assertEquals(2, manager.snapshotCount)
        assertEquals(2, manager.currentSequence.snapshots.size)
    }

    @Test
    fun `remove snapshot`() {
        val manager = SnapshotManager()
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        val snap1 = manager.captureSnapshot(skeleton, timestamp = 0f)
        manager.captureSnapshot(skeleton, timestamp = 1f)
        
        assertEquals(2, manager.snapshotCount)
        
        manager.removeSnapshot(snap1.id)
        
        assertEquals(1, manager.snapshotCount)
    }

    @Test
    fun `update snapshot`() {
        val manager = SnapshotManager()
        val rootBone = Bone(id = 0, name = "root", parentId = null)
        val skeleton = Skeleton(bones = listOf(rootBone))
        
        val snap = manager.captureSnapshot(skeleton, timestamp = 0f, label = "Original")
        manager.updateSnapshot(snap.id, newTimestamp = 5f, newLabel = "Updated")
        
        val snapshots = manager.getAllSnapshots()
        assertEquals(1, snapshots.size)
        assertEquals("Updated", snapshots[0].label)
        assertEquals(5f, snapshots[0].timestamp, 0.001f)
    }
}
