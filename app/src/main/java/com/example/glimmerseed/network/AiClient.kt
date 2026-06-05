package com.example.glimmerseed.network

import com.example.glimmerseed.app.data.SettingsDataStore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.util.concurrent.TimeUnit

data class AiChatMessage(
    val role: String,
    val content: String
)

data class AiChatRequest(
    val model: String,
    val messages: List<AiChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2048
)

data class AiChoice(
    val message: AiChatMessage?
)

data class AiUsage(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)

data class AiChatResponse(
    val id: String? = null,
    val choices: List<AiChoice> = emptyList(),
    val usage: AiUsage? = null,
    val error: Map<String, Any>? = null
)

object AiClient {
    private const val TAG = "AiClient"
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(
        petName: String,
        petPersonality: String,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val settings = SettingsDataStore.getInstance()
            val apiKey = settings.getApiKeyBlocking().trim()
            val modelId = settings.getModelIdBlocking().trim()
            val apiUrl = settings.getApiUrlBlocking()
            val format = settings.getFormatBlocking()

            if (apiKey.isEmpty()) {
                return@withContext Result.failure(Exception("请先在设置中配置 API Key"))
            }

            Timber.tag(TAG).d("AI Config - URL: $apiUrl, Model: $modelId, Format: $format")
            Timber.tag(TAG).d("API Key (first 10): ${apiKey.take(10)}...")

            val systemPrompt = buildSystemPrompt(petName, petPersonality)
            val messages = listOf(
                AiChatMessage("system", systemPrompt),
                AiChatMessage("user", userMessage)
            )

            val requestPayload = AiChatRequest(
                model = modelId,
                messages = messages
            )

            val jsonBody = gson.toJson(requestPayload)
                .toRequestBody("application/json".toMediaType())

            val endpoint = when (format) {
                SettingsDataStore.FORMAT_SILICONFLOW -> "$apiUrl/chat/completions"
                else -> "$apiUrl/chat/completions"
            }

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()

            Timber.tag(TAG).d("AI Request: $endpoint model=$modelId")

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Timber.tag(TAG).e("AI Error ${response.code}: $responseBody")
                return@withContext Result.failure(Exception("AI 接口错误 (${response.code}): $responseBody"))
            }

            val aiResponse = gson.fromJson(responseBody, AiChatResponse::class.java)

            aiResponse.error?.let {
                val errorMsg = it["message"]?.toString() ?: it.toString()
                return@withContext Result.failure(Exception("AI 返回错误: $errorMsg"))
            }

            val reply = aiResponse.choices.firstOrNull()?.message?.content
                ?: return@withContext Result.failure(Exception("AI 返回空回复"))

            Timber.tag(TAG).d("AI Reply (${reply.length} chars): ${reply.take(80)}...")
            Result.success(reply)
        }.getOrElse { e ->
            Timber.tag(TAG).e(e, "AiClient exception")
            Result.failure(e)
        }
    }

    private fun buildSystemPrompt(petName: String, personalityPrompt: String): String {
        val prompt = personalityPrompt.ifEmpty { "可爱、温暖、略带俏皮的虚拟宠物" }
        return """你是一个名为「$petName」的桌宠。
角色设定：$prompt
请严格遵循角色设定进行回复，就像一个小宠物在回应主人。
保持回复简短，控制在100字以内，可以适当使用emoji表情。
如果主人问关于你自己的事情（名字、外貌、性格等），请根据角色设定回答。"""
    }
}