package com.example.battlerunner.network

import com.example.battlerunner.data.model.LoginInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("api/login/register")
    fun addLoginInfo(@Body loginInfo: LoginInfo): Call<LoginInfo>
}
