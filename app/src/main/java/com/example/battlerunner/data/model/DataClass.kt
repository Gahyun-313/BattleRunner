package com.example.battlerunner.data.model

data class User(
    val userId: String,         // 사용자 아이디
    val username: String,       // 사용자 이름
    val runningTime: Long = 0L, // 현재 세션의 러닝 시간 (밀리초)
    val timestamp: Long = 0L,   // 데이터 생성 또는 마지막 업데이트 시간 (밀리초)
    val distance: Float = 0f,   // 현재 세션의 러닝 거리 (미터)
    val totalDistance: Float = 0f, // 사용자 누적 총 러닝 거리 (미터)
    val totalTime: Float = 0f,  // 사용자 누적 총 러닝 시간 (초 또는 분)
    val averageSpeed: Float = 0f // 사용자 평균 속도 (거리/시간)
)

data class LoginInfo(
    val userId: String,       // 사용자 아이디
    val password: String, // 사용자 비밀번호
    val username: String      // 사용자 이름
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
