package com.example.battlerunner.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // BASE_URL은 서버의 기본 URL 설정
    // TODO URL 설정
    private const val BASE_URL = " "

    // Retrofit 인스턴스 생성 및 API 인터페이스 초기화하여 제공
    val api: ApiService by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

