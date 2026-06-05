package com.example.glimmerseed.renderengine

import com.example.glimmerseed.editorcore.animation.Mesh
import com.example.glimmerseed.editorcore.animation.Skeleton

/**
 * 渲染器接口，与Vulkan版本完全兼容
 */
interface Renderer {
    /**
     * 初始化渲染器
     */
    fun initialize()

    /**
     * 渲染一帧
     * @param skeleton 要渲染的骨骼
     * @param mesh 网格数据
     * @param texture 纹理
     */
    fun render(skeleton: Skeleton, mesh: Mesh, texture: Texture?)

    /**
     * 销毁渲染器，释放所有GPU资源
     */
    fun destroy()

    /**
     * 更新视图尺寸
     * @param width 宽度
     * @param height 高度
     */
    fun resize(width: Int, height: Int)
}
