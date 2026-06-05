package com.example.glimmerseed.floatingpreviewbase.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.editorcore.animation.Vertex
import org.joml.Matrix4f
import org.joml.Vector4f

class CanvasSkinningRenderer {

    private val skinVec = Vector4f()
    private val trianglePath = Path()

    private val triangleFillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        isDither = true
    }

    private val triangleStrokePaint = Paint().apply {
        color = Color.argb(60, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val skeletonBonePaint = Paint().apply {
        color = Color.argb(150, 200, 200, 80)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val skeletonJointPaint = Paint().apply {
        color = Color.argb(180, 255, 200, 80)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    data class SkinningInput(
        val skeleton: Skeleton,
        val mesh: Mesh,
        val texture: Bitmap? = null,
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f
    )

    fun render(canvas: Canvas, input: SkinningInput) {
        input.skeleton.updateAllWorldTransforms()
        
        val boneMatrices = input.skeleton.bones.map { 
            input.skeleton.getBoneWorldTransform(it.id)
        }
        val vertices = input.mesh.vertices
        val indices = input.mesh.indices
        val ox = input.offsetX
        val oy = input.offsetY
        val sx = input.scaleX
        val sy = input.scaleY

        if (indices.size < 3) return

        val meshStrokePaint = if (input.texture != null) {
            Paint().apply {
                color = Color.argb(40, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
        } else {
            null
        }

        for (triIdx in indices.indices step 3) {
            if (triIdx + 2 >= indices.size) break

            val i0 = indices[triIdx]
            val i1 = indices[triIdx + 1]
            val i2 = indices[triIdx + 2]

            val v0 = vertices.getOrNull(i0) ?: continue
            val v1 = vertices.getOrNull(i1) ?: continue
            val v2 = vertices.getOrNull(i2) ?: continue

            val p0 = skinVertex(v0, boneMatrices)
            val p1 = skinVertex(v1, boneMatrices)
            val p2 = skinVertex(v2, boneMatrices)

            trianglePath.rewind()
            trianglePath.moveTo(p0.x * sx + ox, p0.y * sy + oy)
            trianglePath.lineTo(p1.x * sx + ox, p1.y * sy + oy)
            trianglePath.lineTo(p2.x * sx + ox, p2.y * sy + oy)
            trianglePath.close()

            if (input.texture != null) {
                val avgColor = averageTriangleColor(input.texture, v0.texCoord, v1.texCoord, v2.texCoord)
                triangleFillPaint.color = avgColor
            } else {
                triangleFillPaint.color = Color.argb(
                    100,
                    (140 + (triIdx * 37) % 80).coerceIn(0, 255),
                    (160 + (triIdx * 53) % 60).coerceIn(0, 255),
                    (120 + (triIdx * 71) % 100).coerceIn(0, 255)
                )
            }

            canvas.drawPath(trianglePath, triangleFillPaint)
            meshStrokePaint?.let { canvas.drawPath(trianglePath, it) }
        }

        if (input.skeleton.bones.isNotEmpty()) {
            drawSkeleton(canvas, input)
        }
    }

    fun renderSkeletonOnly(canvas: Canvas, input: SkinningInput) {
        drawSkeleton(canvas, input)
    }

    private fun drawSkeleton(canvas: Canvas, input: SkinningInput) {
        val boneMap = input.skeleton.bones.associateBy { it.id }
        val ox = input.offsetX
        val oy = input.offsetY
        val sx = input.scaleX
        val sy = input.scaleY

        for (bone in input.skeleton.bones) {
            val worldMat = input.skeleton.getBoneWorldTransform(bone.id)
            val jointX = worldMat.m30() * sx + ox
            val jointY = worldMat.m31() * sy + oy

            canvas.drawCircle(jointX, jointY, 6f, skeletonJointPaint)

            val parentId = bone.parentId
            if (parentId != null) {
                val parent = boneMap[parentId]
                if (parent != null) {
                    val pMat = input.skeleton.getBoneWorldTransform(parent.id)
                    val px = pMat.m30() * sx + ox
                    val py = pMat.m31() * sy + oy
                    canvas.drawLine(px, py, jointX, jointY, skeletonBonePaint)
                }
            }
        }
    }

    private fun skinVertex(vertex: Vertex, boneMatrices: List<Matrix4f>): Vector4f {
        val x = vertex.position.x
        val y = vertex.position.y
        val z = vertex.position.z

        var resultX = 0.0f
        var resultY = 0.0f
        var resultZ = 0.0f
        var resultW = 0.0f

        for (i in 0 until 4) {
            val boneIdx = vertex.boneIDs[i]
            val weight = vertex.boneWeights[i]
            if (weight <= 0.0f || boneIdx < 0 || boneIdx >= boneMatrices.size) continue

            val mat = boneMatrices[boneIdx]
            val w = weight * 1.0f
            val m00 = mat.m00()
            val m01 = mat.m01()
            val m02 = mat.m02()
            val m03 = mat.m03()
            val m10 = mat.m10()
            val m11 = mat.m11()
            val m12 = mat.m12()
            val m13 = mat.m13()
            val m20 = mat.m20()
            val m21 = mat.m21()
            val m22 = mat.m22()
            val m23 = mat.m23()
            val m30 = mat.m30()
            val m31 = mat.m31()
            val m32 = mat.m32()
            val m33 = mat.m33()
            
            resultX += (m00 * x + m01 * y + m02 * z + m03) * w
            resultY += (m10 * x + m11 * y + m12 * z + m13) * w
            resultZ += (m20 * x + m21 * y + m22 * z + m23) * w
            resultW += (m30 * x + m31 * y + m32 * z + m33) * w
        }

        skinVec.set(resultX, resultY, resultZ, resultW)
        return skinVec
    }

    private fun averageTriangleColor(
        texture: Bitmap,
        uv0: org.joml.Vector2f,
        uv1: org.joml.Vector2f,
        uv2: org.joml.Vector2f
    ): Int {
        val texW = texture.width
        val texH = texture.height

        val u = (uv0.x + uv1.x + uv2.x) / 3f
        val v = (uv0.y + uv1.y + uv2.y) / 3f

        val px = (u * texW).toInt().coerceIn(0, texW - 1)
        val py = (v * texH).toInt().coerceIn(0, texH - 1)

        return texture.getPixel(px, py)
    }
}