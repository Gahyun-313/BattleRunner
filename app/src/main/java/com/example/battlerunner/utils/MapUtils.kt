package com.example.battlerunner.utils

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

// 지도 권한에 대한 유틸리티 클래스
object MapUtils {

    // 현재 위치를 나타내는 LiveData
    private val _currentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = _currentLocation

    // 위치 경로를 나타내는 LiveData
    private val _pathPoints = MutableLiveData<List<LatLng>>(emptyList())
    val pathPoints: LiveData<List<LatLng>> get() = _pathPoints

    // 위치 제공자와 콜백 초기화
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.lastOrNull()?.let { location ->
                _currentLocation.value = location
                updatePathPoints(location)
            }
        }
    }

    // 위치 업데이트 시작
    fun startLocationUpdates(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        this.fusedLocationClient = fusedLocationClient

        if (LocationUtils.hasLocationPermission(context)) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // 위치 업데이트 중지
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 지도에 경로를 그리는 메서드
    fun drawPathOnMap(googleMap: GoogleMap) {
        _pathPoints.value?.let { points ->
            googleMap.clear()
            googleMap.addPolyline(
                PolylineOptions().addAll(points)
                    .color(android.graphics.Color.BLUE)
                    .width(5f)
            )
        }
    }

    // 경로를 업데이트
    private fun updatePathPoints(location: Location) {
        val newPathPoints = _pathPoints.value?.toMutableList() ?: mutableListOf()
        newPathPoints.add(LatLng(location.latitude, location.longitude))
        _pathPoints.value = newPathPoints
    }
}
