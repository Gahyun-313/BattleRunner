package com.example.battlerunner.ui.battle

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.battlerunner.data.model.*
import com.example.battlerunner.network.RetrofitInstance
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BattleViewModel : ViewModel() {

    // 그리드 폴리곤 리스트를 관리하는 LiveData
    private val _gridPolygons = MutableLiveData<List<Polygon>>() // 지도에 표시된 그리드 목록
    val gridPolygons: LiveData<List<Polygon>> get() = _gridPolygons // 외부에서 읽기 가능하도록 제공

    // 각 그리드 ID와 소유자 ID를 매핑
    val ownershipMap = mutableMapOf<String, String>() // 그리드 ID와 소유자의 맵

    // 소유권 추적 상태 플래그
    private var isTrackingActive = false // 소유권 추적 활성화 여부를 나타냄

    // 그리드 시작 위치를 저장하는 LiveData
    private val _gridStartLocation = MutableLiveData<LatLng>() // 시작 위치 정보 저장
    val gridStartLocation: LiveData<LatLng> get() = _gridStartLocation // 외부에서 읽기 가능하도록 제공

    // 배틀 ID를 저장하는 LiveData
    private val _battleId = MutableLiveData<Long>()
    val battleId: LiveData<Long> get() = _battleId

    private var isRequestPending = false

    // 배틀 ID 설정 메서드
    fun setBattleId(id: Long) {
        _battleId.value = id
    }

    // 배틀 상대 이름을 저장하는 LiveData
    private val _user2Name = MutableLiveData<String>() // 배틀 상대 이름 정보 저장
    val user2Name: LiveData<String> get() = _user2Name // 외부에서 읽기 가능하도록 제공

    /**
     * 서버에서 그리드 시작 위치를 가져오는 함수
     *
     * @param battleId 배틀 ID
     * @param onLocationReceived 콜백으로 위치 반환
     */
    // BattleViewModel.kt
    fun getGridStartLocationFromServer(
        battleId: Long,
        onLocationReceived: (LatLng?) -> Unit
    ) {
        RetrofitInstance.battleApi.getGridStartLocation(battleId).enqueue(object : Callback<GridStartLocationResponse> {
            override fun onResponse(call: Call<GridStartLocationResponse>, response: Response<GridStartLocationResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.gridStartLat != null && body.gridStartLng != null) {
                            onLocationReceived(LatLng(body.gridStartLat, body.gridStartLng))
                        } else {
                            Log.w("BattleViewModel", "서버에 시작 위치가 없습니다.")
                            onLocationReceived(null)
                        }
                    }
                } else {
                    Log.e("BattleViewModel", "시작 위치 가져오기 실패: ${response.errorBody()?.string()}")
                    onLocationReceived(null)
                }
            }

            override fun onFailure(call: Call<GridStartLocationResponse>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t)
                onLocationReceived(null)
            }
        })
    }


    /**
     * 서버에 그리드 시작 위치를 저장하는 함수
     *
     * @param battleId 배틀 ID
     * @param location 저장할 위치 정보
     * @param onComplete 완료 여부 콜백
     */
    fun setGridStartLocationToServer(
        battleId: Long,
        location: LatLng,
        onComplete: (Boolean) -> Unit
    ) {
        val request = GridStartLocationRequest(battleId, location.latitude, location.longitude)
        RetrofitInstance.battleApi.setGridStartLocation(battleId, request).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("BattleViewModel", "서버 응답 성공: ${response.body()?.string()}")
                    onComplete(true)
                } else {
                    Log.e("BattleViewModel", "서버 응답 실패: ${response.code()}, Error Body: ${response.errorBody()?.string()}")
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t)
                onComplete(false)
            }
        })
    }


    /**
     * 소유권 추적 활성화/비활성화 설정
     *
     * @param active 활성화 여부
     */
    fun setTrackingActive(active: Boolean) {
        isTrackingActive = active // 추적 상태 업데이트
    }

    /**
     * 배틀 상대 이름 설정
     *
     * @param name 배틀 상대 이름
     */
    fun setUser2Name(name: String) {
        if (_user2Name.value != name) { // 이름이 변경된 경우에만
            _user2Name.postValue(name) // LiveData 업데이트
        }
    }

    /**
     * 고유 그리드 ID 생성
     *
     * @param row 행 번호
     * @param col 열 번호
     * @return 생성된 그리드 ID
     */
    private fun generateGridId(row: Int, col: Int): String {
        return "grid_${row}_${col}" // 그리드 ID를 행, 열 번호로 생성
    }

    /**
     * 고정된 크기의 그리드를 생성하는 함수
     *
     * @param map GoogleMap 객체
     * @param gridStartLatLng 그리드 시작 위치
     * @param rows 행 개수
     * @param cols 열 개수
     * @param gridSize 그리드 크기 (기본값 500m)
     */
    fun createFixedGrid(
        map: GoogleMap,
        gridStartLatLng: LatLng,
        rows: Int,
        cols: Int,
        gridSize: Int = 500
    ) {
        val polygons = mutableListOf<Polygon>() // 그리드 폴리곤을 저장할 리스트
        val metersToLatLng = 0.000009 // 1m를 LatLng 단위로 변환하는 값

        // 그리드 시작 위치 계산
        val startLatLng = LatLng(
            gridStartLatLng.latitude - (rows / 2) * gridSize * metersToLatLng,
            gridStartLatLng.longitude - (cols / 2) * gridSize * metersToLatLng
        )

        // 그리드 생성
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val southWest = LatLng(
                    startLatLng.latitude + row * gridSize * metersToLatLng,
                    startLatLng.longitude + col * gridSize * metersToLatLng
                )
                val northEast = LatLng(
                    southWest.latitude + gridSize * metersToLatLng,
                    southWest.longitude + gridSize * metersToLatLng
                )

                val polygonOptions = PolygonOptions()
                    .add(
                        southWest,
                        LatLng(southWest.latitude, northEast.longitude),
                        northEast,
                        LatLng(northEast.latitude, southWest.longitude)
                    )
                    .strokeColor(Color.GRAY) // 그리드 외곽선 색상 설정
                    .strokeWidth(0.5f) // 외곽선 두께 설정
                    .fillColor(Color.argb(10, 0, 0, 0)) // 채우기 색상 설정

                val polygon = map.addPolygon(polygonOptions) // 지도에 폴리곤 추가
                val gridId = generateGridId(row, col) // 그리드 ID 생성
                polygon.tag = gridId // 폴리곤에 태그 설정
                ownershipMap[gridId] = "neutral" // 기본 소유권 설정
                polygons.add(polygon) // 폴리곤 리스트에 추가
            }
        }

        _gridPolygons.value = polygons // LiveData 업데이트
        Log.d("BattleViewModel", "그리드 생성 완료. 총 폴리곤 수: ${polygons.size}") // 완료 로그 출력
    }

    // 소유권 업데이트 메서드
    fun updateOwnership(userLocation: LatLng, userId: String, battleId: Long) {
        if (!isTrackingActive) return // 소유권 추적이 비활성화된 경우 바로 종료

        // 폴리곤 리스트에서 사용자가 위치한 폴리곤 찾기
        _gridPolygons.value?.forEach { polygon ->
            if (polygon.isPointInside(userLocation)) { // 사용자가 그리드 내부에 있는지 확인
                val gridId = polygon.tag.toString() // 그리드 Id 가져오기
                if (ownershipMap[gridId] != userId) { // 소유권 변경 발생
                    ownershipMap[gridId] = userId
                    polygon.fillColor = Color.BLUE // 내 소유권 색상 변경
                    sendOwnershipToServer(battleId, gridId, userId) // 서버에 업데이트
                }
            }
        }
    }

    // 서버로 소유권 데이터 전송
    private fun sendOwnershipToServer(battleId: Long, gridId: String, ownerId: String) {
        RetrofitInstance.battleApi.updateGridOwnership(battleId, gridId, ownerId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d("BattleViewModel", "소유권 서버 업데이트 성공")
                } else {
                    Log.e("BattleViewModel", "소유권 서버 업데이트 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t)
            }
        })
    }

    // 서버에서 그리드 소유권 가져와 업데이트
    fun getGridOwnershipFromServer(battleId: Long, onComplete: (List<GridOwnership>) -> Unit) {
        RetrofitInstance.battleApi.getGridOwnership(battleId).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    Log.d("BattleViewModel", "서버 응답 데이터: ${response.body()}")
                    val ownershipList = response.body()?.let { convertMapToList(it) } ?: emptyList()
                    onComplete(ownershipList)
                } else {
                    Log.e("BattleViewModel", "응답 실패: ${response.code()}, ${response.errorBody()?.string()}")
                    onComplete(emptyList())
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t)
                onComplete(emptyList())
            }
        })
    }




    // 배틀 종료를 서버에 알리는 메서드
    fun endBattle(battleId: Long, onComplete: (Boolean) -> Unit) {
        RetrofitInstance.battleApi.endBattle(battleId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    onComplete(true)
                    Log.d("BattleViewModel", "배틀 종료 성공")
                } else {
                    onComplete(false)
                    Log.e("BattleViewModel", "배틀 종료 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onComplete(false)
                Log.e("BattleViewModel", "서버 통신 실패", t)
            }
        })
    }


    // 상대 소유권 업데이트
    fun updateOpponentOwnership(gridId: String, opponentId: String) {
        _gridPolygons.value?.find { it.tag.toString() == gridId }?.let { polygon ->
            ownershipMap[gridId] = opponentId
            polygon.fillColor = Color.RED // 상대 소유권 색상으로 변경
        }
    }

    // 그리드 데이터를 JSON 형식으로 변환
    fun getGridDataAsJson(): String {
        val gridData = ownershipMap.map { (gridId, owner) ->
            mapOf("id" to gridId, "owner" to owner)
        }
        return Gson().toJson(gridData)
    }

    // 서버에서 가져온 Map<String, String> 데이터를 List<GridOwnership>로 변환하는 함수
    fun convertMapToList(ownershipMap: Map<String, String>): List<GridOwnership> {
        return ownershipMap.map { (gridId, userId) -> GridOwnership(gridId, userId) }
    }

    fun updateOwnershipFromServer(battleId: Long) {
        getGridOwnershipFromServer(battleId) { ownershipMap ->
            ownershipMap.forEach { (gridId, userId) ->
                if (this.ownershipMap[gridId] != userId) { // 소유권 변경 감지
                    this.ownershipMap[gridId] = userId
                    updateGridPolygon(gridId, userId) // UI 업데이트
                }
            }
        }
    }



    // 그리드 색상 업데이트 메서드
    private fun updateGridPolygon(gridId: String, userId: String) {
        _gridPolygons.value?.find { it.tag == gridId }?.let { polygon ->
            polygon.fillColor = when (userId) {
                userId -> Color.BLUE // 현재 사용자의 소유권일 경우
                //TODO 상대 ID 가져와야 함
                "상대 사용자 ID" -> Color.RED  // 상대 사용자의 소유권일 경우
                else -> Color.argb(10, 0, 0, 0) // 중립 상태일 경우
            }
        }
    }



    // 그리드 초기화
    fun clearGrid() {
        ownershipMap.clear()
        _gridPolygons.value?.forEach { it.remove() }
        _gridPolygons.value = emptyList()
        Log.d("BattleViewModel", "그리드 초기화 완료")
    }

    // 폴리곤 내부 여부 확인 메서드
    private fun Polygon.isPointInside(point: LatLng): Boolean {
        val vertices = this.points // 폴리곤의 꼭짓점 리스트
        var contains = false // 점이 폴리곤 내부에 있는지 여부를 저장하는 변수
        var j = vertices.size - 1 // 마지막 꼭짓점을 첫 번째 꼭짓점과 비교하기 위한 인덱스
        for (i in vertices.indices) {
            // 현재 점과 이전 점의 위도 값 비교 후 경계선을 넘는지 확인
            if ((vertices[i].latitude > point.latitude) != (vertices[j].latitude > point.latitude) &&
                (point.longitude < (vertices[j].longitude - vertices[i].longitude) *
                        (point.latitude - vertices[i].latitude) /
                        (vertices[j].latitude - vertices[i].latitude) + vertices[i].longitude)
            ) {
                contains = !contains // 경계선을 넘으면 상태를 반전
            }
            j = i // 현재 점을 이전 점으로 설정
        }
        return contains // 점이 폴리곤 내부에 있는지 여부 반환
    }
}
