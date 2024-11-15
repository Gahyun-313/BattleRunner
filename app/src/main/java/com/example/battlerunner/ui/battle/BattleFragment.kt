//BattleFragment.kt
package com.example.battlerunner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.shared.SharedViewModel
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polygon

class BattleFragment : Fragment(R.layout.fragment_battle), OnMapReadyCallback {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!
    //private lateinit var battleViewModel: BattleViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private lateinit var locationCallback: LocationCallback // 위치 콜백
    private lateinit var dbHelper: DBHelper // --그리드 소유권 때문에 임시로 넣은 것--
    private var gridInitialized = false
    //private var mapFragment = MapFragment()

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("BattleFragment", "onViewCreated called")



        //battleViewModel = ViewModelProvider(requireActivity()).get(BattleViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        dbHelper = DBHelper.getInstance(requireContext())

        // SupportMapFragment를 동적으로 추가 (수정된 부분)
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

        // 위치 업데이트 콜백 초기화
        initializeLocationUpdates()
        Log.d("BattleFragment", "initializeLocationUpdates() 호출 완료")


        // 초기 버튼 상태 설정: 시작 버튼만 보이도록
        binding.startBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        binding.finishBtn.visibility = View.GONE


        val userName = arguments?.getString("userName") ?: battleViewModel.userName.value ?: ""
        binding.title.text = "$userName 님과의 배틀"
        battleViewModel.setUserName(userName)

        battleViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.appliedUserName.text = name
        }

        sharedViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        sharedViewModel.hasStarted.observe(viewLifecycleOwner) { hasStarted ->
            if (hasStarted) {
                if (sharedViewModel.isRunning.value == true) {
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

        sharedViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (!isRunning) {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }

        battleViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (::googleMap.isInitialized) {
                googleMap.clear()
                googleMap.addPolyline(
                    PolylineOptions().addAll(pathPoints).color(android.graphics.Color.BLUE).width(5f)
                )
            } else {
                Log.e("BattleFragment", "GoogleMap is not initialized yet")
            }
        }


        binding.startBtn.setOnClickListener {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                startLocationUpdates() // 위치 업데이트 시작 메서드 호출
            } else {
                LocationUtils.requestLocationPermission(this)
            }
            sharedViewModel.startTimer()
            (activity as? MainActivity)?.notifyStartPathDrawing() // MainActivity에 알림 -> HomeFragment 시작 버튼 공유
            sharedViewModel.setHasStarted(true) // 타이머 시작 상태를 true로 설정
            binding.startBtn.visibility = View.GONE
            binding.stopBtn.visibility = View.VISIBLE
            binding.finishBtn.visibility = View.VISIBLE
        }

        // 정지 버튼 클릭 리스너
        binding.stopBtn.setOnClickListener {
            sharedViewModel.stopTimer() // 타이머 정지

            // 버튼 상태 변경: 정지 버튼 숨기고 시작 버튼 보이기
            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }

        // 종료 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            sharedViewModel.stopTimer() // 타이머 정지
            val intent = Intent(requireActivity(), PersonalEndActivity::class.java).apply {
                putExtra("elapsedTime", sharedViewModel.elapsedTime.value ?: 0L)
                putExtra("userName", binding.title.text.toString())
            }
            startActivityForResult(intent, REQUEST_CODE_PERSONAL_END)
        }





        binding.BattlefinishBtn.setOnClickListener {
            sharedViewModel.stopTimer()
            val intent = Intent(requireActivity(), BattleEndActivity::class.java).apply {
                putExtra("elapsedTime", sharedViewModel.elapsedTime.value ?: 0L)
                putExtra("userName", binding.title.text.toString())
            }
            startActivity(intent)
        }
        sharedViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning) {
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERSONAL_END && resultCode == Activity.RESULT_OK) {
            sharedViewModel.resetTimer() // 타이머 초기화
        }
    }

    // GoogleMap이 준비되었을 때 호출되는 메서드 (수정된 부분)
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("BattleFragment", "onMapReady called") // onMapReady 호출 확인 로그

        googleMap.uiSettings.isMyLocationButtonEnabled = true

        // 권한 확인 후 내 위치 표시
        if (LocationUtils.hasLocationPermission(requireContext())) {
            enableMyLocation() // 내 위치 활성화
            initializeGridWithCurrentLocation() // 현재 위치 기준으로 그리드 초기화
        } else {
            LocationUtils.requestLocationPermission(this)
        }
        // 초기 카메라 위치 설정
        //initializeMap() // 수정된 부분: initializeMap 메서드 호출
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
    // Territory Capture 그리드 초기화
    /*private fun initializeMap() {
        Log.d("BattleFragment", "initializeMap called") // 추가: initializeMap 호출 확인

        val startLatLng = LatLng(37.5665, 126.9780) // 시작 위치
        battleViewModel.createGrid(googleMap, startLatLng, 10, 10)

        // 그리드 폴리곤을 관찰하여 지도에 추가 (이미 BattleViewModel에서 지도에 추가되었으므로 생략 가능)
        // 여기서는 필요하지 않을 수 있습니다.
    }*/

    // 위치 업데이트를 위한 LocationRequest와 LocationCallback 설정
    /*private fun initializeLocationUpdates() {
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
    }*/

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

    override fun onResume() {
        super.onResume()
        if (!::googleMap.isInitialized) {
            val supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
            supportMapFragment?.getMapAsync(this)
        }
    }


    // Fragment가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fusedLocationClient.removeLocationUpdates(locationCallback) // 수정: 위치 업데이트 중지함함
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
        private const val REQUEST_CODE_PERSONAL_END = 1001
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000 // 권한 요청 코드 상수
    }
}