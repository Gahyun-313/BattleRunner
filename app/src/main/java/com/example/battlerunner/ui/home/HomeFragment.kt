//HomeFragment
package com.example.battlerunner.ui.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.BattleEndActivity
import com.example.battlerunner.PersonalEndActivity
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.ui.shared.SharedViewModel
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.LocationUtils.requestLocationPermission
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mapFragment = MapFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var cameraPosition: CameraPosition? = null
    private lateinit var locationCallback: LocationCallback // 위치 업데이트 콜백






    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    private val pathPoints = mutableListOf<LatLng>()

    private var isDrawing = false // 경로 그리기 상태 변수임

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)


        binding.startBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        binding.finishBtn.visibility = View.GONE


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

        // MapFragment가 추가된 후에 map 초기화를 시도
        childFragmentManager.executePendingTransactions()//추가한거

        // MapFragment의 onMapReady가 호출되었을 때 현재 위치로 이동하도록 콜백 설정
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate() // 내 위치 초기화 호출
        }//추가한거

        // MainActivity의 콜백 설정 (BattleFragment' 시작 버튼)
        (activity as? MainActivity)?.startPathDrawing = {
            Log.d("HomeFragment", "startPathDrawing called from MainActivity") // 로그 추가

            viewModel.setDrawingStatus(true) // 경로 그리기 활성화
            observePathUpdates() // 경로 관찰 시작
        }//추가한거



        // hasStarted와 isRunning 상태를 관찰하여 버튼 가시성 업데이트
        sharedViewModel.hasStarted.observe(viewLifecycleOwner) { hasStarted ->
            if (hasStarted && sharedViewModel.isRunning.value == true) {
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }

        sharedViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning && sharedViewModel.hasStarted.value == true) {
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }


        // ViewModel의 경과 시간 관찰하여 UI 업데이트
        sharedViewModel.elapsedTimeForHome.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // ViewModel의 거리 관찰하여 UI 업데이트
        viewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance)
        }

        binding.startBtn.setOnClickListener {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, viewModel)
                sharedViewModel.startTimer() // 타이머 시작

                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE
            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        binding.stopBtn.setOnClickListener {
            sharedViewModel.stopTimer()
            viewModel.setDrawingStatus(false) // 경로 그리기 중지
            MapUtils.stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지

            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }

        binding.finishBtn.setOnClickListener {
            sharedViewModel.stopTimer()
            viewModel.setDrawingStatus(false) // 경로 그리기 중지
            MapUtils.stopLocationUpdates(fusedLocationClient) // 경로 업데이트 중지

            // PersonalEndActivity로 이동하며 경과 시간과 거리를 전달
            val intent = Intent(requireContext(), PersonalEndActivity::class.java).apply {
                putExtra("elapsedTime", sharedViewModel.elapsedTimeForHome.value ?: 0L)
                putExtra("distance", viewModel.distance.value ?: 0f)
            }
            startActivityForResult(intent, REQUEST_CODE_PERSONAL_END)

            // HomeFragment에서만 타이머를 완전히 초기화

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





    /*binding.BattlefinishBtn.setOnClickListener {
        // BattleEndActivity로 이동
        val intent = Intent(requireContext(), BattleEndActivity::class.java)
        startActivity(intent)
    }*/






    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERSONAL_END && resultCode == Activity.RESULT_OK) {
            sharedViewModel.resetTimer()

            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        // ViewModel의 현재 상태에 따라 버튼 가시성 설정
        if (sharedViewModel.hasStarted.value == true && sharedViewModel.isRunning.value == true) {
            binding.startBtn.visibility = View.GONE
            binding.stopBtn.visibility = View.VISIBLE
            binding.finishBtn.visibility = View.VISIBLE
        } else {
            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }
        //binding.startBtn.visibility = View.VISIBLE
        //binding.stopBtn.visibility = View.GONE
        //binding.finishBtn.visibility = View.GONE
    }

    private fun observePathUpdates() {
        viewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (viewModel.isDrawing.value == true) { // viewModel의 isDrawing을 사용
                mapFragment.drawPath(pathPoints) // 경로 그리기
            }
        }
    }

    /*private fun updateMapPath(pathPoints: List<LatLng>) {
        googleMap.clear()
        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(5f)
        )
    }*/ //지워봄

    /*override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            LocationUtils.requestLocationPermission(this)
        }
        startLocationUpdates()
    }*/ //지워봄

    /*private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (LocationUtils.hasLocationPermission(requireContext())) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }*/ //지워봄

    /*private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                updateLocationUI(location)
            }
        }
    }*/ //지워봄

    /*private fun updateLocationUI(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        pathPoints.add(latLng)
        viewModel.addPathPoint(latLng)
        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(5f)
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }*/ //지워봄


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopTimer() // 타이머 정지
    }

    companion object {
        private const val REQUEST_CODE_PERSONAL_END = 1001
    }
}