// BattleViewModel.kt
package com.example.battlerunner.ui.battle

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap // 수정: GoogleMap 임포트 추가
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class BattleViewModel : ViewModel() {

    // 생성된 그리드 폴리곤 리스트 (Polygon 객체 리스트)
    private val _gridPolygons = MutableLiveData<List<Polygon>>() // 수정: PolygonOptions에서 Polygon으로 변경
    val gridPolygons: LiveData<List<Polygon>> get() = _gridPolygons

    // 각 폴리곤의 소유자를 추적하기 위한 Map
    private val ownershipMap = mutableMapOf<Polygon, String>()

    // 초기 그리드를 생성하고 _gridPolygons LiveData에 추가
    fun createGrid(map: GoogleMap, startLatLng: LatLng, rows: Int, cols: Int, gridSize: Int = 250) { // 수정: map 매개변수 추가
        Log.d("BattleViewModel", "createGrid called") // 추가: createGrid 호출 확인

        val polygons = mutableListOf<Polygon>()
        val metersToLatLng = 0.00000225     // 약 1m를 위도/경도로 변환한 값 (수정: 정확한 변환 값 사용)

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
                    .strokeColor(Color.BLACK) // 검은색 경계선
                    .strokeWidth(2f) // 경계선 두께
                    .fillColor(Color.argb(100, 0, 0, 255)) // 초기 상태에서는 투명

                val polygon = map.addPolygon(polygonOptions)
                Log.d("BattleViewModel", "Polygon created and added to map: ${polygon.points}")

                ownershipMap[polygon] = "neutral" // 초기 소유자는 중립으로 설정
                polygons.add(polygon)
            }
        }
        _gridPolygons.value = polygons
    }

    // 사용자의 위치를 기준으로 그리드의 소유권을 업데이트
    fun updateOwnership(userLocation: LatLng, userId: String) {
        _gridPolygons.value?.forEach { polygon ->
            if (polygon.contains(userLocation)) { // 특정 위치가 폴리곤 내에 있는지 확인
                val currentOwner = ownershipMap[polygon]
                if (currentOwner != userId) { // 기존 소유자와 다르면 업데이트
                    ownershipMap[polygon] = userId
                    polygon.fillColor = Color.BLUE // 사용자가 해당 그리드에 있을 경우 파란색으로 변경
                }
            }
        }
    }

    // Polygon.contains 확장 함수 정의
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
