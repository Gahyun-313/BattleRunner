// BattleViewModel.kt
package com.example.battlerunner.ui.battle

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class BattleViewModel : ViewModel() {

    // 생성된 그리드 폴리곤 객체 리스트
    private val _gridPolygons = MutableLiveData<List<PolygonOptions>>()
    val gridPolygons: LiveData<List<PolygonOptions>> get() = _gridPolygons

    // 각 폴리곤의 소유자를 추적하기 위한 Map
    private val ownershipMap = mutableMapOf<Polygon, String>()

    // 초기 그리드를 생성하고 맵에 폴리곤을 추가
    fun createGrid(startLatLng: LatLng, rows: Int, cols: Int, gridSize: Int = 250) {
        val polygons = mutableListOf<PolygonOptions>()
        val metersToLatLng = 0.00000225 // 약 1m를 위도/경도로 변환한 값

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val southWest = LatLng(
                    startLatLng.latitude + i * gridSize * metersToLatLng,
                    startLatLng.longitude + j * gridSize * metersToLatLng
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
                    .strokeColor(Color.BLACK)
                    .fillColor(Color.LTGRAY) // 기본 색상

                polygons.add(polygonOptions) // 생성한 PolygonOptions 추가
            }
        }
        _gridPolygons.value = polygons
    }


    // 사용자의 위치를 기준으로 폴리곤 소유권을 업데이트
    fun updateOwnership(polygon: Polygon, userId: String) {
        val currentOwner = ownershipMap[polygon] // 기존 소유자 확인
        if (currentOwner != userId) { // 기존 소유자가 다른 경우에만 업데이트
            ownershipMap[polygon] = userId
            polygon.fillColor = if (userId == "user1") Color.BLUE else Color.RED // 색상 변경
        }
    }

    // Polygon.contains() 확장 함수 정의
    private fun Polygon.contains(point: LatLng): Boolean {
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
        return contains
    }
}
