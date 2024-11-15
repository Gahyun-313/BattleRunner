//OpenAIRetrofitInstance
package com.example.battlerunner.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenAIRetrofitInstance {
    private const val BASE_URL = "https://api.openai.com/"

    val api: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }
}
