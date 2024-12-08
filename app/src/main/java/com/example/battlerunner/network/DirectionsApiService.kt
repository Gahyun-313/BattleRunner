package com.example.battlerunner.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface DirectionsApiService {
    @GET("map-direction/v1/driving") // 정확한 경로 확인
    fun getWalkingRoute(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String, // 경도, 위도 순
        @Query("goal") goal: String    // 경도, 위도 순
    ): Call<DirectionsResponse>
}


