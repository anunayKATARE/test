package com.cloudtokenchat.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AnthropicClient {

    companion object {
        const val DEFAULT_MODEL = "claude-sonnet-4-6"
        val MODELS = listOf("claude-sonnet-4-6", "claude-opus-4-8", "claude-haiku-4-5-20251001")
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val ANTHROPIC_VERSION = "2023-06-01"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>,
        newMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val messages = JSONArray()
            history.forEach { (role, text) ->
                messages.put(JSONObject().put("role", role).put("content", text))
            }
            messages.put(JSONObject().put("role", "user").put("content", newMessage))

            val requestBody = JSONObject()
                .put("model", model)
                .put("max_tokens", 1024)
                .put("messages", messages)
                .toString()

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", ANTHROPIC_VERSION)
                .addHeader("content-type", "application/json")
                .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                val json = try {
                    JSONObject(responseText)
                } catch (e: Exception) {
                    JSONObject()
                }

                if (!response.isSuccessful) {
                    val errorMessage = json.optJSONObject("error")?.optString("message")
                        ?: "Request failed with status ${response.code}."
                    throw IOException(errorMessage)
                }

                val content = json.optJSONArray("content") ?: JSONArray()
                buildString {
                    for (i in 0 until content.length()) {
                        append(content.optJSONObject(i)?.optString("text").orEmpty())
                    }
                }
            }
        }
    }
}
