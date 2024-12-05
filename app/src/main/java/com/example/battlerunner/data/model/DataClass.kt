package com.example.battlerunner.data.model


// 데이터 송수신에 필요한 데이터를 담은 클래스를 모두 만들어 주거나
// http요청시 @query문을 이용하셔 spring boot의 레포지토리에서 처리



data class LoginInfo(
    val userId: String,
    val password: String,
    val name: String? = null,
    val loginType: String? = null
)

// 마이페이지나 검색 시 user 정보 표시할 때 사용
data class User(
    val userId: String,         // 유저 아이디
    val username: String,       // 유저 이름
    val totalDistance: Float = 0f,   // 현재 세션의 러닝 거리
    val totalTime: Long = 0L, // 현재 세션의 러닝 시간
    val averageSpeed: Float,   // 평균 속도
)

// running 데이터 전송. 러닝 관련 데이터 저장하기 위한 클래스
data class UserRunning(
    val userId: String,
    val runningTime: Long,
    val distance: Float
)

// user 개인 러닝 데이터 전체 (필요하면 사용. 일단 User 클래스 쓰면 될듯)
data class UserResponse(

    val userId: String,        // 사용자 ID
    val username: String,      // 사용자 이름
    val runningTime: Long,     // 현재 세션의 러닝 시간
    val distance: Float,       // 현재 세션의 러닝 거리
    val totalDistance: Float,  // 누적 러닝 거리
    val totalTime: Float,       // 총 러닝 시간
    val averageSpeed: Float   // 평균 속도
)


// 배틀 상대 표현을 위해 사용자1,2로 구분.
data class Battle(
    val battleId: Long,
    val user1Id: String,  // 사용자 1 (신청 한 사람)
    val user2Id: String,  // 사용자 2 (신청 받은 사람)
    val isBattleStarted: Boolean,
    val gridStartLat: Double,   // 그리드 시작(중심) 위도
    val gridStartLng: Double    // 그리드 시작(중심) 경도
)

//TODO 배틀 기록 저장은 Batlle class 사용. date 사용하지 말자... gridId의 소유권을 실시간으로 업데이트하면 굳이 동시에 들어왔을 때 date를 비교할 필요가 없을듯.
// 동시에 같은 그리드에 들어가 있는 상황: 일단 떠날 때 현재 그리드 상태를 확인한 후 내 영역으로 업데이트 되도록 설정(소유권 업데이트는 새로운 항목을 만드는게 아니라 덮어쓰기라 실시간으로 저장해도 데이터가 많아지지 않음)
// 한 사람이 먼저 그 그리드를 떠나면 바로 소유권 업데이트.
// 남아 있는 사람도 떠날 때 그리드 소유권을 확인하고, 다시 내 소유권으로 업데이트되는 구조.

// 배틀 기록 저장 ▶ 마이페이지 기록 저장용으로 사용하는 것 (서버에서 신경 안 써도 됨)
data class BattleRecord(
   val date: String,
   val opponentName: String,
   val imagePath: String
)

data class BattleGrid(
    val id: Int, // 인조키
    val gridId: Int,
    val battleId: Long,
    val userId: String  // 소유자 아이디 저장
)


// TODO 그리드 소유권은 계속해서 변경되므로 따로 데이터 클래스를 생성해서 다루는 게 좋은듯.
// 배틀 소유권 - 요청 데이터 모델
data class GridOwnershipUpdateRequest(
    val gridId: String,    // 그리드 ID
    val userId: String    // 소유자 ID
)
data class GridOwnershipMapResponse(
    val ownershipMap: Map<String, String> // <gridId, userId>
)


//TODO 가현
// 시작 위치 관련해서는 따로 데이터 클래스랑 레트로핏 코드 작성할 필요 없어 보임.
// 어짜피 시작 위치는 battleGrid 테이블이 아니라 battle 테이블에 저장하고 다시는 수정 안할 것이므로 전송할 땐 user1, user2, isbattlestarted와 함께 Battle class에 한꺼번에 전송.
// 나중에 battle 테이블에서 startLat이랑 startLng를 가져와서 사용만 함.
// 일단 사용 중인 코드라 냅둘게. 수정 필요
//배틀 그리드 - 시작 위치를 서버로 전송
data class GridStartLocationRequest(
    val battleId: String,
    val gridStartLat: Double,
    val gridStartLng: Double
)
// 배틀 그리드 - 시작 위치를 서버에서 가져오기
data class GridStartLocationResponse(
    val gridStartLat: Double?,
    val gridStartLng: Double?
)



data class ApiResponse(val success: Boolean, val message: String)

