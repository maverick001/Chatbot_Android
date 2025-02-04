import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.example.chatbot3.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson

interface TogetherAIApi {
    @POST("chat/completions")
    suspend fun generateResponse(
        @Header("Authorization") apiKey: String,
        @Body request: TogetherAIRequest
    ): Response
}

data class TogetherAIRequest(
    val model: String,
    val prompt: String,
    val stream_tokens: Boolean = false
)

data class Choice(
    val text: String
)

data class Response(
    val choices: List<Choice>,
    val created: Long,
    val model: String
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
        val response = api.generateResponse(
            apiKey = "Bearer ${BuildConfig.TOGETHER_AI_API_KEY}",
            request = TogetherAIRequest(
                model = "meta-llama/Meta-Llama-3.1-8B-Instruct-Turbo-128K",
                prompt = prompt,
                stream_tokens = false
            )
        )
        emit(response.choices.first().text)
    }
} 