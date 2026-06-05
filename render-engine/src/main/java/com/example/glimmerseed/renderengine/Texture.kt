package com.example.glimmerseed.renderengine

/**
 * 纹理接口
 */
interface Texture {
    /**
     * 绑定纹理
     * @param unit 纹理单元
     */
    fun bind(unit: Int = 0)

    /**
     * 解绑纹理
     */
    fun unbind()

    /**
     * 获取纹理ID
     */
    val textureId: Int

    /**
     * 获取纹理宽度
     */
    val width: Int

    /**
     * 获取纹理高度
     */
    val height: Int

    /**
     * 销毁纹理，释放GPU资源
     */
    fun destroy()
}
