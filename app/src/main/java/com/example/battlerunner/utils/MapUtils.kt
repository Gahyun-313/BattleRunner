package com.example.battlerunner.utils

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.ui.home.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.gson.Gson

// 지도 권한에 대한 유틸리티 클래스
object MapUtils {

    // 현재 위치를 나타내는 LiveData
    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = _currentLocation

    // 위치 경로를 나타내는 LiveData
    private val _pathPoints = MutableLiveData<List<LatLng>>(emptyList())
    val pathPoints: LiveData<List<LatLng>> get() = _pathPoints

    // 위치 콜백 설정
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.lastOrNull()?.let { location ->
                _currentLocation.value = location
                println("Location Update: ${location.latitude}, ${location.longitude}")
                updatePathPoints(location) // 위치 경로 업데이트
            }
        }
    }

    // 위치 업데이트 시작
    fun startLocationUpdates(context: Context, fusedLocationClient: FusedLocationProviderClient, viewModel: HomeViewModel) {
        if (LocationUtils.hasLocationPermission(context)) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            locationResult.locations.lastOrNull()?.let { location ->
                                // 현재 위치 업데이트
                                _currentLocation.value = location
                                // ViewModel에 새로운 위치 추가
                                viewModel.addPathPoint(LatLng(location.latitude, location.longitude))
                            }
                        }
                    },
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // [경로] 경로를 그릴 PolylineOptions 생성
    fun createPolylineOptions(points: List<LatLng>): PolylineOptions {
        return PolylineOptions()
            .addAll(points)
            .width(500f)  // 두께
            .color(Color.BLUE)  // 경로 색상
            .geodesic(true)  // 지오데식 경로 설정
            .startCap(RoundCap())  // 시작점 둥글게
            .endCap(RoundCap())  // 끝점 둥글게
            .jointType(JointType.ROUND)  // 경로 연결 부분 둥글게
    }

    // 위치 업데이트 중지
    fun stopLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // [경로] 경로를 업데이트
    private fun updatePathPoints(location: Location) {
        val newPathPoints = _pathPoints.value?.toMutableList() ?: mutableListOf()
        newPathPoints.add(LatLng(location.latitude, location.longitude))
        _pathPoints.value = newPathPoints
    }

    // [경로] 경로 초기화 - 그려진 경로 지우기(리셋)
    fun clearPathPoints() {
        _pathPoints.value = emptyList()
    }
}

