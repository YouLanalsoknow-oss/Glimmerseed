package com.example.glimmerseed.renderengine.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton
import com.example.glimmerseed.renderengine.Texture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL ES 3.2渲染SurfaceView
 */
class GLESRenderSurface @JvmOverloads constructor(
    context: Context
) : GLSurfaceView(context), GLSurfaceView.Renderer {

    private val renderer = GLESRenderer()

    private var currentSkeleton: Skeleton? = null
    private var currentMesh: Mesh? = null
    private var currentTexture: Texture? = null

    init {
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    /**
     * 设置要渲染的数据
     */
    fun setData(skeleton: Skeleton, mesh: Mesh, texture: Texture?) {
        this.currentSkeleton = skeleton
        this.currentMesh = mesh
        this.currentTexture = texture
        requestRender()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        renderer.initialize()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        renderer.resize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val skeleton = currentSkeleton ?: return
        val mesh = currentMesh ?: return
        renderer.render(skeleton, mesh, currentTexture)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        renderer.destroy()
    }
}
