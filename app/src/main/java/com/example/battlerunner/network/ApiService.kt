package com.example.battlerunner.network

import com.example.battlerunner.data.model.LoginInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {

    @POST("uploadLoginInfo")
    suspend fun uploadLoginInfo(@Body loginInfo: LoginInfo): Response<Any>

}



