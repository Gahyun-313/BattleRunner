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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BattleViewModel : ViewModel() {

    // 그리드 폴리곤 리스트를 관리하는 LiveData
    private val _gridPolygons = MutableLiveData<List<Polygon>>() // 지도에 표시된 그리드 목록
    val gridPolygons: LiveData<List<Polygon>> get() = _gridPolygons // 외부에서 읽기 가능하도록 제공

    // 각 그리드 ID와 소유자 ID를 매핑
    val ownershipMap = mutableMapOf<Int, String>() // 그리드 ID와 소유자의 맵

    // 소유권 추적 상태 플래그
    private var isTrackingActive = false // 소유권 추적 활성화 여부를 나타냄

    // 그리드 시작 위치를 저장하는 LiveData
    private val _gridStartLocation = MutableLiveData<LatLng>() // 시작 위치 정보 저장
    val gridStartLocation: LiveData<LatLng> get() = _gridStartLocation // 외부에서 읽기 가능하도록 제공

    // 배틀 상대 이름을 저장하는 LiveData
    private val _opponentName = MutableLiveData<String>() // 배틀 상대 이름 정보 저장
    val opponentName: LiveData<String> get() = _opponentName // 외부에서 읽기 가능하도록 제공

    val userId: String = "gu20313@gmail.com"
    val opponentId: String = "gus20313@gmail.com"
    //TODO 작동 가능하게 변경해야 함

    // DBHelper 싱글턴 인스턴스 초기화

    /**
     * 서버에서 그리드 시작 위치를 가져오는 함수
     *
     * @param battleId 배틀 ID
     * @param onLocationReceived 콜백으로 위치 반환
     */
    fun getGridStartLocationFromServer(
        battleId: Long,
        onLocationReceived: (LatLng?) -> Unit
    ) {
        RetrofitInstance.battleApi.getGridStartLocation(battleId).enqueue(object : Callback<GridStartLocationResponse> {
            override fun onResponse(call: Call<GridStartLocationResponse>, response: Response<GridStartLocationResponse>) {
                if (response.isSuccessful) { // 서버 요청 성공 여부 확인
                    response.body()?.let { body -> // 응답 본문 확인
                        if (body.gridStartLat != null && body.gridStartLng != null) { // 시작 위치가 존재하면
                            onLocationReceived(LatLng(body.gridStartLat, body.gridStartLng)) // 위치 반환
                        } else {
                            Log.w("BattleViewModel", "그리드 시작 위치 정보가 없음") // 로그 출력
                            onLocationReceived(null) // 위치가 없으면 null 반환
                        }
                    }
                } else {
                    Log.e("BattleViewModel", "시작 위치 가져오기 실패: ${response.errorBody()?.string()}") // 실패 로그 출력
                    onLocationReceived(null) // 실패 시 null 반환
                }
            }

            override fun onFailure(call: Call<GridStartLocationResponse>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t) // 통신 오류 로그 출력
                onLocationReceived(null) // 오류 시 null 반환
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
        val request = GridStartLocationRequest(battleId, location.latitude, location.longitude) // 요청 객체 생성
        RetrofitInstance.battleApi.setGridStartLocation(battleId, request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) { // 서버 요청 성공 여부 확인
                    Log.d("BattleViewModel", "그리드 시작 위치 저장 성공") // 성공 로그 출력
                    onComplete(true) // 성공 시 true 반환
                } else {
                    Log.e("BattleViewModel", "그리드 시작 위치 저장 실패: ${response.errorBody()?.string()}") // 실패 로그 출력
                    onComplete(false) // 실패 시 false 반환
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t) // 통신 오류 로그 출력
                onComplete(false) // 오류 시 false 반환
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
    fun setOpponentName(name: String) {
        if (_opponentName.value != name) { // 이름이 변경된 경우에만
            _opponentName.postValue(name) // LiveData 업데이트
        }
    }

    /**
     * 고유 그리드 ID 생성
     *
     * @param row 행 번호
     * @param col 열 번호
     * @return 생성된 그리드 ID
     */
    private fun generateGridId(row: Int, col: Int, cols: Int): Int {
        return row * cols + col // 동적으로 cols를 받아서 사용
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
                val gridId = generateGridId(row, col, cols) // 그리드 ID 생성
                polygon.tag = gridId // 폴리곤에 태그 설정
                //ownershipMap[gridId] = "neutral" // 기본 소유권 설정
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
            if (polygon.isPointInside(userLocation)) { // 사용자의 위치가 폴리곤 내부인지 확인
                val gridId = polygon.tag as Int // 폴리곤의 태그에서 그리드 ID 가져오기
                if (ownershipMap[gridId] != userId) { // 소유자가 변경된 경우에만 처리
                    ownershipMap[gridId] = userId // 소유권 업데이트
                    polygon.fillColor = Color.BLUE // 폴리곤 색상을 사용자 소유 색상으로 변경
                    Log.d("BattleViewModel", "소유권 업데이트 - Grid ID: $gridId, User ID: $userId")
                    sendOwnershipToServer(battleId, gridId, userId) // 소유권 데이터를 서버로 전송
                }
            }
        }
    }

    // 서버로 소유권 데이터 전송
    private fun sendOwnershipToServer(battleId: Long, gridId: Int, ownerId: String) {
        Log.d("BattleViewModel", "서버로 소유권 업데이트 : battleId=$battleId, gridId=$gridId, ownerId=$ownerId")

        RetrofitInstance.battleApi.updateGridOwnership(battleId, gridId, ownerId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("BattleViewModel", "소유권 서버 업데이트 성공")
                } else {
                    Log.e("BattleViewModel", "소유권 서버 업데이트 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t)
            }
        })
    }

    // 서버에서 소유권 데이터를 가져오는 함수
    fun fetchGridOwnership(battleId: Long, onComplete: (Boolean) -> Unit) {
        RetrofitInstance.battleApi.getGridOwnership(battleId).enqueue(object : Callback<Map<Int, String>> {
            override fun onResponse(call: Call<Map<Int, String>>, response: Response<Map<Int, String>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    //Log.d("BattleViewModel", "Server Response Body: $responseBody") // 서버 응답 전체 출력

                    val ownershipMapFromServer = response.body() ?: emptyMap()
                    if (ownershipMapFromServer.isEmpty()) {
                        Log.w("BattleViewModel", "서버에서 빈 소유권 데이터 반환")
                    } else {
                        Log.d("BattleViewModel", "Received ownership map: $ownershipMapFromServer") // 각 GridId와 OwnerId 출력
                        ownershipMap.clear()
                        ownershipMapFromServer.forEach { (gridId, userId) ->
                            //Log.d("BattleViewModel", "Grid ID: $gridId, Owner ID: $userId")
                            ownershipMap[gridId] = userId
                        }
                        updateGridColors()
                    }
                    onComplete(true)
                } else {
                    Log.e("BattleViewModel", "소유권 동기화 실패: ${response.errorBody()?.string()}")
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<Map<Int, String>>, t: Throwable) {
                Log.e("BattleViewModel", "소유권 동기화 중 오류 발생", t)
                onComplete(false)
            }
        })
    }



    // 지도에 소유권 정보 반영
    private fun updateGridColors() {
        _gridPolygons.value?.forEach { polygon ->
            val gridId = polygon.tag as? Int // 폴리곤 태그에서 Grid ID 가져오기
            if (gridId != null) {
                val ownerId = ownershipMap[gridId] // Grid ID로 소유권 확인
                polygon.fillColor = when (ownerId) {
                    // TODO: !!!!!!!!!!!!!!!아이디 하드코딩!!!!!!!!!!!!!!!!!!!!!!!
                    "gu20313@naver.com" -> Color.BLUE // 내 소유
                    "shhk2100@gmail.com" -> Color.RED // 상대 소유
                    else -> Color.argb(10, 0, 0, 0) // 중립
                }
                Log.d("BattleViewModel", "Grid $gridId 색상 업데이트: $ownerId")
            } else {
                Log.e("BattleViewModel", "Polygon 태그에서 gridId를 가져올 수 없음: $polygon")
            }
        }
        Log.d("BattleViewModel", "지도 색상 업데이트 완료")
    }



    // 상대 소유권 업데이트
    fun updateGridOwnership_Real(gridId: Int, ownerId: String) {
        _gridPolygons.value?.find { it.tag as? Int == gridId }?.let { polygon ->
            ownershipMap[gridId] = ownerId
            polygon.fillColor = when (ownerId) {
                // TODO: !!!!!!!!!!!!!!!아이디 하드코딩!!!!!!!!!!!!!!!!!!!!!!!
                "gu20313@naver.com" -> Color.BLUE // 내 소유
                "shhk2100@gmail.com" -> Color.RED // 상대 소유
                else -> Color.argb(10, 0, 0, 0) // 중립
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
