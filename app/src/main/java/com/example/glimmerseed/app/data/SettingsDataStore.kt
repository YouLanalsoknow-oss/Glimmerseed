package com.example.glimmerseed.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// DataStore 单例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "glimmerseed_settings")

class SettingsDataStore private constructor(private val context: Context) {

    companion object {
        private val KEY_API_KEY = stringPreferencesKey("ai_api_key")
        private val KEY_MODEL_ID = stringPreferencesKey("ai_model_id")
        private val KEY_API_URL = stringPreferencesKey("ai_api_url")
        private val KEY_API_FORMAT = stringPreferencesKey("ai_api_format")

        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_AVATAR_URL = stringPreferencesKey("avatar_url")
        private val KEY_DEVICE_ID = stringPreferencesKey("device_id")

        const val DEFAULT_MODEL_ID = "deepseek-ai/DeepSeek-R1-0528-Qwen3-8B"
        const val DEFAULT_API_URL = "https://api.siliconflow.cn/v1"
        const val FORMAT_OPENAI = "openai"
        const val FORMAT_SILICONFLOW = "siliconflow"
        const val DEFAULT_FORMAT = FORMAT_OPENAI

        @Volatile
        private var instance: SettingsDataStore? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = SettingsDataStore(context.applicationContext)
                runLegacyMigration(context.applicationContext)
            }
        }

        fun getInstance(): SettingsDataStore {
            return instance ?: error("SettingsDataStore not initialized! Call init() first.")
        }

        private fun runLegacyMigration(appContext: Context) {
            // 暂时禁用旧版迁移，避免依赖问题
        }

        fun createForTest(context: Context): SettingsDataStore {
            return SettingsDataStore(context)
        }
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_API_KEY] ?: ""
    }

    val modelIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_MODEL_ID] ?: DEFAULT_MODEL_ID
    }

    val apiUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        (preferences[KEY_API_URL] ?: DEFAULT_API_URL).trim()
    }

    val apiFormatFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_API_FORMAT] ?: DEFAULT_FORMAT
    }

    val tokenFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOKEN] ?: ""
    }

    val userIdFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ID] ?: -1L
    }

    val usernameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USERNAME] ?: ""
    }

    val emailFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_EMAIL] ?: ""
    }

    val avatarUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_AVATAR_URL] ?: ""
    }

    val deviceIdFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEVICE_ID] ?: ""
    }

    val isLoggedInFlow: Flow<Boolean> = tokenFlow.map { it.isNotEmpty() }

    val isConfiguredFlow: Flow<Boolean> = apiKeyFlow.map { key ->
        key.isNotEmpty()
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_KEY] = key
        }
    }

    suspend fun saveModelId(modelId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MODEL_ID] = modelId
        }
    }

    suspend fun saveApiUrl(url: String) {
        val cleanedUrl = url.trim().trimEnd('/', ',', ' ')
        context.dataStore.edit { preferences ->
            preferences[KEY_API_URL] = cleanedUrl
        }
    }

    suspend fun saveFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_FORMAT] = format
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOKEN] = token
        }
    }

    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
        }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_EMAIL] = email
        }
    }

    suspend fun saveAvatarUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AVATAR_URL] = url
        }
    }

    suspend fun saveDeviceId(deviceId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEVICE_ID] = deviceId
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USERNAME)
            preferences.remove(KEY_AVATAR_URL)
        }
    }

    suspend fun getApiKey(): String = apiKeyFlow.first()
    suspend fun getModelId(): String = modelIdFlow.first()
    suspend fun getApiUrl(): String = apiUrlFlow.first()
    suspend fun getFormat(): String = apiFormatFlow.first()
    suspend fun getToken(): String = tokenFlow.first()
    suspend fun getUserId(): Long = userIdFlow.first()
    suspend fun getUsername(): String = usernameFlow.first()
    suspend fun getEmail(): String = emailFlow.first()
    suspend fun getAvatarUrl(): String = avatarUrlFlow.first()
    suspend fun getDeviceId(): String = deviceIdFlow.first()
    suspend fun isLoggedIn(): Boolean = isLoggedInFlow.first()
    suspend fun isConfigured(): Boolean = isConfiguredFlow.first()

    // 阻塞版本的 API
    fun getApiKeyBlocking(): String = runBlocking { getApiKey() }
    fun getModelIdBlocking(): String = runBlocking { getModelId() }
    fun getApiUrlBlocking(): String = runBlocking { getApiUrl() }
    fun getFormatBlocking(): String = runBlocking { getFormat() }
    fun getTokenBlocking(): String = runBlocking { getToken() }
    fun getUserIdBlocking(): Long = runBlocking { getUserId() }
    fun getUsernameBlocking(): String = runBlocking { getUsername() }
    fun getEmailBlocking(): String = runBlocking { getEmail() }
    fun getAvatarUrlBlocking(): String = runBlocking { getAvatarUrl() }
    fun getDeviceIdBlocking(): String = runBlocking { getDeviceId() }
    fun isLoggedInBlocking(): Boolean = runBlocking { isLoggedIn() }
    fun isConfiguredBlocking(): Boolean = runBlocking { isConfigured() }

    fun saveTokenBlocking(token: String) = runBlocking { saveToken(token) }
    fun saveUserIdBlocking(userId: Long) = runBlocking { saveUserId(userId) }
    fun saveUsernameBlocking(username: String) = runBlocking { saveUsername(username) }
    fun saveAvatarUrlBlocking(url: String) = runBlocking { saveAvatarUrl(url) }
    fun clearTokenBlocking() = runBlocking { clearToken() }
    fun saveApiKeyBlocking(key: String) = runBlocking { saveApiKey(key) }
    fun saveModelIdBlocking(modelId: String) = runBlocking { saveModelId(modelId) }
    fun saveApiUrlBlocking(url: String) = runBlocking { saveApiUrl(url) }
    fun saveFormatBlocking(format: String) = runBlocking { saveFormat(format) }
}
