package com.example.battlerunner.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {



// 예를 들어 서버에 러닝 데이터를 전송하는 경우
//    @POST("uploadRunningData") // POST 요청
//    suspend fun uploadRunningData(@Body runningData: RunningData): Response<Any>
// *RunningData는 서버로 보낼 데이터를 담고 있는 클래스임
// *ex-> data class RunningData(  val distance: Double, val time: Long    )

}