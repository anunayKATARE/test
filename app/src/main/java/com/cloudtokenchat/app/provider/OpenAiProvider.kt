package com.cloudtokenchat.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/** OpenAI Chat Completions API (https://api.openai.com/v1/chat/completions). */
class OpenAiProvider : ChatProvider {

    override val id = "openai"
    override val displayName = "OpenAI ChatGPT"
    override val models = listOf(
        "gpt-4o",
        "gpt-4o-mini",
        "gpt-4.1"
    )
    override val defaultModel = models.first()

    override suspend fun sendMessage(
        apiKey: String,
        model: String,
        history: List<ChatTurn>,
        newMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val messages = JSONArray()
            history.forEach { turn ->
                messages.put(
                    JSONObject()
                        .put("role", turn.role.toWireRole())
                        .put("content", turn.text)
                )
            }
            messages.put(JSONObject().put("role", "user").put("content", newMessage))

            val requestBody = JSONObject()
                .put("model", model)
                .put("messages", messages)
                .toString()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("content-type", "application/json")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            NetworkClient.client.newCall(request).execute().use { response ->
                val responseText = response.body?.string().orEmpty()
                val json = parseJsonObject(responseText)

                if (!response.isSuccessful) {
                    val errorMessage = json.optJSONObject("error")?.optString("message")
                        ?: "Request failed with status ${response.code}."
                    throw IOException(errorMessage)
                }

                json.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    .orEmpty()
            }
        }
    }

    private fun ChatRole.toWireRole() = if (this == ChatRole.USER) "user" else "assistant"
}
