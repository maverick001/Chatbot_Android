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

class ChatViewModel(private val api: TogetherAIDataSource) : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun addUserMessage(message: String) {
        // Create temporary bot message with loading state
        val tempBotMessage = ChatMessage(
            text = "▋", // Shows loading indicator
            isUser = false
        )
        
        // Update messages list atomically
        _chatMessages.update { messages ->
            messages + ChatMessage(message, true) + tempBotMessage
        }

        viewModelScope.launch {
            try {
                // Capture the index of the temporary message
                val responseIndex = _chatMessages.value.lastIndex
                
                api.streamResponse(message).collect { response ->
                    _chatMessages.update { messages ->
                        messages.toMutableList().apply {
                            // Update only the specific response entry
                            set(responseIndex, ChatMessage(response, false))
                        }
                    }
                }
            } catch (e: Exception) {
                handleError(e)
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

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is SocketTimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Check your connection."
            else -> "Error: ${e.localizedMessage}"
        }
        
        _chatMessages.value = _chatMessages.value.toMutableList().apply {
            if (isNotEmpty() && last().isUser.not()) {
                set(lastIndex, last().copy(text = errorMessage))
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
) 