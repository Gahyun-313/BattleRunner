package com.example.battlerunner.data.model

data class LoginInfo(
    val userId: String, // 변수명 변경 : user_id -> userID
    val password: String,
    val name: String,
    val loginType: String // (추가) 로그인 타입
)

data class User(
    val userId: String,         // 사용자 아이디
    val username: String,       // 사용자 이름
    val runningTime: Long = 0L, // 현재 세션의 러닝 시간 (밀리초)
    val timestamp: Long = 0L,   // 데이터 생성 또는 마지막 업데이트 시간 (밀리초)
    val distance: Float = 0f,   // 현재 세션의 러닝 거리 (미터)
    val totalDistance: Float = 0f, // 사용자 누적 총 러닝 거리 (미터)
    val totalTime: Float = 0f,  // 사용자 누적 총 러닝 시간 (초 또는 분)
    val averageSpeed: Float = 0f, // 사용자 평균 속도 (거리/시간)

    val profileImageResId: Int // 프로필 사진 <배틀 매칭>
)

//배틀 상대 표현을 위해 사용자1,2로 구분
data class Battle(
    val battleId: Long,
    val ranking: Int,
    //val flags: Int,
    val isBattleStarted: Boolean,
    val user1Username: String,  // 사용자 1 (신청 한 사람)
    val user2Username: String,  // 사용자 2 (신청 받은 사람)

    val gridStartLat: Double,   // 그리드 시작(중심) 위도
    val gridStartLng: Double    // 그리드 시작(중심) 경도
)

// 배틀 기록 저장
data class BattleRecord(
    val date: String,
    val opponentName: String,
    val imagePath: String
    // val result: String
)