package com.example.battlerunner.network

import com.example.battlerunner.data.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @POST("/api/user/{userId}/running-record")
    suspend fun updateRunningRecord(
        @Path("userId") userId: String,
        @Query("additionalDistance") additionalDistance: Float,
        @Query("additionalTime") additionalTime: Long
    ): Response<User>

    // 사용자 정보 조회
    @GET("/api/user/{userId}")
    suspend fun getUserInfo(
        @Path("userId") userId: String // 조회할 사용자 ID
    ): Response<User> // 사용자 정보 반환

    // 사용자 ID로 검색
    @GET("/api/user/search")
    suspend fun findUserById(
        @Query("userId") userId: String // 검색할 사용자 ID
    ): Response<User> // 검색된 사용자 정보 반환
}