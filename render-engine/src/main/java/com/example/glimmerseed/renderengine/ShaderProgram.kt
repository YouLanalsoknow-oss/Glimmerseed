package com.example.glimmerseed.renderengine

/**
 * 着色器程序接口
 */
interface ShaderProgram {
    /**
     * 绑定着色器程序
     */
    fun bind()

    /**
     * 解绑着色器程序
     */
    fun unbind()

    /**
     * 设置Uniform变量
     */
    fun setUniform(name: String, value: Float)
    fun setUniform(name: String, value: Int)
    fun setUniform(name: String, value: FloatArray)
    fun setUniform(name: String, value: IntArray)
    fun setUniformMatrix4(name: String, matrix: FloatArray)

    /**
     * 获取着色器程序ID
     */
    val programId: Int
}
