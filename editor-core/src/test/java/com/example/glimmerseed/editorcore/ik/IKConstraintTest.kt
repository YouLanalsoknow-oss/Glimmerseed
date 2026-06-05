package com.example.glimmerseed.editorcore.ik

import org.junit.Test
import org.junit.Assert.*
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Bone
import org.joml.Vector3f

class IKConstraintTest {

    @Test
    fun `create IK constraint`() {
        val constraint = IKConstraint.create(targetBone = 0)
        
        assertEquals(0, constraint.targetBone)
        assertTrue(constraint.enable)
        assertEquals(1f, constraint.blendFactor, 0.001f)
    }

    @Test
    fun `create IK constraint with target`() {
        val target = Vector3f(1f, 2f, 3f)
        val constraint = IKConstraint.create(
            targetBone = 1,
            ikTarget = target,
            blendFactor = 0.5f
        )
        
        assertEquals(1, constraint.targetBone)
        assertEquals(1f, constraint.ikTarget.x, 0.001f)
        assertEquals(2f, constraint.ikTarget.y, 0.001f)
        assertEquals(3f, constraint.ikTarget.z, 0.001f)
        assertEquals(0.5f, constraint.blendFactor, 0.001f)
    }

    @Test
    fun `create solver from constraint`() {
        val constraint = IKConstraint.create(targetBone = 0)
        val solver = constraint.createSolver()
        
        assertNotNull(solver)
    }
}
