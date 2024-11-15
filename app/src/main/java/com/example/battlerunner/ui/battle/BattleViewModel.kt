// BattleViewModel.kt
package com.example.battlerunner

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.GoogleMap // 수정: GoogleMap 임포트 추가
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions


class BattleViewModel : ViewModel() {

    // 그리드 polygon 리스트 (polygon 객체 리스트)
    private val _gridPolygons = MutableLiveData<List<Polygon>>() // _gridPolygons: 생성된 그리드 폴리곤들을 LiveData 형태로 저장
    val gridPolygons: LiveData<List<Polygon>> get() = _gridPolygons
    private val ownershipMap = mutableMapOf<Polygon, String>() // 각 폴리곤의 소유자를 추적하기 위한 맵 => <폴리곤 객체, 소유자ID>




    private val _elapsedTime = MutableLiveData<Long>().apply { value = 0L }
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private val _pathPoints = MutableLiveData<MutableList<LatLng>>().apply { value = mutableListOf() }
    val pathPoints: LiveData<MutableList<LatLng>> get() = _pathPoints

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName


    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTimeValue: Long = 0

    // 초기 그리드를 생성하고 _gridPolygons LiveData에 추가
    fun createGrid(map: GoogleMap, centerLatLng: LatLng, rows: Int, cols: Int, gridSize: Int = 250) {
        Log.d("BattleViewModel", "createGrid 호출됨. Center: $centerLatLng")
        val polygons = mutableListOf<Polygon>()
        val metersToLatLng = 0.00000225     // 약 1m를 위도/경도로 변환한 값 (수정: 정확한 변환 값 사용)

        // 시작 좌표를 사용자 위치 기준으로 중앙에 설정
        val startLatLng = LatLng(
            centerLatLng.latitude - (rows / 2) * gridSize * metersToLatLng, // 남쪽으로 이동
            centerLatLng.longitude - (cols / 2) * gridSize * metersToLatLng // 서쪽으로 이동
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
                    .fillColor(Color.argb(10, 0, 0, 0)) // 폴리곤 채우기 색상

                val polygon = map.addPolygon(polygonOptions) // 설정한 옵션을 사용해 폴리곤을 지도에 추가
                ownershipMap[polygon] = "neutral" // 초기 소유자-> "neutral"
                polygons.add(polygon) // 생성한 폴리곤을 리스트에 추가함
            }
        }
        _gridPolygons.value = polygons  // 생성된 그리드를 LiveData에 추가
        Log.d("BattleViewModel", "생성된 그리드 폴리곤 수: ${polygons.size}")

    }

    // 사용자의 위치를 기준으로 그리드의 소유권을 업데이트
    fun updateOwnership(userLocation: LatLng, userId: String) {
        _gridPolygons.value?.forEach { polygon ->
            if (polygon.isPointInside(userLocation)) { // isPointInside 함수 호출
                val currentOwner = ownershipMap[polygon]
                if (currentOwner != userId) { // 기존 소유자와 다르면 업데이트
                    ownershipMap[polygon] = userId
                    polygon.fillColor = Color.BLUE // 사용자가 해당 그리드에 있을 경우 파란색으로 변경
                    Log.d("BattleViewModel", "Polygon ownership updated for user: $userId") // 추가: 소유권 업데이트 로그
                    // sendOwnershipToServer(polygon.id.toString(), userId) // 서버로 소유권 전송 예시
                    // TODO: 서버로 소유권 전송
                }
            } else {
                Log.d("BattleViewModel", "User location $userLocation not inside polygon: ${polygon.points}")

            }
        }
    }

    // Polygon 확장 함수 정의
    private fun Polygon.isPointInside(point: LatLng): Boolean {
        val vertices = this.points
        var contains = false
        var j = vertices.size - 1

        for (i in vertices.indices) {
            if ((vertices[i].latitude > point.latitude) != (vertices[j].latitude > point.latitude) &&
                (point.longitude < (vertices[j].longitude - vertices[i].longitude) * (point.latitude - vertices[i].latitude) /
                        (vertices[j].latitude - vertices[i].latitude) + vertices[i].longitude)
            ) {
                contains = !contains
            }
            j = i
        }

        Log.d("BattleViewModel", "Point $point ${if (contains) "is inside" else "is outside"} polygon")
        return contains // Polygon 내부 여부를 반환
    }

    fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTimeValue
            isRunning = true
            updateTimer()
        }
    }

    fun stopTimer() {
        isRunning = false
    }

    private fun updateTimer() {
        if (isRunning) {
            elapsedTimeValue = System.currentTimeMillis() - startTime
            _elapsedTime.postValue(elapsedTimeValue)
            Handler(Looper.getMainLooper()).postDelayed({ updateTimer() }, 1000)
        }
    }

    fun addPathPoint(point: LatLng) {
        _pathPoints.value?.apply {
            add(point)
            _pathPoints.postValue(this)
        }
    }

    fun setUserName(name: String) {
        if (_userName.value != name) {
            _userName.postValue(name)
        }
    }





    fun resetTimer() {
        stopTimer()
        elapsedTimeValue = 0
        _elapsedTime.postValue(0)
    }
}