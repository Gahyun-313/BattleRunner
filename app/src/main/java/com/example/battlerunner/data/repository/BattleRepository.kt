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
}
