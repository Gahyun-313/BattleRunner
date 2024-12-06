package com.example.battlerunner.network

import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.data.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LoginApi {

    // 회원가입 요청
    @POST("api/login/register")
    fun register(@Body loginInfo: LoginInfo): Call<LoginInfo>

    // 로그인 요청
    @POST("api/login/login")
    fun login(@Body loginInfo: LoginInfo): Call<LoginInfo>

    // 특정 사용자의 LoginInfo 조회
    @GET("api/login/{userId}")
    fun getLoginInfoById(@Path("userId") userId: String): Call<LoginInfo>

    // ID 중복 확인
    @POST("/api/login/check-duplicate")
    suspend fun checkDuplicateUserId(@Body userId: String): Boolean

    // ID로 사용자 검색
    @GET("/api/login/{userId}")
    suspend fun findUserById(@Path("userId") userId: String): User?

    // 모든 사용자 조회
    @GET("/api/login/all")
    suspend fun getAllUsers(): List<User>
}