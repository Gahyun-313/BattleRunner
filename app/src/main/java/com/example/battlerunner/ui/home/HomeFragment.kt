package com.example.battlerunner.ui.home

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mapFragment = MapFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient // fusedLocationClient 초기화 선언
    private lateinit var googleMap: GoogleMap // 구글 맵 객체 저장
    private lateinit var locationCallback: LocationCallback // 위치 업데이트 콜백



    // ★ Activity 범위에서 HomeViewModel을 가져오기
    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private var isDrawing = false // 경로 그리기 상태 변수

    // 프래그먼트의 뷰를 생성하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드, 주요 초기화 작업 수행
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // MapFragment 초기화 및 설정
        mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()


        // MainActivity의 콜백 설정 (BattleFragment' 시작 버튼)
        (activity as? MainActivity)?.startPathDrawing = {
            Log.d("HomeFragment", "startPathDrawing called from MainActivity") // 로그 추가

            viewModel.setDrawingStatus(true) // 경로 그리기 활성화
            observePathUpdates() // 경로 관찰 시작
        }

        // 타이머와 경과 시간을 ViewModel에서 관찰하여 UI 업데이트
        viewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // 총 러닝 거리 관찰 및 UI 업데이트
        viewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance) // 'm' 단위로 표시
        }

        // 시작 버튼 리스너
        binding.startBtn.setOnClickListener {
            // 위치 권한이 있다면 위치 업데이트 시작
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, viewModel)
            } else {
                LocationUtils.requestLocationPermission(this)
            }

            viewModel.startTimer() // 타이머 시작
            viewModel.setDrawingStatus(true) // 경로 그리기 활성화
            observePathUpdates() // 경로 관찰 시작
        }

        // 종료 버튼 리스너
        binding.finishBtn.setOnClickListener {
            viewModel.stopTimer() // 타이머 중지
            viewModel.setDrawingStatus(false) // 경로 그리기 중지
            MapUtils.stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지

//            // 서버로 러닝 기록 전송
//            viewModel.sendRunningRecordToServer()
            // userId가 설정된 경우에만 서버로 기록 전송
        }
    }

    private fun observePathUpdates() {
        viewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (viewModel.isDrawing.value == true) { // viewModel의 isDrawing을 사용
                mapFragment.drawPath(pathPoints) // 경로 그리기
            }
        }
    }

    // 프래그먼트가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopTimer() // 타이머 정지
    }
}
