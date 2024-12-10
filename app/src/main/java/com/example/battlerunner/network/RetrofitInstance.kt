package com.example.battlerunner.network

import com.example.battlerunner.data.model.Battle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://172.20.10.9:8080"

    // Logging interceptor 추가
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 모든 요청과 응답의 상세 로그를 출력
    }

    // OkHttpClient에 loggingInterceptor 추가
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .enableComplexMapKeySerialization() // Map 키를 Int로 처리 가능
        .create()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    val loginApi: LoginApi by lazy {
        retrofit.create(LoginApi::class.java)
    }

    val userApi: UserApi by lazy{
        retrofit.create(UserApi::class.java)
    }

    val battleApi: BattleApi by lazy {
        retrofit.create(BattleApi::class.java)
    }
}

