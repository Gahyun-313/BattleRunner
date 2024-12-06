package com.example.battlerunner.ui.home

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.service.LocationService
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.example.battlerunner.utils.MapUtils.pathPoints
import com.example.battlerunner.utils.MapUtils.stopLocationUpdates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

// TODO 앱 실행 시 맵 >> 위치 못 불러옴. 기본 위치(5공)으로만 뜸
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mapFragment: MapFragment? = null

    // ★ GlobalApplication에서 HomeViewModel을 가져오기
    private val homeViewModel: HomeViewModel by lazy {
        (requireActivity().application as GlobalApplication).homeViewModel
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

        // MapFragment 초기화
        mapFragment = MapFragment().also {
            childFragmentManager.beginTransaction()
                .replace(R.id.mapFragmentContainer, it)
                .commitNow()
        }

        // MapFragment가 준비된 후 현재 위치를 이동
        childFragmentManager.executePendingTransactions()

        mapFragment?.setOnMapReadyCallback {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                mapFragment?.enableMyLocation()
                mapFragment?.moveToCurrentLocationImmediate()
            } else {
                // 위치 권한 없을 경우 기본 위치 설정 (명지대 5공학관)
                val defaultLocation = LatLng(37.222101, 127.187709)
                mapFragment?.googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f)
                )
            }
        }

        setupObservers()
        setupButtons()

        // MainActivity의 콜백 설정 (BattleFragment' 시작 버튼)
        (activity as? MainActivity)?.startPathDrawing = {
            // Foreground Service 시작 (백그라운드)
            val serviceIntent = Intent(requireContext(), LocationService::class.java)
            requireContext().startService(serviceIntent)

            homeViewModel.setDrawingStatus(true) // 경로 그리기 활성화
            observePathUpdates() // 경로 관찰 시작
        }
        // MainActivity의 콜백 설정 (BattleFragment' 정지 버튼)
        (activity as? MainActivity)?.stopPathDrawing = {
            homeViewModel.setDrawingStatus(false) // 경로 그리기 비활성화
        }

        // MainActivity의 콜백 설정 (BattleFragment' 오늘의 러닝 종료 버튼)
        (activity as? MainActivity)?.resetPathDrawing = {
            homeViewModel.setDrawingStatus(false) // 경로 그리기 중지
            stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지

            // 새로운 MapFragment 생성하여 경로 제거
            childFragmentManager.beginTransaction()
                .replace(R.id.mapFragmentContainer, MapFragment())
                .commitNow()
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
        homeViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (pathPoints.isEmpty()) {
                mapFragment!!.clearMapPath() // 지도에서 경로 제거
            }
        }

        homeViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (pathPoints.isEmpty()) {
                mapFragment!!.clearMapPath() // 경로 제거
            } else {
                mapFragment!!.drawPath(pathPoints) // 경로 다시 그리기
            }
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

                // Foreground Service 시작
                (activity as? MainActivity)?.startLocationService()

                homeViewModel.startTimer() // 타이머 시작
                homeViewModel.setHasStarted(true) // 타이머 시작 상태를 true로 설정

                homeViewModel.setDrawingStatus(true) // 경로 그리기 활성화
                observePathUpdates() // 경로 관찰 시작

                // MainActivity에 알림 -> battleFragment 시작 버튼 공유
                (activity as? MainActivity)?.notifyTracking(true)

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

            (activity as? MainActivity)?.notifyTracking(false) // MainActivity에 알림 -> battleFragment 정지 버튼 공유

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

            (activity as? MainActivity)?.notifyTracking(false) // MainActivity에 알림 -> battleFragment 소유권 추적 중지

            // Foreground Service 중지
            (activity as? MainActivity)?.stopLocationService()

            // PersonalEndActivity 실행
            val intent = Intent(requireActivity(), PersonalEndActivity::class.java).apply {
                // 데이터 전달
                putExtra("elapsedTime", homeViewModel.elapsedTime.value ?: 0L) // 러닝 소요 시간 전달
                putExtra("distance", homeViewModel.distance.value ?: 0f) // 러닝 거리 전달
            }
            startActivityForResult(intent, REQUEST_CODE_PERSONAL_END)
            /*
                (기존) _binding = null 으로 프래그먼트를 종료하는 대신,
                프래그먼트에 나타나는 데이터 값들을 초기화하는 형태로 리셋
                */

            onDestroyView()
        }

        // Goal_Btn 클릭 시 HomeGoalActivity로 이동
        binding.GoalBtn?.setOnClickListener {
            try {
                val intent = Intent(requireContext(), HomeGoalActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error navigating to HomeGoalActivity", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        // 타이머 UI 업데이트
        homeViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // 거리 UI 업데이트
        homeViewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance)
        }

        // 경로 업데이트
        homeViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (pathPoints.isNotEmpty()) {
                mapFragment?.drawPath(pathPoints)
            } else {
                mapFragment?.clearMapPath()
            }
        }

        // 버튼 상태 업데이트
        homeViewModel.hasStarted.observe(viewLifecycleOwner) { hasStarted ->
            if (hasStarted) {
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }
    }

    private fun setupButtons() {
        binding.startBtn.setOnClickListener {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)
                homeViewModel.startTimer()
                homeViewModel.setHasStarted(true)
                (activity as? MainActivity)?.notifyTracking(true)

                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE
            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        binding.stopBtn.setOnClickListener {
            homeViewModel.stopTimer()
            homeViewModel.setDrawingStatus(false)
            (activity as? MainActivity)?.notifyTracking(false)

            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }

        binding.finishBtn.setOnClickListener {
            homeViewModel.stopTimer()
            homeViewModel.setDrawingStatus(false)
            MapUtils.stopLocationUpdates(fusedLocationClient)
            (activity as? MainActivity)?.notifyTracking(false)

            val intent = Intent(requireActivity(), PersonalEndActivity::class.java).apply {
                putExtra("elapsedTime", homeViewModel.elapsedTime.value ?: 0L)
                putExtra("distance", homeViewModel.distance.value ?: 0f)
            }
            startActivityForResult(intent, REQUEST_CODE_PERSONAL_END)
        }
    }


    private fun observePathUpdates() {
        homeViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (pathPoints.isNotEmpty()) {
                println("Observed Path Points: ${pathPoints.size}") // 로그 추가
                mapFragment?.drawPath(pathPoints)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERSONAL_END && resultCode == Activity.RESULT_OK) {
            homeViewModel.resetAllData() // ViewModel 데이터 초기화
            mapFragment?.clearMapPath() // 지도 경로 초기화
        }
    }

    override fun onResume() {
        super.onResume()

        // 위치 권한 확인 및 요청
        if (!LocationUtils.hasLocationPermission(requireContext())) {
            LocationUtils.requestLocationPermission(this)
        }
    }

    companion object {
        private const val REQUEST_CODE_PERSONAL_END = 1001
    }

}
