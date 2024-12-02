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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BattleViewModel : ViewModel() {

    // 그리드 폴리곤 리스트를 관리하는 LiveData
    private val _gridPolygons = MutableLiveData<List<Polygon>>()
    val gridPolygons: LiveData<List<Polygon>> get() = _gridPolygons

    // 각 그리드 ID와 소유자 ID를 매핑
    val ownershipMap = mutableMapOf<String, String>()

    // 소유권 추적 상태 플래그
    private var isTrackingActive = false

    // 그리드 시작 위치를 저장하는 LiveData
    private val _gridStartLocation = MutableLiveData<LatLng>()
    val gridStartLocation: LiveData<LatLng> get() = _gridStartLocation

    // 배틀 상대 이름을 저장하는 LiveData
    private val _user2Name = MutableLiveData<String>()
    val user2Name: LiveData<String> get() = _user2Name

    // 서버에서 그리드 시작 위치 가져오기
    fun getGridStartLocationFromServer(
        battleId: String,
        onLocationReceived: (LatLng?) -> Unit
    ) {
        RetrofitInstance.battleApi.getGridStartLocation(battleId).enqueue(object : Callback<GridStartLocationResponse> {
            override fun onResponse(call: Call<GridStartLocationResponse>, response: Response<GridStartLocationResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.gridStartLat != null && body.gridStartLng != null) {
                        onLocationReceived(LatLng(body.gridStartLat, body.gridStartLng))
                    } else {
                        Log.w("BattleViewModel", "그리드 시작 위치 정보가 없음")
                        onLocationReceived(null)
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

    // 서버에 그리드 시작 위치 저장하기
    fun setGridStartLocationToServer(
        battleId: String,
        location: LatLng,
        onComplete: (Boolean) -> Unit
    ) {
        val request = GridStartLocationRequest(battleId, location.latitude, location.longitude)
        RetrofitInstance.battleApi.setGridStartLocation(request).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d("BattleViewModel", "그리드 시작 위치 저장 성공")
                    onComplete(true)
                } else {
                    Log.e("BattleViewModel", "그리드 시작 위치 저장 실패: ${response.errorBody()?.string()}")
                    onComplete(false)
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("BattleViewModel", "서버 통신 실패", t)
                onComplete(false)
            }
        })
    }

    // 소유권 추적 활성화/비활성화 설정
    fun setTrackingActive(active: Boolean) {
        isTrackingActive = active
    }

    // 배틀 상대 이름 설정
    fun setUser2Name(name: String) {
        if (_user2Name.value != name) {
            _user2Name.postValue(name)
        }
    }

    // 고유 그리드 ID 생성
    private fun generateGridId(row: Int, col: Int): String {
        return "grid_${row}_${col}"
    }

    // 고정된 크기의 그리드 생성
    fun createFixedGrid(
        map: GoogleMap,
        gridStartLatLng: LatLng,
        rows: Int,
        cols: Int,
        gridSize: Int = 500
    ) {
        val polygons = mutableListOf<Polygon>()
        val metersToLatLng = 0.000009

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
                    .strokeColor(Color.GRAY)
                    .strokeWidth(0.5f)
                    .fillColor(Color.argb(10, 0, 0, 0))

                val polygon = map.addPolygon(polygonOptions)
                val gridId = generateGridId(row, col)
                polygon.tag = gridId
                ownershipMap[gridId] = "neutral"
                polygons.add(polygon)
            }
        }

        _gridPolygons.value = polygons
        Log.d("BattleViewModel", "그리드 생성 완료. 총 폴리곤 수: ${polygons.size}")
    }

    // 소유권 업데이트
    fun updateOwnership(userLocation: LatLng, userId: String) {
        if (!isTrackingActive) return

        _gridPolygons.value?.forEach { polygon ->
            if (polygon.isPointInside(userLocation)) {
                val gridId = polygon.tag.toString()
                if (ownershipMap[gridId] != userId) {
                    ownershipMap[gridId] = userId
                    polygon.fillColor = Color.BLUE
                    sendOwnershipToServer(gridId, userId)
                }
            }
        }
    }

    // 서버로 소유권 데이터 전송
    private fun sendOwnershipToServer(gridId: String, ownerId: String) {
        val request = GridOwnershipUpdateRequest(gridId, ownerId)
        RetrofitInstance.battleApi.updateGridOwnership(request).enqueue(object : Callback<ApiResponse> {
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

    // 상대 소유권 업데이트
    fun updateOpponentOwnership(gridId: String, opponentId: String) {
        _gridPolygons.value?.find { it.tag.toString() == gridId }?.let { polygon ->
            ownershipMap[gridId] = opponentId
            polygon.fillColor = Color.RED // 상대 소유권을 빨간색으로 표시
        }
    }

    // 그리드 데이터를 JSON 형식으로 변환
    fun getGridDataAsJson(): String {
        val gridData = ownershipMap.map { (gridId, owner) ->
            mapOf("id" to gridId, "owner" to owner)
        }
        return Gson().toJson(gridData)
    }

    // 그리드 초기화
    fun clearGrid() {
        ownershipMap.clear()
        _gridPolygons.value?.forEach { it.remove() }
        _gridPolygons.value = emptyList()
        Log.d("BattleViewModel", "그리드 초기화 완료")
    }

    // 폴리곤 내부 여부 확인
    private fun Polygon.isPointInside(point: LatLng): Boolean {
        val vertices = this.points
        var contains = false
        var j = vertices.size - 1
        for (i in vertices.indices) {
            if ((vertices[i].latitude > point.latitude) != (vertices[j].latitude > point.latitude) &&
                (point.longitude < (vertices[j].longitude - vertices[i].longitude) *
                        (point.latitude - vertices[i].latitude) /
                        (vertices[j].latitude - vertices[i].latitude) + vertices[i].longitude)
            ) {
                contains = !contains
            }
            j = i
        }
        return contains
    }
}
