package com.cloudtokenchat.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/** Anthropic Messages API (https://api.anthropic.com/v1/messages). */
class AnthropicProvider : ChatProvider {

    override val id = "anthropic"
    override val displayName = "Anthropic Claude"
    override val models = listOf(
        "claude-sonnet-4-6",
        "claude-opus-4-8",
        "claude-haiku-4-5-20251001"
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
                .put("max_tokens", 1024)
                .put("messages", messages)
                .toString()

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
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

                val content = json.optJSONArray("content") ?: JSONArray()
                buildString {
                    for (i in 0 until content.length()) {
                        append(content.optJSONObject(i)?.optString("text").orEmpty())
                    }
                }
            }
        }
    }

    private fun ChatRole.toWireRole() = if (this == ChatRole.USER) "user" else "assistant"
}
