package com.example.battlerunner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class BattleFragment : Fragment(R.layout.fragment_battle), OnMapReadyCallback {  // OnMapReadyCallback 추가

    // ViewBinding 변수 선언
    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private var isRunning = false  // 타이머 실행 여부
    private var startTime: Long = 0  // 타이머 시작 시간
    private var elapsedTime: Long = 0  // 경과 시간 저장
    private val handler = Handler(Looper.getMainLooper())  // 타이머 업데이트를 위한 핸들러

    // 지도 및 경로 관련 변수
    private lateinit var fusedLocationClient: FusedLocationProviderClient  // 위치 제공자
    private lateinit var googleMap: GoogleMap  // GoogleMap 객체
    private val pathPoints = mutableListOf<LatLng>()  // 경로 좌표 리스트

    // 타이머를 주기적으로 업데이트하기 위한 Runnable
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60))

                // 경과된 시간을 시:분:초 형식으로 표시
                binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                handler.postDelayed(this, 1000)  // 1초마다 업데이트
            }
        }
    }

    // 초기화 (inflate)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용한 inflate
        _binding = FragmentBattleBinding.inflate(inflater, container, false)

        // FusedLocationProviderClient 초기화 (위치 제공자)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드 (버튼 리스너 설정)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 시작 버튼 클릭 리스너
        binding.startBtn.setOnClickListener {
            startTimer()
        }

        // 종료 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            stopTimer()
        }

        // Battle 종료 버튼 클릭 리스너
        binding.BattlefinishBtn.setOnClickListener {
            val intent = Intent(requireActivity(), BattleEndActivity::class.java)
            intent.putExtra("elapsedTime", elapsedTime)  // 경과 시간 전달
            startActivity(intent)
        }

        // MapFragment 추가 및 초기화
        val supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
        supportMapFragment?.getMapAsync(this)

    }


    // 타이머 시작 함수
    private fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime  // 타이머 시작
            isRunning = true
            handler.post(timerRunnable)  // 타이머 동작 시작
        }
    }

    // 타이머 종료 함수
    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(timerRunnable)  // 타이머 중지
        }
    }
    // GoogleMap 준비되면 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 위치 권한 확인
        if (hasLocationPermission()) {
            try {
                googleMap.isMyLocationEnabled = true  // 권한이 있을 때 내 위치 버튼 활성화
            } catch (e: SecurityException) {
                // 권한 없을 경우 예외 처리
                e.printStackTrace()
            }
        } else {
            requestLocationPermission()
        }

        // 지도 초기화 후 위치 추적 시작
        startLocationUpdates()
    }

    // 위치 권한 확인 메서드
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 위치 권한 요청 메서드
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
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

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    // 뷰가 파괴될 때 호출되는 메서드 (메모리 누수 방지)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
