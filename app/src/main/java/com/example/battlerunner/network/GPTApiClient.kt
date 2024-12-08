package com.example.battlerunner.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Request Model
data class GPTRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

// Response Model
data class GPTResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// Retrofit API Service
interface GPTApiService {
    @Headers("Authorization: Bearer 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", "Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun getRecommendation(@Body request: GPTRequest): GPTResponse
}

object GPTApiClient {
    private const val BASE_URL = "https://api.openai.com/"

    val instance: GPTApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GPTApiService::class.java)
    }
}
