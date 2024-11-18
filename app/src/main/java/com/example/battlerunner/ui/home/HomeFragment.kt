package com.example.battlerunner.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.PersonalEndActivity
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.example.battlerunner.utils.MapUtils.stopLocationUpdates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mapFragment = MapFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient // fusedLocationClient 초기화 선언
    private lateinit var googleMap: GoogleMap // 구글 맵 객체 저장
    private lateinit var locationCallback: LocationCallback // 위치 업데이트 콜백

    // ★ Activity 범위에서 HomeViewModel을 가져오기
    private val homeViewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

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

        // MapFragment가 추가된 후에 map 초기화를 시도
        childFragmentManager.executePendingTransactions()

        // MapFragment의 onMapReady가 호출되었을 때 현재 위치로 이동하도록 콜백 설정
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate() // 내 위치 초기화 호출
        }

        // MainActivity의 콜백 설정 (BattleFragment' 시작 버튼)
        (activity as? MainActivity)?.startPathDrawing = {
            homeViewModel.setDrawingStatus(true) // 경로 그리기 활성화
            observePathUpdates() // 경로 관찰 시작
        }
        // MainActivity의 콜백 설정 (BattleFragment' 정지 버튼)
        (activity as? MainActivity)?.stopPathDrawing = {
            homeViewModel.setDrawingStatus(false) // 경로 그리기 활성화
        }

        // 초기 버튼 상태 설정: 시작 버튼만 보이도록
        binding.startBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        binding.finishBtn.visibility = View.GONE

        // 타이머 UI 업데이트
        homeViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
        // 거리 UI 업데이트
        homeViewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance) // 'm' 단위로 표시
        }

        // homeViewModel의 start, isRunning의 여부에 따른 버튼 변경
        // battleFragment에서 시작, 정지 버튼을 눌렀을 때 homeFragment에도 적용
        homeViewModel.hasStarted.observe(viewLifecycleOwner) { hasStarted ->
            if (hasStarted) {
                if (homeViewModel.isRunning.value == true) {
                    binding.startBtn.visibility = View.GONE
                    binding.stopBtn.visibility = View.VISIBLE
                    binding.finishBtn.visibility = View.VISIBLE
                } else {
                    binding.startBtn.visibility = View.VISIBLE
                    binding.stopBtn.visibility = View.GONE
                    binding.finishBtn.visibility = View.GONE
                }
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }
        homeViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (!isRunning) {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }

        // 시작 버튼 리스너
        binding.startBtn.setOnClickListener {
            // 위치 권한이 있다면 위치 업데이트 시작
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)

                homeViewModel.startTimer() // 타이머 시작
                homeViewModel.setHasStarted(true) // 타이머 시작 상태를 true로 설정

                homeViewModel.setDrawingStatus(true) // 경로 그리기 활성화
                observePathUpdates() // 경로 관찰 시작

                (activity as? MainActivity)?.notifyStartTracking() // MainActivity에 알림 -> battleFragment 시작 버튼 공유

                // 버튼 상태 변경
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE
            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        // 정지 버튼 리스너
        binding.stopBtn.setOnClickListener {
            homeViewModel.stopTimer() // 타이머 중지
            homeViewModel.setDrawingStatus(false) // 경로 그리기 중지

            (activity as? MainActivity)?.notifyStopTracking() // MainActivity에 알림 -> battleFragment 정지 버튼 공유

            // 버튼 상태 변경
            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }

        // 종료 버튼 리스너
        binding.finishBtn.setOnClickListener {
            homeViewModel.stopTimer() // 타이머 중지
            homeViewModel.setDrawingStatus(false) // 경로 그리기 중지
            stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지

            // PersonalEndActivity 실행
            val intent = Intent(requireActivity(), PersonalEndActivity::class.java).apply {
                // 러닝 소요 시간, 거리 전달
                putExtra("elapsedTime", homeViewModel.elapsedTime.value ?: 0L)
                putExtra("distance", homeViewModel.distance.value ?: 0f)
            }
            homeViewModel.resetTimer()
            startActivityForResult(intent, REQUEST_CODE_PERSONAL_END)
        }

        // Goal 버튼 리스너
//        binding.GoalBtn?.setOnClickListener {
//            try {
//                val intent = Intent(requireContext(), HomeGoalActivity::class.java)
//                startActivity(intent)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(requireContext(), "Error navigating to HomeGoalActivity", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    private fun observePathUpdates() {
        homeViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (homeViewModel.isDrawing.value == true) { // viewModel의 isDrawing을 사용
                mapFragment.drawPath(pathPoints) // 경로 그리기
            }
        }
    }

    // 프래그먼트가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        homeViewModel.stopTimer() // 타이머 정지
    }

    companion object {
        private const val REQUEST_CODE_PERSONAL_END = 1001
    }
}
