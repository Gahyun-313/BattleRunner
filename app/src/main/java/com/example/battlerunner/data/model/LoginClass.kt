package com.example.battlerunner.data.model

data class User(
    val userId: String,
    val username: String,
    val runningTime: Long = 0L,
    val timestamp: Long = 0L,
    val distance: Float = 0f,
    val totalDistance: Float = 0f,
    val totalTime: Float = 0f,
    val averageSpeed: Float = 0f
)

//data class LoginInfo(
//    val userId: String,
//    val password: String,
//    val username: String
//)


data class LoginInfo(
    val userId: String,
    val userPassword: String,
    val username: String
)


//배틀 상대 표현을 위해 사용자1,2로 구분
data class Battle(
    val battleId: Long,
    val ranking: Int,
    val flags: Int,
    val isBattleStarted: Boolean,
    val user1Username: String,   //사용자 1
    val user2Username: String    //사용자 2
)
