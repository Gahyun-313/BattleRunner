// HomeFragment.kt
package com.example.battlerunner.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Activity 범위에서 HomeViewModel을 가져오기
    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private lateinit var googleMap: GoogleMap

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

        // 타이머와 경과 시간을 ViewModel에서 관찰하여 UI 업데이트
        viewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // 위치 추적 및 타이머 시작 버튼
        binding.startBtn.setOnClickListener {
            viewModel.startTimer()
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), LocationServices.getFusedLocationProviderClient(requireActivity()))
            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        // 위치 추적 및 타이머 중지 버튼
        binding.finishBtn.setOnClickListener {
            viewModel.stopTimer()
            MapUtils.stopLocationUpdates()
        }

        // 지도 초기화 및 준비
        val supportMapFragment = SupportMapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, supportMapFragment)
            .commit()
        supportMapFragment.getMapAsync(this)
    }

    // GoogleMap 준비가 완료되면 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (LocationUtils.hasLocationPermission(requireContext())) {
            MapUtils.drawPathOnMap(googleMap)
            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            LocationUtils.requestLocationPermission(this)
        }

        // ViewModel의 경로 포인트 LiveData를 관찰하여 지도에 경로 업데이트
        viewModel.pathPoints.observe(viewLifecycleOwner) {
            MapUtils.drawPathOnMap(googleMap)
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
