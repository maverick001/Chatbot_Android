package com.example.chatbot3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import com.example.chatbot3.BuildConfig
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.flow.StateFlow
import com.example.chatbot3.TogetherAIDataSource
import java.util.UUID

class ChatViewModel(private val api: TogetherAIDataSource) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun addUserMessage(message: String) {
        // Add user message and temporary bot message
        _chatMessages.update { messages ->
            messages + listOf(
                ChatMessage(text = message, isUser = true),
                ChatMessage(text = "▋", isUser = false)
            )
        }
        
        val responseIndex = _chatMessages.value.lastIndex

        viewModelScope.launch {
            try {
                api.streamResponse(message).collect { response ->
                    _chatMessages.update { messages ->
                        messages.toMutableList().apply {
                            set(responseIndex, ChatMessage(text = response, isUser = false))
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Request timed out"
                    is IOException -> "Network error"
                    else -> "Error: ${e.localizedMessage}"
                }
                _chatMessages.update { messages ->
                    messages.toMutableList().apply {
                        set(responseIndex, ChatMessage(text = errorMessage, isUser = false))
                    }
                }
            }
        }
    }

    fun addBotResponse(response: String) {
        _chatMessages.value = _chatMessages.value + ChatMessage(
            text = response,
            isUser = false
        )
    }

    private fun updateLastBotResponse(newText: String) {
        _chatMessages.value = _chatMessages.value.toMutableList().apply {
            if (isNotEmpty() && last().isUser.not()) {
                set(lastIndex, last().copy(text = last().text + newText))
            }
        }
    }

    private fun handleError(e: Exception, messageId: String) {
        val errorMessage = when (e) {
            is SocketTimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Check your connection."
            else -> "Error: ${e.localizedMessage}"
        }
        
        _chatMessages.update { messages ->
            messages.map { msg ->
                if (msg.id == messageId) msg.copy(text = errorMessage, isLoading = false)
                else msg
            }
        }
    }

    fun clearAllMessages() {
        _chatMessages.value = emptyList()
    }
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
) 