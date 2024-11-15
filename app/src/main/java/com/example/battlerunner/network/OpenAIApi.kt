//OpenAIAPi
package com.example.battlerunner.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApi {
    @Headers("Authorization: Bearer YOUR_OPENAI_API_KEY", "Content-Type: application/json")
    @POST("v1/completions")
    fun getRouteSuggestion(@Body request: OpenAIRequest): Call<OpenAIResponse>
}

data class OpenAIRequest(
    @SerializedName("model") val model: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    @SerializedName("temperature") val temperature: Float
)

data class OpenAIResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("text") val text: String
)
