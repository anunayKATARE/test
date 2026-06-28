package com.cloudtokenchat.app.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/** Google Gemini generateContent API (https://ai.google.dev/api/generate-content). */
class GeminiProvider : ChatProvider {

    override val id = "gemini"
    override val displayName = "Google Gemini"
    override val models = listOf(
        "gemini-2.5-flash",
        "gemini-2.5-pro",
        "gemini-2.0-flash"
    )
    override val defaultModel = models.first()

    override suspend fun sendMessage(
        apiKey: String,
        model: String,
        history: List<ChatTurn>,
        newMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val contents = JSONArray()
            history.forEach { turn ->
                contents.put(
                    JSONObject()
                        .put("role", turn.role.toWireRole())
                        .put("parts", JSONArray().put(JSONObject().put("text", turn.text)))
                )
            }
            contents.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", newMessage)))
            )

            val requestBody = JSONObject()
                .put("contents", contents)
                .toString()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
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

                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()
            }
        }
    }

    // Gemini calls the assistant's role "model" rather than "assistant".
    private fun ChatRole.toWireRole() = if (this == ChatRole.USER) "user" else "model"
}
