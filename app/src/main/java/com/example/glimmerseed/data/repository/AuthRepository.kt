package com.example.glimmerseed.data.repository

import android.content.Context
import com.example.glimmerseed.app.data.SettingsDataStore
import com.example.glimmerseed.network.ApiClient
import com.example.glimmerseed.network.DeviceIdManager
import com.example.glimmerseed.data.model.LoginRequest
import com.example.glimmerseed.data.model.RegisterRequest

data class AuthResult(val success: Boolean, val message: String = "")

class AuthRepository(private val context: Context) {
    private val deviceId: String by lazy { 
        val id = DeviceIdManager.getOrCreate(context)
        id
    }

    private val settings: SettingsDataStore
        get() = SettingsDataStore.getInstance()

    suspend fun register(username: String, password: String, email: String?): AuthResult {
        return try {
            val response = ApiClient.apiService.register(
                RegisterRequest(username = username, password = password, email = email, deviceId = deviceId)
            )
            
            if (response.success && response.token != null) {
                settings.saveToken(response.token)
                
                response.userId?.let { userId ->
                    settings.saveUserId(userId)
                } ?: run {
                    settings.saveUserId(0L)
                }
                
                response.username?.let { 
                    settings.saveUsername(it)
                } ?: run {
                    settings.saveUsername(username)
                }

                response.avatarUrl?.let { avatarUrl ->
                    settings.saveAvatarUrl(avatarUrl)
                }

                AuthResult(success = true, message = response.message.ifEmpty { "注册成功" })
            } else {
                AuthResult(success = false, message = response.message.ifEmpty { "注册失败" })
            }
        } catch (e: Exception) {
            AuthResult(success = false, message = "网络错误: ${e.message}")
        }
    }

    suspend fun login(account: String, password: String): AuthResult {
        return try {
            val response = ApiClient.apiService.login(
                LoginRequest(account = account, password = password, deviceId = deviceId)
            )
            
            if (response.success && response.token != null) {
                settings.saveToken(response.token)
                
                response.userId?.let { userId ->
                    settings.saveUserId(userId)
                } ?: run {
                    settings.saveUserId(0L)
                }
                
                val savedUsername = response.username ?: "用户"
                settings.saveUsername(savedUsername)

                response.avatarUrl?.let { avatarUrl ->
                    settings.saveAvatarUrl(avatarUrl)
                }
                
                AuthResult(success = true, message = response.message.ifEmpty { "登录成功" })
            } else {
                AuthResult(success = false, message = response.message.ifEmpty { "登录失败：账号或密码错误" })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult(success = false, message = "网络错误: ${e.message}")
        }
    }

    fun logout() {
        settings.clearTokenBlocking()
    }

    fun isLoggedIn(): Boolean {
        return settings.isLoggedInBlocking()
    }
}