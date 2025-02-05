import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import com.example.chatbot3.BuildConfig

class ChatViewModel : ViewModel() {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val api = TogetherAIDataSource()

    fun addUserMessage(message: String) {
        _chatMessages.value = _chatMessages.value + ChatMessage(
            text = message,
            isUser = true
        )
        
        _chatMessages.value = _chatMessages.value + ChatMessage(
            text = "",
            isUser = false
        )

        viewModelScope.launch {
            api.streamResponse(message).collect { response ->
                updateLastBotResponse(response)
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
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
) 