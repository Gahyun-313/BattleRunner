package com.example.battlerunner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.battlerunner.databinding.ActivityPersonalEndBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class PersonalEndActivity : AppCompatActivity(), OnMapReadyCallback {

    // ViewBinding을 위한 변수 선언
    private lateinit var binding: ActivityPersonalEndBinding

    // 지도 및 경로 관련 변수
    private lateinit var fusedLocationClient: FusedLocationProviderClient  // 위치 제공자
    private lateinit var googleMap: GoogleMap  // GoogleMap 객체
    private val pathPoints = mutableListOf<LatLng>()  // 경로 좌표 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalEndBinding.inflate(layoutInflater)
        setContentView(binding.root)  // 레이아웃을 화면에 표시

        // FusedLocationProviderClient 초기화 (위치 제공자)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // HomeFragment에서 전달된 경과 시간 받기
        val elapsedTime = intent.getLongExtra("elapsedTime", 0)
        val distance = intent.getFloatExtra("distance", 0f)



        // 경과된 시간, 분, 초 계산
        val seconds = (elapsedTime / 1000) % 60  // 초 계산
        val minutes = (elapsedTime / (1000 * 60)) % 60  // 분 계산
        val hours = (elapsedTime / (1000 * 60 * 60))  // 시 계산

        // 경과 시간을 텍스트뷰에 표시 (시:분:초 형식)
        binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // X 버튼 클릭 리스너 추가
        binding.closeBtn.setOnClickListener {
            setResult(Activity.RESULT_OK) // 결과 설정
            finish() // 액티비티 종료
        }

        // MapFragment 추가 및 초기화
        val supportMapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        supportMapFragment.getMapAsync(this)  // OnMapReadyCallback을 구현한 this를 전달
    }

    // GoogleMap 준비되면 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 위치 권한 확인
        if (hasLocationPermission()) {
            try {
                googleMap.isMyLocationEnabled = true  // 권한이 있을 때 내 위치 버튼 활성화
                startLocationUpdates()
            } catch (e: SecurityException) {
                // 권한 없을 경우 예외 처리
                e.printStackTrace()
            }
        } else {
            requestLocationPermission()
        }
    }

    // 위치 권한 확인 메서드
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 요청 메서드
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // 위치 업데이트 시작하는 메서드
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000  // 위치 업데이트 간격 (2초)
            fastestInterval = 1000  // 가장 빠른 위치 업데이트 간격 (1초)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 높은 정확도 우선
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()  // 권한 요청
            return
        }

        // 실시간 위치 업데이트를 위한 LocationCallback 설정
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    // 위치 업데이트 콜백 설정
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                updateLocationUI(location)  // 실시간 경로 업데이트
            }
        }
    }

    // 위치 업데이트 UI를 실시간으로 갱신하는 메서드
    private fun updateLocationUI(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        pathPoints.add(latLng)  // 경로 리스트에 좌표 추가

        // 경로를 지도에 표시
        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints).color(android.graphics.Color.BLUE).width(5f)
        )

        // 구글맵 카메라 이동 (실시간으로 경로를 따라가도록 설정)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))  // 줌 레벨 18
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}