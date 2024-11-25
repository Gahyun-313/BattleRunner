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
        // '확인' 버튼 클릭 이벤트 설정
        findViewById<Button>(R.id.confirmBtn)?.setOnClickListener {
            val distanceInput = findViewById<EditText>(R.id.distanceInput)
            val distance = distanceInput?.text.toString().toIntOrNull()
            if (distance != null) {
                // 유효한 거리 입력 처리
                handleDistanceInput(distance)
            }
        }

        // '닫기' 버튼 클릭 시 액티비티 종료
        findViewById<ImageButton>(R.id.closeBtn)?.setOnClickListener {
            finish()
        }
    }

    private fun handleDistanceInput(distance: Int) {
        fetchCurrentLocation { currentLocation ->
            // 도착지 계산 (예: 대략적으로 북쪽으로 이동)
            val goalLocation = LatLng(
                currentLocation.latitude + (distance * 0.001), // 임의로 거리 계산
                currentLocation.longitude
            )

            // Directions API 호출
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





    // 경로 데이터를 지도에 표시하는 메서드
    private fun displayRouteOnMap(path: List<List<Double>>) {
        if (!::naverMap.isInitialized) {
            Toast.makeText(this, "지도 초기화 중입니다. 잠시 후 다시 시도하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val coordinates = path.map { LatLng(it[1], it[0]) } // [경도, 위도]를 [위도, 경도]로 변환

        Log.d("Route", "Coordinates: $coordinates") // 디버깅용 로그

        // 기존 폴리라인 제거
        polyline?.map = null

        // 새로운 경로 표시
        polyline = PolylineOverlay().apply {
            this.coords = coordinates
            this.color = resources.getColor(R.color.purple_500, null)
            this.width = 10
            this.map = naverMap
        }

        // 카메라를 경로 시작점으로 이동
        naverMap.moveCamera(CameraUpdate.scrollTo(coordinates.first()))
    }






    // 경로 데이터를 파싱하여 LatLng 리스트로 변환하는 메서드
    private fun parseRouteData(routeData: String): List<List<Double>> {
        return routeData.split("\n") // 각 줄이 하나의 좌표
            .mapNotNull { line ->
                val parts = line.split(",") // "위도,경도" 형식 분리
                if (parts.size == 2) {
                    val lat = parts[0].toDoubleOrNull()
                    val lng = parts[1].toDoubleOrNull()
                    if (lat != null && lng != null) {
                        listOf(lat, lng) // [위도, 경도] 리스트로 반환
                    } else {
                        null
                    }
                } else {
                    null
                }
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
                        addMarkers(startPoint, endPoint)
                        displayRouteOnMap(path)
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
            // 마지막 위치 값으로 초기 카메라 설정
            locationSource.lastLocation?.let { lastLocation ->
                val lastLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                naverMap.moveCamera(CameraUpdate.scrollTo(lastLatLng)) // 마지막 위치로 빠르게 이동
            }

            // 위치 소스 설정
            naverMap.locationSource = locationSource

            // 위치 추적 모드 활성화
            naverMap.locationTrackingMode = LocationTrackingMode.Follow

            // 지도 UI 설정
            naverMap.uiSettings.apply {
                isLocationButtonEnabled = true // 현재 위치 버튼 활성화
                isZoomControlEnabled = true // 확대/축소 컨트롤 활성화
                isCompassEnabled = true // 나침반 활성화
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
                // 위치 권한 거부 시 메시지 표시
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
            return
        }
    }

    // MapView와 관련된 생명주기 메서드
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
