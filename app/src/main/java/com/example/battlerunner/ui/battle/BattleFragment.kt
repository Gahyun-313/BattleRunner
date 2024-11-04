package com.example.battlerunner.ui.battle

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.ui.home.HomeFragment
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

class BattleFragment : Fragment(R.layout.fragment_battle) {

    private var _binding: FragmentBattleBinding? = null // 바인딩 객체
    private val binding get() = _binding!!
    private var mapFragment = MapFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient // fusedLocationClient 선언

    // Activity 범위에서 HomeViewModel을 가져오기 (HomeFragment랑 타이머 공유용)
    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private var isDrawing = false // 경로 그리기 상태 변수

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity()) // fusedLocationClient 초기화

        // MapFragment 초기화 및 설정
        mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, mapFragment)
            .commitNow() // commitNow를 사용해 트랜잭션이 즉시 완료되도록 보장

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
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient)
                // 위치 권한이 없다면
            } else {
                LocationUtils.requestLocationPermission(this)
            }

            viewModel.startTimer() // 타이머 시작
            isDrawing = true // 경로 그리기 활성화
            observePathUpdates() // 경로 관찰 시작
        }

        // 종료 버튼 리스너
        binding.finishBtn.setOnClickListener {
            viewModel.stopTimer() // 타이머 중지
            isDrawing = false // 경로 그리기 중지
            MapUtils.stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지
        }
    }

    private fun observePathUpdates() {
        viewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (isDrawing) { // isDrawing이 활성화된 경우에만 경로 업데이트
                mapFragment.drawPath(pathPoints)
            }
        }
    }

    // 프래그먼트가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopTimer() // 타이머 정지
        // Todo : 프래그먼트를 전환해도 종료 버튼을 누를 때 까지는 타이머가 살아있도록 수정 필요

    }

}
