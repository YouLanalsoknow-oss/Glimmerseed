package com.example.glimmerseed.app.data

import kotlinx.coroutines.runBlocking

/**
 * 已弃用的 TokenManager
 * 保留文件以避免破坏旧代码，所有功能已迁移到 SettingsDataStore
 */
@Deprecated("Use SettingsDataStore instead", ReplaceWith("SettingsDataStore"))
object TokenManager {
    private val settingsDataStore: SettingsDataStore?
        get() {
            return try {
                SettingsDataStore.getInstance()
            } catch (e: Exception) {
                null
            }
        }

    fun getToken(): String? {
        return try {
            runBlocking { settingsDataStore?.getToken()?.takeIf { it.isNotEmpty() } }
        } catch (e: Exception) {
            null
        }
    }

    fun saveToken(token: String) {
        runBlocking { settingsDataStore?.saveToken(token) }
    }

    fun clearToken() {
        runBlocking { settingsDataStore?.clearToken() }
    }

    fun getUserId(): Long? {
        return try {
            runBlocking { 
                settingsDataStore?.getUserId()?.takeIf { it != -1L } 
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveUserId(userId: Long) {
        runBlocking { settingsDataStore?.saveUserId(userId) }
    }

    fun getUsername(): String? {
        return try {
            runBlocking { 
                settingsDataStore?.getUsername()?.takeIf { it.isNotEmpty() } 
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveUsername(username: String) {
        runBlocking { settingsDataStore?.saveUsername(username) }
    }

    fun getEmail(): String? {
        return try {
            runBlocking { 
                settingsDataStore?.getEmail()?.takeIf { it.isNotEmpty() } 
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveEmail(email: String) {
        runBlocking { settingsDataStore?.saveEmail(email) }
    }

    fun getAvatarUrl(): String? {
        return try {
            runBlocking { 
                settingsDataStore?.getAvatarUrl()?.takeIf { it.isNotEmpty() } 
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveAvatarUrl(url: String) {
        runBlocking { settingsDataStore?.saveAvatarUrl(url) }
    }

    fun isLoggedIn(): Boolean {
        return try {
            runBlocking { settingsDataStore?.isLoggedIn() ?: false }
        } catch (e: Exception) {
            false
        }
    }
}
