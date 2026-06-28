package com.cloudtokenchat.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cloudtokenchat.app.provider.ChatRole
import com.cloudtokenchat.app.provider.ChatTurn
import com.cloudtokenchat.app.provider.ProviderRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val text: String, val isError: Boolean = false)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val providerId: String = ProviderRegistry.default.id,
    val model: String = ProviderRegistry.default.defaultModel,
    val apiKey: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private fun loadInitialState(): ChatUiState {
        val provider = ProviderRegistry.get(tokenStore.getSelectedProviderId() ?: ProviderRegistry.default.id)
        return ChatUiState(
            providerId = provider.id,
            model = tokenStore.getModel(provider.id) ?: provider.defaultModel,
            apiKey = tokenStore.getApiKey(provider.id) ?: ""
        )
    }

    /** Looks up the stored API key/model for [providerId] without changing current chat state. */
    fun credentialsFor(providerId: String): Pair<String, String> {
        val provider = ProviderRegistry.get(providerId)
        return (tokenStore.getApiKey(provider.id) ?: "") to (tokenStore.getModel(provider.id) ?: provider.defaultModel)
    }

    fun saveSettings(providerId: String, apiKey: String, model: String) {
        tokenStore.saveSelectedProviderId(providerId)
        tokenStore.saveApiKey(providerId, apiKey)
        tokenStore.saveModel(providerId, model)
        _uiState.update { it.copy(providerId = providerId, apiKey = apiKey, model = model) }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val state = _uiState.value
        if (state.apiKey.isBlank()) {
            _uiState.update {
                it.copy(messages = it.messages + ChatMessage("error", "Please enter your API key first.", isError = true))
            }
            return
        }

        val provider = ProviderRegistry.get(state.providerId)
        val history = state.messages.filter { !it.isError }.map { message ->
            ChatTurn(if (message.role == "user") ChatRole.USER else ChatRole.ASSISTANT, message.text)
        }

        _uiState.update {
            it.copy(messages = it.messages + ChatMessage("user", trimmed), isSending = true)
        }

        viewModelScope.launch {
            val result = provider.sendMessage(
                apiKey = state.apiKey,
                model = state.model,
                history = history,
                newMessage = trimmed
            )
            _uiState.update { s ->
                val message = result.fold(
                    onSuccess = { ChatMessage("assistant", it) },
                    onFailure = { ChatMessage("error", it.message ?: "Something went wrong.", isError = true) }
                )
                s.copy(messages = s.messages + message, isSending = false)
            }
        }
    }
}
