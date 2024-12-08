package com.example.battlerunner.data.model


// 데이터 송수신에 필요한 데이터를 담은 클래스를 모두 만들어 주거나
// http요청시 @query문을 이용하셔 spring boot의 레포지토리에서 처리

data class LoginInfo(
    val userId: String,
    val password: String,
    val name: String?,
    val loginType: String
)

// 마이페이지나 검색 시 user 정보 표시할 때 사용
data class User(
    val userId: String,         // 유저 아이디
    val username: String,       // 유저 이름
    val totalDistance: Float = 0f,   // 현재 세션의 러닝 거리
    val totalTime: Long = 0L, // 현재 세션의 러닝 시간
    val averageSpeed: Float?   // 평균 속도
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
    val battleId: Long?, // 서버에서 생성
    val user1Id: String,  // 사용자 1 (신청 한 사람)
    val user2Id: String,  // 사용자 2 (신청 받은 사람)
    val isBattleStarted: Boolean,
    val gridStartLat: Double?,   // 그리드 시작(중심) 위도
    val gridStartLng: Double?    // 그리드 시작(중심) 경도
)

//TODO 배틀 기록 저장은 Battle class 사용.
// 동시에 같은 그리드에 들어가 있는 상황: 일단 떠날 때 현재 그리드 상태를 확인한 후 내 영역으로 업데이트 되도록 설정(소유권 업데이트는 새로운 항목을 만드는게 아니라 덮어쓰기라 실시간으로 저장해도 데이터가 많아지지 않음)
// 한 사람이 먼저 그 그리드를 떠나면 바로 소유권 업데이트.
// 남아 있는 사람도 떠날 때 그리드 소유권을 확인하고, 다시 내 소유권으로 업데이트되는 구조.

// 배틀 기록 저장 => 마이페이지에서 기록 확인용 데이터 클래스임!!
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

data class GridOwnership(
    val gridId: String,
    val userId: String
)

data class GridOwnershipMapResponse(
    val ownershipMap: Map<String, String> // <gridId, userId>
)


//	① 배틀 매칭(생성) 시 시작 그리드를 같이 전송하는 것이 불가능.
//		(아직 map관련 메서드가 활성화되지 않은 상태(배틀 매칭은 되었으나 실제 배틀이 시작되지 않은 상태)이기 때문)
//	② "배틀 매칭 -> 맵 프래그먼트 활성화 -> 그리드 그리기" 순서임.
//		처음 Battle 클래스 생성 시 (battleId, user1Id, user2Id, isBattleStarted=true, null, null)로 해두고 이후 <시작>버튼 눌렀을 때 값을 채우는 것으로 해야 함.

//배틀 그리드 - 시작 위치를 서버로 전송
data class GridStartLocationRequest(
    val battleId: Long,
    val gridStartLat: Double,
    val gridStartLng: Double
)
// 배틀 그리드 - 시작 위치를 서버에서 가져오기
data class GridStartLocationResponse(
    val gridStartLat: Double?,
    val gridStartLng: Double?
)

data class ApiResponse(val success: Boolean, val message: String)
