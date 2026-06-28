package com.cloudtokenchat.app.provider

/** A single turn of prior conversation, in provider-agnostic form. */
enum class ChatRole { USER, ASSISTANT }

data class ChatTurn(val role: ChatRole, val text: String)

/**
 * Abstraction over a single LLM vendor's chat API. Adding a new vendor means
 * implementing this interface and registering it in [ProviderRegistry] — no
 * other code (ViewModel, UI, storage) needs to change.
 */
interface ChatProvider {
    val id: String
    val displayName: String
    val models: List<String>
    val defaultModel: String

    suspend fun sendMessage(
        apiKey: String,
        model: String,
        history: List<ChatTurn>,
        newMessage: String
    ): Result<String>
}
