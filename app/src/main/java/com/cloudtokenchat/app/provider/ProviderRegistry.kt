package com.cloudtokenchat.app.provider

/**
 * Central catalog of supported [ChatProvider]s. The rest of the app (storage,
 * ViewModel, UI) depends only on the [ChatProvider] abstraction and this
 * registry — supporting a new vendor means adding one class here.
 */
object ProviderRegistry {

    val all: List<ChatProvider> = listOf(
        AnthropicProvider(),
        OpenAiProvider(),
        GeminiProvider()
    )

    val default: ChatProvider = all.first()

    fun get(id: String): ChatProvider = all.find { it.id == id } ?: default
}
