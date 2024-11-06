package com.example.battlerunner.network

import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.data.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {

    @POST("login-info/addLoginInfo")
    suspend fun addLoginInfo(@Body loginInfo: LoginInfo): Response<Void>

    @POST("users/addUser")
    suspend fun addUser(@Body user: User): Response<Void>

}



