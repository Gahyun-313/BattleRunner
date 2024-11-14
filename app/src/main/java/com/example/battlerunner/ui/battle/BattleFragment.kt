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
    private lateinit var googleMap: GoogleMap
    private lateinit var locationCallback: LocationCallback // 위치 콜백
    private lateinit var dbHelper: DBHelper // --그리드 소유권 때문에 임시로 넣은 것--
    private var gridInitialized = false
    //private var mapFragment = MapFragment()

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

        initializeLocationUpdates() // 위치 업데이트 함수 호출
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
            if (LocationUtils.hasLocationPermission(requireContext())) {
                //MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)
                startLocationUpdates() // 위치 업데이트 시작 메서드 호출
            } else {
                LocationUtils.requestLocationPermission(this)
            }
            homeViewModel.startTimer()
            (activity as? MainActivity)?.notifyStartPathDrawing()
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

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                Log.d("BattleFragment", "현재 위치를 기준으로 그리드 생성 시작")

                battleViewModel.createGrid(googleMap, currentLatLng, 29, 29) // 현재 위치 기준으로 그리드 생성
                // * battleViewModel.createGrid(지도 객체, 그리드 생성 기준이 되는 중심 위치, 그리드의 행, 그리드의 열)
                gridInitialized = true
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
                        battleViewModel.updateOwnership(userLocation, userInfo.second)
                    }
                }
            }
        }
        //MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel) // 위치 업데이트 시작
        startLocationUpdates()
    }

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
        fusedLocationClient.removeLocationUpdates(locationCallback) // 수정: 위치 업데이트 중지
    }

    // Polygon.contains 확장 함수 정의
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
        return contains
    }

    // 내 위치 표시 활성화 메서드
    private fun enableMyLocation() {
        try {
            googleMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e("BattleFragment", "위치 권한이 필요합니다.", e)
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000 // 권한 요청 코드 상수
    }
}