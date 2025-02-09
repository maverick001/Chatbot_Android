package com.example.chatbot3

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.example.chatbot3.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import retrofit2.http.Streaming
import okhttp3.ResponseBody
import retrofit2.Response

interface TogetherAIApi {
    @POST("chat/completions")
    suspend fun generateResponse(
        @Header("Authorization") apiKey: String,
        @Body request: TogetherAIRequest
    ): Response<ChatResponse>

    @Streaming
    @POST("chat/completions")
    suspend fun generateStreamingResponse(
        @Header("Authorization") apiKey: String,
        @Body request: TogetherAIRequest
    ): Response<ResponseBody>
}

data class ChatResponse(
    val choices: List<Choice>
)

data class TogetherAIRequest(
    val model: String,
    val prompt: String,
    val stream_tokens: Boolean = false,
    val max_tokens: Int = 100
)

data class Choice(
    val message: Message
)

data class Message(
    val content: String
)

data class Response(
    val choices: List<Choice>,
    val created: Long,
    val model: String
)

data class StreamResponse(
    val choices: List<StreamChoice>
)

data class StreamChoice(
    val delta: Delta
)

data class Delta(
    val content: String
)

class TogetherAIDataSource {
    private val api: TogetherAIApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.together.xyz/v1/")
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TogetherAIApi::class.java)
    }

    fun streamResponse(prompt: String): Flow<String> = flow {
        // Add system prompt to user's prompt
        val systemPrompt = """You are a helpful AI assistant. Keep your responses clear and concise, always within 150 tokens. 
            |Avoid repetition and unnecessary details. Focus on providing direct, informative answers.
            |
            |User: """.trimMargin()
        
        val fullPrompt = systemPrompt + prompt

        val response = api.generateResponse(
            apiKey = "Bearer ${BuildConfig.TOGETHER_AI_API_KEY}",
            request = TogetherAIRequest(
                model = "meta-llama/Meta-Llama-3-8B-Instruct-Lite",
                prompt = fullPrompt,
                max_tokens = 150  // Keep this as a hard limit
            )
        )
        
        response.body()?.let { chatResponse ->
            emit(chatResponse.choices.first().message.content)
        }
    }.flowOn(Dispatchers.IO)
} 