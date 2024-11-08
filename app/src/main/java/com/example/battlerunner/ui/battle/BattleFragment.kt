package com.example.battlerunner.ui.battle

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.*
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
        Log.d("BattleFragment", "onCreateView called") // 추가: onCreateView 호출 확인

        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("BattleFragment", "onViewCreated called")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // SupportMapFragment를 동적으로 추가 (수정된 부분)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapFragmentContainer, it)
                    .commit()
            }
        mapFragment.getMapAsync(this) // getMapAsync 호출

        // 위치 업데이트 콜백 초기화
        initializeLocationUpdates()

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
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)
            } else {
                LocationUtils.requestLocationPermission(this)
            }
            homeViewModel.startTimer()
            (activity as? MainActivity)?.notifyStartPathDrawing()
        }
    }
    // GoogleMap이 준비되었을 때 호출되는 메서드 (수정된 부분)
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("BattleFragment", "onMapReady called") // onMapReady 호출 확인 로그

        // 초기 카메라 위치 설정
        initializeMap() // 수정된 부분: initializeMap 메서드 호출
    }

    // Territory Capture 그리드 초기화
    private fun initializeMap() {
        Log.d("BattleFragment", "initializeMap called") // 추가: initializeMap 호출 확인

        val startLatLng = LatLng(37.5665, 126.9780) // 시작 위치
        battleViewModel.createGrid(googleMap, startLatLng, 10, 10)

        // 그리드 폴리곤을 관찰하여 지도에 추가 (이미 BattleViewModel에서 지도에 추가되었으므로 생략 가능)
        // 여기서는 필요하지 않을 수 있습니다.
    }

    // 위치 업데이트를 위한 LocationRequest와 LocationCallback 설정
    private fun initializeLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // 위치 업데이트 주기 (1초)
        ).apply {
            setMinUpdateIntervalMillis(500) // 최소 업데이트 주기
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)

                    // 현재 위치가 포함된 폴리곤을 찾아 소유권을 업데이트
//                    battleViewModel.updateOwnership(userLocation, "user1") // 수정: 위치에 따라 소유권 갱신
                    dbHelper.getUserInfo()
                        ?.let { battleViewModel.updateOwnership(userLocation, it.second) } // 수정: 위치에 따라 소유권 갱신

                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000 // 권한 요청 코드 상수
    }
}
