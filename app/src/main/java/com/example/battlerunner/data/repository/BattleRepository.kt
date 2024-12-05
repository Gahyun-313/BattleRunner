package com.example.battlerunner.data.repository

import com.example.battlerunner.data.model.ApiResponse
import com.example.battlerunner.data.model.Battle
import com.example.battlerunner.data.model.GridOwnershipMapResponse
import com.example.battlerunner.data.model.GridOwnershipUpdateRequest
import com.example.battlerunner.data.model.GridStartLocationRequest
import com.example.battlerunner.data.model.GridStartLocationResponse
import com.example.battlerunner.network.BattleApi
import retrofit2.Call
import retrofit2.Response

class BattleRepository(private val battleApi: BattleApi) {

    // 배틀 생성
    suspend fun createBattle(battleDto: Battle): Response<Battle> {
        return battleApi.createBattle(battleDto)
    }

    // 특정 배틀 조회
    fun getBattleById(battleId: Long): Call<Battle> {
        return battleApi.getBattleById(battleId)
    }

    // 특정 사용자의 배틀 리스트 조회
    fun getBattlesByUserId(userId: String): Call<List<Battle>> {
        return battleApi.getBattlesByUserId(userId)
    }

    // 배틀 업데이트
    fun updateBattle(battleId: Long, battle: Battle): Call<Battle> {
        return battleApi.updateBattle(battleId, battle)
    }

    // 배틀 삭제
    fun deleteBattle(battleId: Long): Call<Void> {
        return battleApi.deleteBattle(battleId)
    }

    fun getGridStartLocation(battleId: Long): Call<GridStartLocationResponse> {
        return battleApi.getGridStartLocation(battleId)
    }

    fun setGridStartLocation(
        battleId: Long,
        startLocationRequest: GridStartLocationRequest
    ): Call<ApiResponse> {
        return battleApi.setGridStartLocation(battleId, startLocationRequest)
    }

    fun updateGridOwnership(
        battleId: Long,
        gridId: String,
        userId: String
    ): Call<ApiResponse> {
        return battleApi.updateGridOwnership(battleId, gridId, userId)
    }

    fun getGridOwnership(battleId: Long): Call<GridOwnershipMapResponse> {
        return battleApi.getGridOwnership(battleId)
    }
}
