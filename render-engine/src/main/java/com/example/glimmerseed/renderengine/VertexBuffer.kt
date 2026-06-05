package com.example.glimmerseed.renderengine

/**
 * 顶点缓冲区接口
 */
interface VertexBuffer {
    /**
     * 绑定顶点缓冲区
     */
    fun bind()

    /**
     * 解绑顶点缓冲区
     */
    fun unbind()

    /**
     * 更新顶点数据
     * @param data 新的顶点数据
     */
    fun updateData(data: FloatArray)

    /**
     * 更新索引数据
     * @param data 新的索引数据
     */
    fun updateIndices(data: IntArray)

    /**
     * 获取顶点数量
     */
    val vertexCount: Int

    /**
     * 获取索引数量
     */
    val indexCount: Int
}
