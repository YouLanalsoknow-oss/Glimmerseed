package com.example.glimmerseed.renderengine.opengl

import android.opengl.GLES32
import android.opengl.Matrix
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.renderengine.Renderer
import com.example.glimmerseed.renderengine.Texture
import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * OpenGL ES 3.2渲染器实现
 * 使用GPU进行骨骼蒙皮计算
 * 支持VBO、IBO、VAO和UBO
 */
class GLESRenderer : Renderer {

    private lateinit var shaderProgram: GLESShaderProgram
    private var vertexBuffer: GLESVertexBuffer? = null
    private var boneMatricesUBO: BoneMatricesUBO? = null

    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val identityMatrix = FloatArray(16)

    companion object {
        // 顶点着色器 - 包含GPU蒙皮逻辑
        private val VERTEX_SHADER = """
            #version 320 es
            precision highp float;
            
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec2 aTexCoord;
            layout(location = 2) in ivec4 aBoneIDs;
            layout(location = 3) in vec4 aBoneWeights;
            
            layout(std140, binding = 0) uniform BoneMatrices {
                mat4 uBoneMatrices[100];
            };
            
            uniform mat4 uMVPMatrix;
            
            out vec2 vTexCoord;
            
            void main() {
                mat4 skinMatrix = mat4(0.0);
                
                // 累加最多4个骨骼的影响
                for (int i = 0; i < 4; i++) {
                    if (aBoneWeights[i] > 0.0001) {
                        skinMatrix += uBoneMatrices[aBoneIDs[i]] * aBoneWeights[i];
                    }
                }
                
                // 应用蒙皮矩阵
                vec4 skinnedPosition = skinMatrix * vec4(aPosition, 1.0);
                gl_Position = uMVPMatrix * skinnedPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        // 片段着色器
        private val FRAGMENT_SHADER = """
            #version 320 es
            precision highp float;
            
            in vec2 vTexCoord;
            uniform sampler2D uTexture;
            uniform vec4 uTintColor;
            
            out vec4 fragColor;
            
            void main() {
                vec4 texColor = texture(uTexture, vTexCoord);
                fragColor = texColor * uTintColor;
            }
        """.trimIndent()
    }

    init {
        Matrix.setIdentityM(identityMatrix, 0)
    }

    override fun initialize() {
        // 初始化着色器
        shaderProgram = GLESShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        // 初始化UBO
        boneMatricesUBO = BoneMatricesUBO()

        // 绑定UBO到着色器
        shaderProgram.bindUniformBlock("BoneMatrices", 0)

        // 设置初始状态
        GLES32.glEnable(GLES32.GL_DEPTH_TEST)
        GLES32.glEnable(GLES32.GL_CULL_FACE)
        GLES32.glClearColor(0.1f, 0.1f, 0.15f, 1.0f)
    }

    override fun resize(width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)

        // 计算投影矩阵（正交投影）
        val aspectRatio = width.toFloat() / height.toFloat()
        val orthoHeight = 2.0f
        val orthoWidth = orthoHeight * aspectRatio

        Matrix.orthoM(
            projectionMatrix, 0,
            -orthoWidth, orthoWidth,
            -orthoHeight, orthoHeight,
            -10.0f, 10.0f
        )

        // 视图矩阵（相机位置）
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 3f,
            0f, 0f, 0f,
            0f, 1f, 0f
        )
    }

    /**
     * 设置网格数据
     */
    fun setMesh(mesh: Mesh) {
        vertexBuffer?.destroy()
        vertexBuffer = GLESVertexBuffer(
            mesh.toFloatArray(),
            mesh.toIndexArray()
        )
    }

    override fun render(skeleton: Skeleton, mesh: Mesh, texture: Texture?) {
        // 清空画布
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        // 如果顶点缓冲区还没设置，设置一下
        if (vertexBuffer == null) {
            setMesh(mesh)
        }

        // 计算MVP矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // 收集所有骨骼的世界变换矩阵
        val boneMatrices = mutableListOf<FloatArray>()
        for (i in skeleton.bones.indices) {
            val bone = skeleton.bones[i]
            val worldMatrix = skeleton.getBoneWorldTransformByIndex(i)
            val matrixArray = FloatArray(16)
            jomlMatrixToArray(worldMatrix, matrixArray)
            boneMatrices.add(matrixArray)
        }

        // 更新并上传UBO
        boneMatricesUBO?.updateBones(boneMatrices)
        boneMatricesUBO?.uploadToGPU()
        boneMatricesUBO?.bind()

        // 绑定着色器
        shaderProgram.bind()
        shaderProgram.setUniformMatrix4("uMVPMatrix", mvpMatrix)
        shaderProgram.setUniform("uTintColor", floatArrayOf(1f, 1f, 1f, 1f))

        // 绑定纹理
        (texture as? GLESTexture)?.let {
            it.bind(0)
            shaderProgram.setUniform("uTexture", 0)
        }

        // 绑定顶点并绘制
        vertexBuffer?.bind()
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLES,
            vertexBuffer?.indexCount ?: 0,
            GLES32.GL_UNSIGNED_INT,
            0
        )
        vertexBuffer?.unbind()

        // 解绑
        texture?.unbind()
        shaderProgram.unbind()
    }

    override fun destroy() {
        vertexBuffer?.destroy()
        vertexBuffer = null

        boneMatricesUBO?.destroy()
        boneMatricesUBO = null

        shaderProgram.destroy()
    }

    private fun jomlMatrixToArray(matrix: Matrix4fc, array: FloatArray) {
        matrix.get(array)
    }
}
