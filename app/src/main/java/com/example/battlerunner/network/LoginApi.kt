package com.example.battlerunner.network

import com.example.battlerunner.data.model.LoginInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LoginApi {
    @POST("api/login/register")
    fun addLoginInfo(@Body loginInfo: LoginInfo): Call<LoginInfo>

    @POST("api/login/login")
    fun login(@Body loginInfo: LoginInfo): Call<LoginInfo>

    // userId를 경로 매개변수로 받아 특정 사용자의 LoginInfo를 조회하는 메서드
    @GET("api/login/{userId}")
    fun getLoginInfoById(@Path("userId") userId: String): Call<LoginInfo>
}