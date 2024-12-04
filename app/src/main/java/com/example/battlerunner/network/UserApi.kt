package com.example.battlerunner.network

import com.example.battlerunner.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    //user 데이터 업데이트
    @POST("/api/user/{userId}/running-record")
    suspend fun updateRunningRecord(
        @Path("userId") userId: String,
        @Body userDto: User
    ): Response<Void>

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
    //    // userId로 특정 user 데이터 전체 가져오기
//    @GET("/api/user/{userId}")
//    suspend fun getUserInfo(@Path("userId") userId: String): Response<UserResponse>




//    // userId로 특정 user 데이터 전체 가져오기
//    @GET("/api/user/{userId}")
//    suspend fun getUserInfo(@Path("userId") userId: String): Response<UserResponse>
//}