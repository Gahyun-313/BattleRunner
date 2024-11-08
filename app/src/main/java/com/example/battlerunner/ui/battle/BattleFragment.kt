package com.example.battlerunner.ui.battle

import android.app.Activity
import android.graphics.Color
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.databinding.FragmentMapBinding
import com.example.battlerunner.ui.home.HomeFragment
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class BattleFragment : Fragment(R.layout.fragment_battle) {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!
    private var mapFragment = MapFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient // fusedLocationClient 초기화 선언
    private lateinit var googleMap: GoogleMap // 구글 맵 객체 저장
    private lateinit var locationCallback: LocationCallback // 위치 업데이트 콜백


    // ★ Activity 범위에서 HomeViewModel을 가져오기
    private val homeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }
    private val battleViewModel by lazy {
        ViewModelProvider(this).get(BattleViewModel::class.java)
    }

    // 프래그먼트의 뷰를 생성하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드, 주요 초기화 작업 수행
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // MapFragment 초기화 및 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer)
                as? SupportMapFragment
        mapFragment?.getMapAsync {
            googleMap = it
            initializeMap()
        }

        // 타이머와 경과 시간을 ViewModel에서 관찰하여 UI 업데이트
        homeViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // 총 러닝 거리 관찰 및 UI 업데이트
        homeViewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance) // 'm' 단위로 표시
        }

        // 시작 버튼 리스너
        binding.startBtn.setOnClickListener {
            // 위치 권한이 있다면 위치 업데이트 시작
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)
            } else {
                LocationUtils.requestLocationPermission(this)
            }

            homeViewModel.startTimer() // 타이머 시작

            // MainActivity에 경로 시작 알리기 -> HomeFragment에서 경로 그리게 함
            (activity as? MainActivity)?.notifyStartPathDrawing()
        }

        /*
        * 현재 종료 버튼 리스너의 기능을 정지 버튼의 리스너로 변경
        * 종료 버튼 리스너 : 종료 팝업 액티비티 실행 후 타이머&거리&경로 리셋
        */
        // 종료 버튼 리스너
        binding.finishBtn.setOnClickListener {
            homeViewModel.stopTimer() // 타이머 중지
            MapUtils.stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지
        }
    }

    // Territory Capture 그리드 초기화 [!!그리드 그릴 때 필요한 메서드!!]
    private fun initializeMap() {
        val startLatLng = LatLng(37.5665, 126.9780) // 시작 위치 (예: 서울)
        battleViewModel.createGrid(startLatLng, 10, 10) // 10x10 그리드 생성

        // gridPolygons LiveData를 관찰하여 지도에 표시
        battleViewModel.gridPolygons.observe(viewLifecycleOwner) { polygonOptionsList ->
            polygonOptionsList.forEach { polygonOptions ->
                val polygon = googleMap.addPolygon(polygonOptions)
                // 이 시점에서 polygon 객체를 사용해 소유권을 업데이트할 준비가 됨
            }
        }
    }

    // 위치 업데이트를 위한 LocationRequest와 LocationCallback 설정 [!!그리드 그릴 때 필요한 메서드!!]
//    private fun initializeLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            1000L // 위치 업데이트 주기 (1초)
//        ).apply {
//            setMinUpdateIntervalMillis(500) // 최소 업데이트 주기
//        }.build()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                for (location in locationResult.locations) {
//                    val userLocation = LatLng(location.latitude, location.longitude)
//
//                    // 현재 위치가 포함된 폴리곤을 찾아 소유권을 업데이트
//                    battleViewModel.gridPolygons.value?.forEach { polygonOptions ->
//                        // googleMap에서 폴리곤을 추가한 후 반환된 polygon 객체를 사용해야 함
//                        val polygon = googleMap.addPolygon(polygonOptions)
//                        if (polygon.contains(userLocation)) {
//                            battleViewModel.updateOwnership(polygon, "user1")
//                        }
//                    }
//                }
//            }
//        }
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
//    }

    // 프래그먼트가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        homeViewModel.stopTimer() // 타이머 정지
    }

    // Polygon.contains 확장 함수 정의 [!!그리드 그릴 때 필요한 메서드!!]
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
