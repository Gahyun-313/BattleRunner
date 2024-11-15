package com.example.battlerunner.ui.battle

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.example.battlerunner.utils.MapUtils.stopLocationUpdates
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon

class BattleFragment : Fragment(R.layout.fragment_battle), OnMapReadyCallback {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap // GoogleMap 객체를 담아 지도를 제어
    private lateinit var locationCallback: LocationCallback // 위치 업데이트에 필요한 콜백 함수
    private lateinit var dbHelper: DBHelper // 데이터베이스 접근을 위한 DBHelper 인스턴스
    private var gridInitialized = false // 그리드가 초기화되었는지 여부를 확인하는 플래그
    private var trackingActive = false // 현재 소유권 추적 활성화 상태

    private val homeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }
    private val battleViewModel by lazy {
        ViewModelProvider(this).get(BattleViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("BattleFragment", "onCreateView called")
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("BattleFragment", "onViewCreated called")

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // DBHelper 싱글턴 인스턴스 초기화
        dbHelper = DBHelper.getInstance(requireContext())

        // MapFragment 초기화 및 설정 (SupportMapFragment 동적으로 추가)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapFragmentContainer, it)
                    .commit()
            }
        mapFragment.getMapAsync(this) // getMapAsync 호출
        Log.d("BattleFragment", "getMapAsync 호출 완료")

        // 위치 업데이트 시작 ***!!!! 이거 없으면 그리드 안 그려짐 GPT가 없애라고 해도 무시하고 남겨둬 !!!!***
        MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)

        initializeLocationUpdates() // 위치 업데이트 콜백 설정
        Log.d("BattleFragment", "initializeLocationUpdates() 호출 완료")

        // 타이머 및 거리 업데이트 UI
        homeViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            binding.todayTime.text = formatElapsedTime(elapsedTime)
        }
        homeViewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance)
        }

        // 시작 버튼 리스너
        binding.startBtn.setOnClickListener {
            if (!trackingActive) { // 추적이 비활성화된 경우에만 시작

                if (LocationUtils.hasLocationPermission(requireContext())) {
                    startLocationUpdates() // 위치 업데이트 시작 메서드 호출
                    battleViewModel.setTrackingActive(true) // 소유권 추적 활성화
                    homeViewModel.startTimer() // 타이머 시작
                    trackingActive = true // 추적 활성화 상태 변경
                    (activity as? MainActivity)?.notifyStartPathDrawing() // MainActivity에 알림 -> HomeFragment 시작 버튼 공유

                } else {
                    LocationUtils.requestLocationPermission(this)
                }
            }
        }

        binding.finishBtn.setOnClickListener {
            if (trackingActive) { // 추적이 활성화된 경우에만 정지
                stopLocationUpdates()
                battleViewModel.setTrackingActive(false) // 소유권 추적 비활성화
                homeViewModel.stopTimer() // 타이머 중지
                trackingActive = false // 추적 비활성화 상태 변경
            }
        }
    }
    // GoogleMap이 준비되었을 때 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("BattleFragment", "onMapReady called")

        // Google Map 기본 내 위치 버튼 활성화
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        // 권한 확인 후 내 위치 표시
        if (LocationUtils.hasLocationPermission(requireContext())) {
            enableMyLocation() // 내 위치 활성화
            initializeGridWithCurrentLocation() // 현재 위치 기준으로 그리드 초기화
        } else {
            LocationUtils.requestLocationPermission(this)
        }
    }

    // 현재 위치 기반으로 그리드 초기화
    private fun initializeGridWithCurrentLocation() {
        MapUtils.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null && !gridInitialized) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f)) // 지도를 현재 위치로 이동
                Log.d("BattleFragment", "현재 위치를 기준으로 그리드 생성 시작")

                battleViewModel.createGrid(googleMap, currentLatLng, 29, 29) // 현재 위치 기준으로 그리드 생성
                    // * battleViewModel.createGrid(지도 객체, 그리드 생성 기준이 되는 중심 위치, 그리드의 행, 그리드의 열)
                gridInitialized = true // 그리드가 초기화되었음을 표시
            }
        }
    }

    // 위치 업데이트를 설정하는 메서드
    private fun initializeLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    Log.d("BattleFragment", "User location updated: $userLocation")

                    // 현재 위치가 포함된 폴리곤을 찾아 소유권을 업데이트
                    dbHelper.getUserInfo()?.let { userInfo ->
                        battleViewModel.updateOwnership(userLocation, userInfo.second) // 소유권 업데이트
                    }
                }
            }
        }
        startLocationUpdates() // 위치 업데이트 시작
    }

    // 위치 업데이트를 요청하는 메서드
    private fun startLocationUpdates() {
        if (LocationUtils.hasLocationPermission(requireContext())) {
            try {
                // 위치 요청 설정
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L) // 1초 주기로 위치 업데이트
                    .setMinUpdateIntervalMillis(500) // 최소 업데이트 간격 500ms
                    .build()

                // 위치 업데이트 요청
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
                Log.d("BattleFragment", "Location updates started.")
            } catch (e: SecurityException) {
                Log.e("BattleFragment", "위치 권한이 없어 위치 업데이트를 요청할 수 없습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            LocationUtils.requestLocationPermission(this)
        }
    }

    // 위치 업데이트 중지
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("BattleFragment", "Location updates stopped.")
    }

    // 경과 시간을 형식에 맞춰 반환
    private fun formatElapsedTime(elapsedTime: Long): String {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Fragment가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fusedLocationClient.removeLocationUpdates(locationCallback) // 위치 업데이트 중지
    }

    // 내 위치 표시 활성화 메서드
    private fun enableMyLocation() {
        try {
            googleMap.isMyLocationEnabled = true // 구글 기본 내 위치 버튼 활성화
        } catch (e: SecurityException) {
            Log.e("BattleFragment", "위치 권한이 필요합니다.", e)
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000 // 권한 요청 코드 상수
    }
}