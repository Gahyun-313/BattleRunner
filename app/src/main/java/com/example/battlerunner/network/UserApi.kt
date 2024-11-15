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

    @GET("/api/user/{userId}")
    suspend fun getUserInfo(@Path("userId") userId: String): Response<User>
}