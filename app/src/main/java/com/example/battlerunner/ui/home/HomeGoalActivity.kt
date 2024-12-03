package com.example.battlerunner.ui.home

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.network.DirectionsApiService
import com.example.battlerunner.network.DirectionsResponse
import com.example.battlerunner.network.GPTApiClient
import com.example.battlerunner.network.GPTRequest
import com.example.battlerunner.network.Message
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeGoalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private var polyline: PolylineOverlay? = null
    private var startMarker: Marker? = null
    private var goalMarker: Marker? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_goal)

        // MapView 초기화 전 위치 소스를 설정
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // MapView 초기화
        mapView = findViewById(R.id.naverMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // 지도 준비 콜백 등록

        setupUI()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.confirmBtn)?.setOnClickListener {
            val distanceInput = findViewById<EditText>(R.id.distanceInput)
            val distance = distanceInput?.text.toString().toIntOrNull()
            if (distance != null) {
                fetchRouteRecommendation(distance)
            } else {
                Toast.makeText(this, "유효한 거리를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageButton>(R.id.closeBtn)?.setOnClickListener {
            finish()
        }
    }

    private fun handleDistanceInput(distance: Int) {
        fetchCurrentLocation { currentLocation ->
            val goalLocation = generateRandomGoal(currentLocation, distance) // 랜덤 목표 지점 생성
            getWalkingDirections(currentLocation, goalLocation)
        }
    }

    private fun fetchCurrentLocation(onLocationFetched: (LatLng) -> Unit) {
        // FusedLocationSource에서 현재 위치 가져오기
        val currentLocation = locationSource.lastLocation
        if (currentLocation != null) {
            val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            onLocationFetched(currentLatLng)
        } else {
            Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRouteRecommendation(distance: Int) {
        fetchCurrentLocation { currentLocation ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // GPT API 요청 (수정된 Message 내용)
                    val request = GPTRequest(
                        model = "gpt-3.5-turbo",
                        messages = listOf(
                            Message(
                                "user",
                                "현재 위치 (${currentLocation.latitude}, ${currentLocation.longitude})를 기준으로 출발지에서 직선 거리로 정확히 $distance km 떨어진 목표 지점을 추천해주세요. 동, 서, 남, 북 다양한 방향을 고려해주세요."
                                        + "응답 형식은 반드시 '위도: <숫자>, 경도: <숫자>'로 작성해주세요."
                            )
                        )
                    )
                    val response = GPTApiClient.instance.getRecommendation(request)
                    val recommendation = response.choices.firstOrNull()?.message?.content

                    withContext(Dispatchers.Main) {
                        if (recommendation != null) {
                            Log.d("GPT Response", "추천 경로 응답: $recommendation")
                            val goalLocation = parseGoalLocationFromGPT(recommendation)

                            if (goalLocation != null) {
                                // 거리 검증 추가 (수정된 부분)
                                val actualDistance = calculateDistance(
                                    currentLocation.latitude, currentLocation.longitude,
                                    goalLocation.latitude, goalLocation.longitude
                                )

                                if (Math.abs(actualDistance - distance) > 0.1) { // 오차 범위 100m
                                    Log.e(
                                        "DistanceCheck",
                                        "GPT 응답이 $distance km와 일치하지 않음. 랜덤 목표 지점을 생성합니다."
                                    )
                                    val randomGoal = generateRandomGoal(currentLocation, distance)
                                    getWalkingDirections(currentLocation, randomGoal)
                                } else {
                                    getWalkingDirections(currentLocation, goalLocation)
                                }
                            } else {
                                val randomGoal = generateRandomGoal(currentLocation, distance)
                                getWalkingDirections(currentLocation, randomGoal)
                            }
                        } else {
                            val randomGoal = generateRandomGoal(currentLocation, distance)
                            getWalkingDirections(currentLocation, randomGoal)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeGoalActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 랜덤 목표 지점 생성 (수정된 함수)
    private fun generateRandomGoal(currentLocation: LatLng, distance: Int): LatLng {
        val randomDirection = Math.toRadians((0..360).random().toDouble())
        val distanceInDegrees = distance / 111.0 // 1° 위도 ≈ 111 km

        val newLatitude = currentLocation.latitude + distanceInDegrees * Math.cos(randomDirection)
        val newLongitude = currentLocation.longitude + distanceInDegrees * Math.sin(randomDirection) / Math.cos(
            Math.toRadians(currentLocation.latitude)
        )

        return LatLng(newLatitude, newLongitude)
    }

    // 두 지점 간 거리 계산 함수 (추가된 함수)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // 지구 반경 (단위: km)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    private fun parseGoalLocationFromGPT(response: String): LatLng? {
        val regex = """위도: ([\d.]+), 경도: ([\d.]+)""".toRegex() // 정규식 수정
        val matchResult = regex.find(response)

        return if (matchResult != null && matchResult.groupValues.size == 3) {
            val lat = matchResult.groupValues[1].toDoubleOrNull()
            val lng = matchResult.groupValues[2].toDoubleOrNull()

            if (lat != null && lng != null) {
                LatLng(lat, lng)
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun addMarkers(start: LatLng, goal: LatLng) {
        // 기존 마커 제거
        startMarker?.map = null
        goalMarker?.map = null

        // 새 시작점 마커 생성
        startMarker = Marker().apply {
            position = start
            captionText = "출발"
            map = naverMap
        }

        // 새 도착점 마커 생성
        goalMarker = Marker().apply {
            position = goal
            captionText = "도착"
            map = naverMap
        }
    }

    // 경로 데이터를 지도에 표시하는 메서드
    private fun displayRouteOnMap(path: List<List<Double>>) {
        if (!::naverMap.isInitialized) {
            Toast.makeText(this, "지도 초기화 중입니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // [위도, 경도] 좌표로 변환
        val coordinates = path.map { LatLng(it[1], it[0]) }

        Log.d("Route", "Coordinates: $coordinates") // 경로 디버깅

        // 기존 폴리라인 제거
        polyline?.map = null

        // 새로운 경로 표시
        polyline = PolylineOverlay().apply {
            this.coords = coordinates
            this.color = resources.getColor(R.color.purple_500, null)
            this.width = 10
            this.map = naverMap
        }

        // 경로 전체를 화면에 맞추기 위해 카메라 이동
        val bounds = coordinates.fold(LatLngBounds.Builder()) { builder, coord ->
            builder.include(coord)
        }.build()

        naverMap.moveCamera(CameraUpdate.fitBounds(bounds, 100)) // 패딩 100 적용
    }

    private fun getWalkingDirections(start: LatLng, goal: LatLng) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://naveropenapi.apigw.ntruss.com/") // Base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()



        val apiService = retrofit.create(DirectionsApiService::class.java)

        Log.d("APIRequest", "Start: ${start.longitude},${start.latitude}")
        Log.d("APIRequest", "Goal: ${goal.longitude},${goal.latitude}")




        val call = apiService.getWalkingRoute(
            clientId = "mpyvkwypzg",
            clientSecret = "dSwX2MAgqpGgklr98vtebpyQvzk1SR9Gi50QCHgt",
            start = "${start.longitude},${start.latitude}", // 경도, 위도 순
            goal = "${goal.longitude},${goal.latitude}" // 경도, 위도 순
        )

        call.enqueue(object : retrofit2.Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: retrofit2.Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
                    val path = directionsResponse?.route?.traoptimal?.get(0)?.path
                    if (path != null) {
                        val startPoint = LatLng(path.first()[1], path.first()[0])
                        val endPoint = LatLng(path.last()[1], path.last()[0])
                        addMarkers(startPoint, endPoint) // 마커 업데이트
                        displayRouteOnMap(path) // 경로 표시
                        Toast.makeText(this@HomeGoalActivity, "경로를 성공적으로 표시했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@HomeGoalActivity, "경로 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@HomeGoalActivity, "API 응답 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@HomeGoalActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        try {
            locationSource.lastLocation?.let { lastLocation ->
                val lastLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                naverMap.moveCamera(CameraUpdate.scrollTo(lastLatLng))
            }

            naverMap.locationSource = locationSource
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            naverMap.uiSettings.apply {
                isLocationButtonEnabled = true
                isZoomControlEnabled = true
                isCompassEnabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "지도를 초기화하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
            return
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}