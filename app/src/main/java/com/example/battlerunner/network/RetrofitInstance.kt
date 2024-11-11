package com.example.battlerunner.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // BASE_URL은 서버의 기본 URL 설정
    // TODO URL 설정
    private const val BASE_URL = "http://192.168.1.71:8080/"

    // Logging interceptor 추가
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 모든 요청과 응답의 상세 로그를 출력
    }

    // OkHttpClient에 loggingInterceptor 추가
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit 인스턴스 생성
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // OkHttpClient 추가
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

