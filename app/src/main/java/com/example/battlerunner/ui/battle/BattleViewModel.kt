package com.example.battlerunner.ui.battle

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.battlerunner.network.RetrofitInstance
import com.google.android.gms.maps.GoogleMap // 수정: GoogleMap 임포트 추가
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.common.collect.Table
import com.google.gson.Gson

class BattleViewModel : ViewModel() {

    // 그리드 polygon 리스트 (polygon 객체 리스트)
    private val _gridPolygons = MutableLiveData<List<Polygon>>() // _gridPolygons: 생성된 그리드 폴리곤들을 LiveData 형태로 저장
    val gridPolygons: LiveData<List<Polygon>> get() = _gridPolygons
    val ownershipMap = mutableMapOf<String, String>() // 각 폴리곤의 소유자를 추적하기 위한 맵 => <폴리곤 ID, 소유자ID>

    private var isTrackingActive = false // 소유권 추적 활성화 상태 플래그

    private val _gridStartLocation = MutableLiveData<LatLng>()
    val gridStartLocation: LiveData<LatLng> get() = _gridStartLocation

    fun setGridStartLocation(location: LatLng) {
        _gridStartLocation.value = location
    }

    fun sendStartLocation(latitude: Double, longitude: Double) {
        // TODO: Retrofit을 사용하여 서버로 전송하는 예제
        Log.d("BattleViewModel", "Start location sent to server: ($latitude, $longitude)")
        // 서버 호출 코드 추가 필요
    }

    // 소유권 추적 활성화/비활성화 설정 메서드
    fun setTrackingActive(active: Boolean) {
        isTrackingActive = active
    }

    // 배틀 상대 이름 가져오기
    private val _user2Name = MutableLiveData<String>()
    val user2Name: LiveData<String> get() = _user2Name

    // 배틀 상대 이름 나타내는 메서드
    fun setUser2Name(name: String) {
        if (_user2Name.value != name) {
            _user2Name.postValue(name)
        }
    }

    // 초기 그리드를 생성하고 _gridPolygons LiveData에 추가
    fun createGrid(map: GoogleMap, startLatLng: LatLng, rows: Int, cols: Int, gridSize: Int = 500) {
        Log.d("BattleViewModel", "createGrid 호출됨. Center: $startLatLng")

        val polygons = mutableListOf<Polygon>() // 생성한 폴리곤 객체들을 저장할 리스트
        val metersToLatLng = 0.000009    // 약 1m를 위도/경도로 변환한 값

        // 시작 좌표
        // TODO: 사용자가 중심 그리드의 중앙 위치에서 시작하도록
        val startLatLng = LatLng(
            startLatLng.latitude - (rows / 2) * gridSize * metersToLatLng,
            startLatLng.longitude - (cols / 2) * gridSize * metersToLatLng
        )

        // 그리드의 행, 열 개수에 맞춰 폴리곤 생성해 지도에 추가
        for (i in 0 until rows) { // 행
            for (j in 0 until cols) { // 열
                val southWest = LatLng( // 폴리곤의 남서쪽 꼭지점 좌표를 계산
                    startLatLng.latitude + i * gridSize * metersToLatLng,
                    startLatLng.longitude + j * gridSize * metersToLatLng
                )
                val northEast = LatLng( // 폴리곤의 북동쪽 꼭지점 좌표를 계산
                    southWest.latitude + gridSize * metersToLatLng,
                    southWest.longitude + gridSize * metersToLatLng
                )
                // 각 꼭지점을 사용하여 폴리곤 옵션을 정의
                val polygonOptions = PolygonOptions()
                    .add(
                        southWest,
                        LatLng(southWest.latitude, northEast.longitude), // 남동쪽 꼭지점
                        northEast,
                        LatLng(northEast.latitude, southWest.longitude) // 북서쪽 꼭지점
                    )
                    .strokeColor(Color.GRAY) // 경계선 색상
                    .strokeWidth(0.5f) // 경계선 두께
                    .fillColor(Color.argb(10, 0, 0, 0)) // 초기 폴리곤 채우기 색상

                val polygon = map.addPolygon(polygonOptions) // 설정한 옵션을 사용해 폴리곤을 지도에 추가
                ownershipMap[polygon.id] = "neutral" // 초기 소유자 설정 -> "neutral"
                polygons.add(polygon) // 생성한 폴리곤을 리스트에 추가
            }
        }
        _gridPolygons.value = polygons  // 생성된 그리드를 LiveData에 추가 -> UI에 반영되도록 함
        Log.d("BattleViewModel", "생성된 그리드 폴리곤 수: ${polygons.size}")

    }

    // 폴리곤 소유권 메서드 (폴리곤 색칠 메서드)
    fun updateOwnership(userLocation: LatLng, userId: String) {
        // 소유권 추적이 비활성화된 경우 => 실행하지 않음
        if (!isTrackingActive) return

        // 소유권 추적이 활성화 된 경우
        _gridPolygons.value?.forEach { polygon -> // 각 폴리곤에 대해 반복

            // 사용자가 해당 폴리곤 내부에 있다면
            if (polygon.isPointInside(userLocation)) {
                val polygonId = polygon.id
                val currentOwner = ownershipMap[polygonId] // 현재 폴리곤의 소유자

                if (currentOwner != userId) { // 현재 소유자가 사용자가 아닐 경우,
                    ownershipMap[polygonId] = userId // 폴리곤의 소유권을 사용자 ID로 업데이트
                    polygon.fillColor = Color.BLUE // 파란색으로 변경

                    // TODO: 서버로 소유권 전송
                    // sendOwnershipToServer(polygon.id.toString(), userId)
                }
            }
        }
    }

    // 상대방의 소유권 업데이트하는 메서드
    fun updateOpponentOwnership(polygonId: String, opponentId: String) {
        _gridPolygons.value?.find { it.id.toString() == polygonId }?.let { polygon ->
            ownershipMap[polygon.id] = opponentId
            polygon.fillColor = Color.RED // 상대방 점유
        }
    }

    //TODO 서버로 소유권 보내는 메서드

    // Polygon 객체에 대해 확장 함수 정의 (포함 여부를 판단)
    private fun Polygon.isPointInside(point: LatLng): Boolean {
        val vertices = this.points // 폴리곤의 꼭지점 좌표 리스트
        var contains = false // 포함 여부를 저장하는 변수
        var j = vertices.size - 1 // 마지막 꼭지점 인덱스를 설정

        // 점이 폴리곤 내부에 있는지 여부를 판별
        for (i in vertices.indices) {
            if ((vertices[i].latitude > point.latitude) != (vertices[j].latitude > point.latitude) &&
                (point.longitude < (vertices[j].longitude - vertices[i].longitude) * (point.latitude - vertices[i].latitude) /
                        (vertices[j].latitude - vertices[i].latitude) + vertices[i].longitude)
            ) {
                contains = !contains // 포함 여부를 토글
            }
            j = i // 이전 꼭지점의 인덱스를 현재 인덱스로 갱신
        }
        return contains // Polygon 내부 여부를 반환 -> 내부 true, 외부 false
    }

    // 그리드 데이터를 Json으로 변환
    fun getGridDataAsJson(): String {
        val gridData = ownershipMap.map { (polygonId, owner) ->
            // 특정 폴리곤의 데이터 검색
            val polygon = _gridPolygons.value?.find { it.id == polygonId }

            // polygon.points를 기반으로 좌표를 매핑
            mapOf(
                "id" to polygonId, // Polygon ID 추가
                "corners" to polygon?.points?.map { point ->
                    mapOf(
                        "latitude" to point.latitude,
                        "longitude" to point.longitude
                    )
                },
                "owner" to owner // 소유권 정보
            )
        }
        return Gson().toJson(gridData) // JSON 변환 후 반환
    }

    // 그리드 초기화 메서드
    fun clearGrid() {
        // 소유권 맵 초기화
        ownershipMap.clear()

        // 그리드 폴리곤 초기화
        _gridPolygons.value?.forEach { polygon ->
            polygon.remove() // 지도에서 제거
        }
        _gridPolygons.value = emptyList() // LiveData를 빈 리스트로 업데이트

        Log.d("BattleViewModel", "Grid가 초기화되었습니다.")
    }


}