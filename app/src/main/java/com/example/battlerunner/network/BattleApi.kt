package com.example.battlerunner.network

import com.example.battlerunner.data.model.ApiResponse
import com.example.battlerunner.data.model.Battle
import com.example.battlerunner.data.model.GridOwnershipMapResponse
import com.example.battlerunner.data.model.GridOwnershipUpdateRequest
import com.example.battlerunner.data.model.GridStartLocationRequest
import com.example.battlerunner.data.model.GridStartLocationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface BattleApi {

    // 배틀 생성. 배틀 신청 시 battleId만 할당하고 나머지 null. 텅 빈 상태로 생성만 하고, 배틀 신청 요청시 update를 통해
    @POST("/api/battles/create")
    suspend fun createBattle(
        @Body battleDto: Battle
    ): Battle

    // 배틀 신청 요청
    // TODO 가현
    //  신청 요청 시  DBHelper에서 내 아이디 받아와서 user1으로 사용. loginApi를 통해 상대 검색을 해서 신청 클릭 시 그 userId를 DBHelper에 user2로 저장하고 가져와서 사용.
    //  이때, isBattleStarted를 true로 저장하고, 시작 그리드의 경도와 위도를 저장. battle 테이블은 처음 배틀 신청 시 생성 및 업데이트하고 그 이후로 데이터를 수정하지 않음.
    //  배틀 시작 후에는 grid 관련 데이터만 변경되므로 grid 테이블을 다뤄야함.
    //  @Query가 아니라 위의 데이터를 모두 모아 BattleDto 객체에 저장해서 @Body battleDto: Battle 이런식으로 서버에 보내야함. <-로그인이나 유저 api 생성한거 참고하기.
    //  다른 코드 꼬여서 일단 수정 안하고 냅둘게. 다른 메소드랑 같이 수정해 줘.
    @POST("/api/battles/update")
    suspend fun updateBattle(
        @Query("user1Id") user1Id: String,
        @Query("user2Id") user2Id: String
    ): Battle

    // TODO 위에 updateBattle을 보낼 때 한번에 시작 위치도 포함해서 보낼거라 필요없음
//    // 배틀 시작 위치를 서버로 전송
//    @POST("/api/battles/{battleId}/poststartLocation")
//    fun setGridStartLocation(@Body startLocationRequest: GridStartLocationRequest): Call<ApiResponse>

    // 배틀 시작 위치를 서버에서 가져오기. 시작위치만 가져다가 쓸 거라 필요함.
    @GET("/api/battles/{battleId}/getstartLocation")
    fun getGridStartLocation(@Path("battleId") battleId: Long): Call<GridStartLocationResponse>

    // 소유권 업데이트. battlegrid 테이블 업데이트
    @PUT("/api/grid/{battleId}/{gridId}/update")
    fun updateGridOwnership(
        @Path("battleId") battleId: Long,
        @Body request: GridOwnershipUpdateRequest
    ): Call<ApiResponse>


    // 소유권 가져오기.
    @GET("/api/grid/{battleId}/grid/ownership")
    fun getGridOwnership(@Path("battleId") battleId: Long): Call<GridOwnershipMapResponse>
}

//
//Repository Layer에서 Retrofit 호출
//안드로이드의 ViewModel 또는 Repository 클래스에서 Retrofit을 사용하여 백엔드 API를 호출합니다. 예를 들어, 배틀을 생성하는 예시를 보면:
//class BattleRepository(private val battleApi: BattleApi) {
//
//    // 배틀 생성
//    suspend fun createBattle(battleDto: Battle): Battle {
//        return battleApi.createBattle(battleDto)
//    }
//
//    // 배틀 신청
//    suspend fun requestBattle(user1Id: String, user2Id: String): Battle {
//        return battleApi.requestBattle(user1Id, user2Id)
//    }
//
//    // 시작 위치 가져오기
//    fun getGridStartLocation(battleId: Long): Call<GridStartLocationResponse> {
//        return battleApi.getGridStartLocation(battleId)
//    }
//
//    // 시작 위치 설정
//    fun setGridStartLocation(startLocationRequest: GridStartLocationRequest): Call<ApiResponse> {
//        return battleApi.setGridStartLocation(startLocationRequest)
//    }
//
//    // 소유권 업데이트
//    fun updateGridOwnership(battleId: Long, request: GridOwnershipUpdateRequest): Call<ApiResponse> {
//        return battleApi.updateGridOwnership(battleId, request)
//    }
//
//    // 소유권 조회
//    fun getGridOwnership(battleId: Long): Call<GridOwnershipMapResponse> {
//        return battleApi.getGridOwnership(battleId)
//    }
//}
//
//ViewModel에서 데이터 처리
//ViewModel에서 Repository를 사용하여 데이터를 처리하고 UI에 반영할 수 있습니다. 예를 들어, 배틀을 시작하는 코드를 작성해보겠습니다.
//
//kotlin
//코드 복사
//class BattleViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val battleRepository = BattleRepository(ApiClient.getInstance().create(BattleApi::class.java))
//    val battleResponse = MutableLiveData<Battle>()
//    val apiResponse = MutableLiveData<ApiResponse>()
//
//    // 배틀 생성 신청하기 버튼 눌렀을 때 생성. 나중에 battleId를 알고 싶으면 user1Id와 user2Id로 id(인조키) 검색해서 알아내야됨.
//    fun createBattle(battleDto: Battle) {
//        viewModelScope.launch {
//            try {
//                val response = battleRepository.createBattle(battleDto)
//                battleResponse.postValue(response)
//            } catch (e: Exception) {
//                // 오류 처리
//            }
//        }
//    }
//
//    // 배틀 신청 user1Id는 내 아이디. DBHelper에서 가져오기. user2Id는 상대 아이디. 신청하기 버튼 둘렀을 때 그 아이디 DBHelper에 저장하고 다시 가져오기.
//    fun requestBattle(user1Id: String, user2Id: String) {
//        viewModelScope.launch {
//            try {
//                val response = battleRepository.requestBattle(user1Id, user2Id)
//                battleResponse.postValue(response)
//            } catch (e: Exception) {
//                // 오류 처리
//            }
//        }
//    }
//
//    // 배틀 그리드 시작 위치 설정
//    fun setGridStartLocation(startLocationRequest: GridStartLocationRequest) {
//        viewModelScope.launch {
//            try {
//                val response = battleRepository.setGridStartLocation(startLocationRequest).execute()
//                if (response.isSuccessful) {
//                    apiResponse.postValue(response.body())
//                } else {
//                    // 실패 처리
//                }
//            } catch (e: Exception) {
//                // 오류 처리
//            }
//        }
//    }
//
//    // 소유권 업데이트
//    fun updateGridOwnership(battleId: Long, gridId: String, userId: String) {
//        val request = GridOwnershipUpdateRequest(gridId, userId)
//        viewModelScope.launch {
//            try {
//                val response = battleRepository.updateGridOwnership(battleId, request).execute()
//                if (response.isSuccessful) {
//                    apiResponse.postValue(response.body())
//                } else {
//                    // 실패 처리
//                }
//            } catch (e: Exception) {
//                // 오류 처리
//            }
//        }
//    }
//}