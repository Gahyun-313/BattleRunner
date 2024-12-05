package com.example.battlerunner.network

import com.example.battlerunner.data.model.ApiResponse
import com.example.battlerunner.data.model.Battle
import com.example.battlerunner.data.model.GridOwnershipMapResponse
import com.example.battlerunner.data.model.GridOwnershipUpdateRequest
import com.example.battlerunner.data.model.GridStartLocationRequest
import com.example.battlerunner.data.model.GridStartLocationResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface BattleApi {

    // 배틀 생성 (생성 시 BattleId, gridStartLat, gridStartLng -> Null)
    // (BattleId: 서버에서 자동 생성, Lat, Lng: 그리드 그릴 때 정해짐)
    @POST("/api/battle/create")
    suspend fun createBattle(
        @Body battle: Battle
    ): Response<Battle>

    // 특정 배틀 조회 (battleId로)
    @GET("/api/battle/{battleId}/get")
    fun getBattleById(
        @Path("battleId") battleId: Long
    ): Call<Battle>

    // 배틀 업데이트 (battleId로)
    @POST("/api/battle/{battleId}/update")
    fun updateBattle(
        @Path("battleId") battleId: Long,
        @Body battle: Battle
    ): Call<Battle>

    // 특정 사용자의 배틀 리스트 조회 (userId로)
    @GET("/api/battle/user/{userId}")
    fun getBattlesByUserId(
        @Path("userId") userId: String
    ): Call<List<Battle>>

    // 배틀 시작 위치를 서버로 전송
    @POST("/api/battle/{battleId}/poststartLocation")
    fun setGridStartLocation(
        @Path("battleId") battleId: Long,
        @Body startLocationRequest: GridStartLocationRequest
    ): Call<ApiResponse>


    // 배틀 시작 위치를 서버에서 가져오기. 시작위치만 가져다가 쓸 거라 필요함.
    @GET("/api/battles/{battleId}/getstartLocation")
    fun getGridStartLocation(
        @Path("battleId") battleId: Long
    ): Call<GridStartLocationResponse>

    // 소유권 업데이트. battlegrid 테이블 업데이트
    @PUT("/api/grid/{battleId}/{gridId}/update")
    fun updateGridOwnership(
        @Path("battleId") battleId: Long,
        @Path("gridId") gridId: String,
        @Query("userId") userId: String // 소유자 ID를 쿼리 파라미터로 전달
        // @Body request: GridOwnershipUpdateRequest
    ): Call<ApiResponse>

    // 소유권 가져오기.
    @GET("/api/grid/{battleId}/grid/ownership")
    fun getGridOwnership(
        @Path("battleId") battleId: Long
    ): Call<GridOwnershipMapResponse>

    // 배틀 삭제 (battleId로)
    @DELETE("/api/battle/{battleId}/delete")
    fun deleteBattle(
        @Path("battleId") battleId: Long
    ): Call<Void>
}