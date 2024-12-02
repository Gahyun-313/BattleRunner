package com.example.battlerunner.network

import com.example.battlerunner.data.model.ApiResponse
import com.example.battlerunner.data.model.Battle
import com.example.battlerunner.data.model.GridOwnershipMapResponse
import com.example.battlerunner.data.model.GridOwnershipUpdateRequest
import com.example.battlerunner.data.model.GridStartLocationRequest
import com.example.battlerunner.data.model.GridStartLocationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BattleApi {
    // 배틀 신청 요청
    @GET("/api/battle/request")
    suspend fun requestBattle(
        @Query("user1Id") user1Id: String, // 신청자 ID
        @Query("user2Id") user2Id: String  // 상대방 ID
    ): Battle

    // 시작 위치를 서버에서 가져오기
    @GET("battle/grid/startLocation")
    fun getGridStartLocation(@Query("battleId") battleId: String): Call<GridStartLocationResponse>

    // 시작 위치를 서버로 전송
    @POST("battle/grid/startLocation")
    fun setGridStartLocation(@Body startLocationRequest: GridStartLocationRequest): Call<ApiResponse>

    // 소유권 업데이트
    @POST("/api/battle/{battleId}/update")
    fun updateGridOwnership(
        @Body request: GridOwnershipUpdateRequest
    ): Call<ApiResponse>

    // 소유권 가져오기
    @GET("battle/grid/ownership")
    fun getGridOwnership(@Query("battleId") battleId: String): Call<GridOwnershipMapResponse>
}
