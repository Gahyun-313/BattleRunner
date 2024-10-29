package com.example.battlerunner.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // BASE_URL은 서버의 기본 URL 설정
    private const val BASE_URL = "http://192.168.1.71:8080/"

    // Retrofit 인스턴스 생성 및 API 인터페이스 초기화하여 제공
    val api: ApiService by lazy {

        Retrofit.Builder()
            // 기본 URL 설정
            .baseUrl(BASE_URL)
            // JSON 데이터를 Kotlin 객체로 변환하는 데 사용할 컨버터를 추가
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // ApiService 인터페이스를 구현한 Retrofit API 객체를 생성
            .create(ApiService::class.java)
    }
}

// "http://192.168.1.71:8080/" <집>
