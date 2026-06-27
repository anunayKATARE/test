package com.cloudtokenchat.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val role: String, val text: String, val isError: Boolean = false)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val apiKey: String = "",
    val model: String = AnthropicClient.DEFAULT_MODEL
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val client = AnthropicClient()

    private val _uiState = MutableStateFlow(
        ChatUiState(
            apiKey = tokenStore.getApiKey() ?: "",
            model = tokenStore.getModel() ?: AnthropicClient.DEFAULT_MODEL
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState

    fun saveSettings(apiKey: String, model: String) {
        tokenStore.saveApiKey(apiKey)
        tokenStore.saveModel(model)
        _uiState.update { it.copy(apiKey = apiKey, model = model) }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val apiKey = _uiState.value.apiKey
        if (apiKey.isBlank()) {
            _uiState.update {
                it.copy(messages = it.messages + ChatMessage("error", "Please enter your API token first.", isError = true))
            }
            return
        }

        val history = _uiState.value.messages.filter { !it.isError }.map { it.role to it.text }
        _uiState.update {
            it.copy(
                messages = it.messages + ChatMessage("user", trimmed),
                isSending = true
            )
        }

        viewModelScope.launch {
            val result = client.sendMessage(
                apiKey = apiKey,
                model = _uiState.value.model,
                history = history,
                newMessage = trimmed
            )
            _uiState.update { state ->
                val message = result.fold(
                    onSuccess = { ChatMessage("assistant", it) },
                    onFailure = { ChatMessage("error", it.message ?: "Something went wrong.", isError = true) }
                )
                state.copy(messages = state.messages + message, isSending = false)
            }
        }
    }
}
