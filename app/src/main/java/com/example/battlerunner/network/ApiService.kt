package com.example.battlerunner.network

import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.data.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {

    @POST("login-info/addLoginInfo") // 서버 경로와 일치하도록 수정
    suspend fun addLoginInfo(@Body loginInfo: LoginInfo): Response<Void>

    @POST("users/addUser") // 서버의 UserController와 맞추기
    suspend fun addUser(@Body user: User): Response<Void>

}



