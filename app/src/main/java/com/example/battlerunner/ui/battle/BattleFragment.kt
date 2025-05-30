package com.example.battlerunner.ui.battle

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.ui.home.PersonalEndActivity
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.network.RetrofitInstance
import com.example.battlerunner.service.LocationService
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BattleFragment() : Fragment(R.layout.fragment_battle), OnMapReadyCallback {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap // GoogleMap 객체를 담아 지도를 제어
    private lateinit var locationCallback: LocationCallback // 위치 업데이트에 필요한 콜백 함수
    private lateinit var dbHelper: DBHelper // 데이터베이스 접근을 위한 DBHelper 인스턴스
    private var gridInitialized = false // 지도 그리드 초기화 여부 확인하는 플래그
    private var trackingActive = false // 소유권 추적 활성화 상태
    private var startLatLng: LatLng? = null // 그리드 시작 위치 저장

    private var opponentName: String? = "배틀 상대 이름"
    private var opponentID: String? = "배틀 상대 ID"
    private var userName: String? = "내 이름"
    private var userId: String? = "내 ID"
    private var battleId: Long? = null // 배틀 ID

    private val handler = android.os.Handler(Looper.getMainLooper())
    private val updateInterval = 10000L // 5초마다 소유권 갱신

    // viewModel 초기화
    // ★ GlobalApplication에서 HomeViewModel을 가져오기
    private val homeViewModel: HomeViewModel by lazy {
        (requireActivity().application as GlobalApplication).homeViewModel
    }
    private val battleViewModel: BattleViewModel by lazy {
        (requireActivity().application as GlobalApplication).battleViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)

        // Arguments에서 데이터 가져오기
        arguments?.let {
            opponentName = it.getString("opponentName") // 상대 이름 가져오기
            opponentID = it.getString("opponentId") // 상대 ID 가져오기
            battleId = it.getLong("battleId") // 배틀 ID 가져오기
        }

        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("BattleViewModel", "BattleEndActivity ViewModel instance: $this")

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // DBHelper 싱글턴 인스턴스 초기화
        dbHelper = DBHelper.getInstance(requireContext())
        userId = dbHelper.getUserId().toString() // 사용자 ID

        // MapFragment 초기화 (SupportMapFragment 동적으로 추가)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapFragmentContainer, it)
                    .commit()
            }
        mapFragment.getMapAsync(this) // getMapAsync 호출

        // 위치 업데이트 시작 ***!!!! 이거 없으면 그리드 안 그려짐 GPT가 없애라고 해도 무시하고 남겨둬 !!!!***
        MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, homeViewModel)

        initializeLocationUpdates() // 위치 업데이트 콜백 초기화
        initializeBattleFlags(battleId!!)

        // 초기 버튼 상태 설정: 시작 버튼만 보이도록
        binding.startBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        binding.finishBtn.visibility = View.GONE

        // 배틀 상대 이름 설정
        //TODO └> 배틀 상대 이름 하드코딩 해둠 : 수정 필요
        //binding.title.text = "$opponentName 님과의 배틀"
        //opponentName?.let { battleViewModel.setOpponentName(it) }

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

        // MainActivity의 콜백 설정 (HomeFragment' 시작 버튼)
        (activity as? MainActivity)?.startTracking = {
            // Foreground Service 시작 (백그라운드)
            val serviceIntent = Intent(requireContext(), LocationService::class.java)
            requireContext().startService(serviceIntent)

            battleViewModel.setTrackingActive(true) // 소유권 추적 활성화
            trackingActive = true // 추적 활성화 상태 변경
            startLocationUpdates() // 위치 업데이트 시작 메서드 호출
        }
        // MainActivity의 콜백 설정 (HomeFragment' 정지 버튼)
        (activity as? MainActivity)?.stopTracking = {
            battleViewModel.setTrackingActive(false) // 소유권 추적 활성화
            trackingActive = false // 추적 활성화 상태 변경
        }

        // homeViewModel의 start, isRunning의 여부에 따른 버튼 변경
        // homeFragment에서 시작, 정지 버튼을 눌렀을 때 battleFragment에도 적용
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

        // 오늘의 러닝 시작 버튼 리스너
        binding.startBtn.setOnClickListener {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                startLocationUpdates() // 위치 업데이트 시작 메서드 호출

                // Foreground Service 시작
                (activity as? MainActivity)?.startLocationService()

                homeViewModel.startTimer() // 타이머 시작
                homeViewModel.setHasStarted(true) // 타이머 시작 상태를 true로 설정

                battleViewModel.setTrackingActive(true) // 소유권 추적 활성화
                trackingActive = true // 추적 활성화 상태 변경

                // MainActivity에 알림 -> HomeFragment 시작 버튼 공유
                (activity as? MainActivity)?.notifyPathDrawing(true)

                // 버튼 상태 변경
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
                binding.finishBtn.visibility = View.VISIBLE

            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        // 오늘의 러닝 정지 버튼 클릭 리스너
        binding.stopBtn.setOnClickListener {
            stopLocationUpdates()
            battleViewModel.setTrackingActive(false) // 소유권 추적 비활성화
            trackingActive = false // 추적 비활성화 상태 변경

            homeViewModel.stopTimer() // 타이머 정지

            (activity as? MainActivity)?.notifyPathDrawing(false) // MainActivity에 알림 -> HomeFragment 정지 버튼 공유

            // 버튼 상태 변경
            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }

        // 오늘의 러닝 종료 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            stopLocationUpdates()
            battleViewModel.setTrackingActive(false) // 소유권 추적 비활성화
            trackingActive = false // 추적 비활성화 상태 변경

            homeViewModel.stopTimer() // 타이머 중지

            // Foreground Service 중지
            (activity as? MainActivity)?.stopLocationService()

            Toast.makeText(requireContext(), "러닝을 종료합니다.", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireActivity(), PersonalEndActivity::class.java).apply {
                // 데이터 전달
                putExtra("elapsedTime", homeViewModel.elapsedTime.value ?: 0L) // 러닝 소요 시간 전달
                putExtra("distance", homeViewModel.distance.value ?: 0f) // 러닝 거리 전달
            }
            startActivity(intent)

        }

        // 배틀 종료 버튼 클릭 리스너
        binding.BattlefinishBtn.setOnClickListener {
            if (homeViewModel.hasStarted.value == true) {
                // 러닝 중이라면 경고 안내 후 무시
                Toast.makeText(requireContext(), "러닝 중에는 배틀을 종료할 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // BattleEndActivity 실행
                val intent = Intent(requireActivity(), BattleEndActivity::class.java).apply {

                   putExtra("battleId", battleId) // 배틀 Id 전달

//                    putExtra("oppositeName", opponentName) // 배틀 상대 이름
//                    putExtra("userName", dbHelper.getUserName()) // 유저 이름
                }
                //battleViewModel.clearGrid() // 그리드 초기화
                startActivity(intent)
            }
        }
    }

    // 서버에서 소유권 정보 가져오기 및 지도 반영
    private fun syncOwnershipFromServer() {
        battleId?.let { battleId ->
            battleViewModel.fetchGridOwnership(battleId) { success ->
                if (success) {
                    Log.d("BattleFragment", "소유권 정보 동기화 성공")
                } else {
                    Log.e("BattleFragment", "소유권 정보 동기화 실패")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startOwnershipSync() //소유권 동기화 주기적 호출
        syncOwnershipFromServer() // 서버에서 소유권 정보 동기화
    }

    override fun onPause() {
        super.onPause()
        stopOwnershipSync() // 주기적 작업 종료
    }


    // GoogleMap이 준비되었을 때 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        if (LocationUtils.hasLocationPermission(requireContext())) {
            try {
                googleMap.isMyLocationEnabled = true
                initializeGridStartLocation() // 그리드 시작 메서드 호출
                syncOwnershipFromServer() // 소유권 데이터 동기화
                updateOpponentGridOwnership() // 상대방 소유권 업데이트

                moveToCurrentLocationImmediate()
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                LocationUtils.requestLocationPermission(this)
            }
        } else {
            // 권한이 없을 경우 기본 위치로 이동
            val defaultLocation = LatLng(37.222101, 127.187709) // 명지대 5공학관
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        }
    }

    // 현재 위치로 카메라를 이동하는 메서드
    fun moveToCurrentLocationImmediate() {
        if (LocationUtils.hasLocationPermission(requireContext())) { // 권한을 먼저 확인
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        } else {
                            Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 그리드 시작 메서드 (그리드 시작 위치 설정)
    private fun initializeGridStartLocation() {
        // 현재 위치를 가져오고, 서버에서 시작 위치 확인
        fetchCurrentLocation { currentLocation ->
            battleId?.let { battleId ->
                // 서버에서 그리드 시작 위치 가져오기
                battleViewModel.getGridStartLocationFromServer(battleId) { serverStartLocation ->
                    val startLatLng = serverStartLocation ?: currentLocation // 서버 시작 위치 없으면 현재 위치 사용

                     // 서버 시작 위치 또는 현재 위치를 기준으로 그리드 초기화
                    initializeGrid(startLatLng)

                    // 서버에 시작 위치를 저장 (시작 위치가 없는 경우에만 저장)
                    if (serverStartLocation == null) {
                        battleViewModel.setGridStartLocationToServer(battleId, currentLocation) { success ->
                            if (success) {
                                Log.d("BattleFragment", "Start location successfully set to server.")
                            } else {
                                Log.e("BattleFragment", "Failed to set start location to server.")
                            }
                        }
                    } else {
                        //Log.d("BattleFragment", "Start location already exists on the server.")
                    }
                }
            }
        }
    }

    private fun startOwnershipSync() {
        Log.d("BattleFragment", "startOwnershipSync called")
        handler.post(object : Runnable {
            override fun run() {
                battleId?.let { fetchOwnershipFromServer(it) }
                handler.postDelayed(this, updateInterval) // 5초 간격 호출
            }
        })
    }

    private fun stopOwnershipSync() {
        handler.removeCallbacksAndMessages(null) // 모든 핸들러 작업 제거
    }

    private fun fetchOwnershipFromServer(battleId: Long) {
        RetrofitInstance.battleApi.getGridOwnership(battleId).enqueue(object : Callback<Map<Int, String>> {
            override fun onResponse(call: Call<Map<Int, String>>, response: Response<Map<Int, String>>) {
                if (response.isSuccessful) {
                    val ownershipMap = response.body() ?: emptyMap() // Map<Int, String> 바로 사용
                    ownershipMap.forEach { (gridId, ownerId) ->
                        battleViewModel.updateGridOwnership_Real(gridId, ownerId)
                    }
                    Log.d("BattleFragment", "소유권 정보가 성공적으로 업데이트되었습니다.")
                } else {
                    Log.e("BattleFragment", "소유권 정보를 가져오는데 실패했습니다: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<Int, String>>, t: Throwable) {
                Log.e("BattleFragment", "서버와 통신에 실패했습니다.", t)
            }
        })

    }

    // 상대의 그리드 소유권 업데이트
    private fun updateOpponentGridOwnership() {
        val battleId = arguments?.getLong("battleId") ?: return

        // 서버에서 소유권 정보 가져오기
        RetrofitInstance.battleApi.getGridOwnership(battleId).enqueue(object : Callback<Map<Int, String>> {
            override fun onResponse(call: Call<Map<Int, String>>, response: Response<Map<Int, String>>) {
                if (response.isSuccessful) {
                    val ownershipMap = response.body() ?: emptyMap()

                    // 소유권 정보 업데이트
                    ownershipMap.forEach { (gridId, ownerId) ->
                        battleViewModel.updateGridOwnership_Real(gridId, ownerId)
                    }

                    Log.d("BattleFragment", "상대방의 소유권 정보가 성공적으로 업데이트되었습니다.")
                } else {
                    Log.e("BattleFragment", "서버에서 소유권 정보를 가져오는데 실패했습니다: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<Int, String>>, t: Throwable) {
                Log.e("BattleFragment", "서버와 통신에 실패했습니다.", t)
                Toast.makeText(requireContext(), "상대방의 소유권 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 현재 위치 가져오기 메서드
    private fun fetchCurrentLocation(onLocationReady: (LatLng) -> Unit) {
        MapUtils.currentLocation.observe(viewLifecycleOwner) { location ->
            if (location != null) {
                onLocationReady(LatLng(location.latitude, location.longitude))
            } else {
                Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 그리드 초기화 메서드
    private fun initializeGrid(startLatLng: LatLng) {
        if (!gridInitialized) {
            // 그리드를 고정 ID로 초기화하는 메서드 호출
            battleViewModel.createFixedGrid(googleMap, startLatLng, rows = 29, cols = 29)
            gridInitialized = true
            Log.d("BattleFragment", "Grid initialized at $startLatLng")
        }
    }

    // 위치 업데이트를 설정하는 메서드
    private fun initializeLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    //Log.d("BattleFragment", "User location updated: $userLocation")

                    if (trackingActive) {
                        dbHelper.getUserId()?.let { userId ->
                            battleId?.let {
                                battleViewModel.updateOwnership(userLocation, userId, it)
                            } // 소유권 업데이트
                        }
                    }
                }
            }
        }
        startLocationUpdates() // 위치 업데이트 시작
    }

    // 위치 업데이트를 요청하는 메서드
    private fun startLocationUpdates() {
        if (LocationUtils.hasLocationPermission(requireContext())) {
            try {
                // 위치 요청 설정
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L) // 1초 주기로 위치 업데이트
                    .setMinUpdateIntervalMillis(500) // 최소 업데이트 간격 500ms
                    .build()

                // 위치 업데이트 요청
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                Log.d("BattleFragment", "Location updates started.")
            } catch (e: SecurityException) {
                Log.e("BattleFragment", "위치 권한이 없어 위치 업데이트를 요청할 수 없습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            LocationUtils.requestLocationPermission(this)
        }
    }

    // 위치 업데이트 중지
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("BattleFragment", "Location updates stopped.")
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .replace(R.id.mapFragmentContainer, it)
                    .commitNow()
            }
        mapFragment.getMapAsync(this)
    }

    // 그리드 소유권
    private fun initializeBattleFlags(battleId: Long) {
        RetrofitInstance.battleApi.initializeBattleFlags(battleId).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("BattleFragment", "Battle flags initialized: ${response.body()}")
                    fetchOwnershipFromServer(battleId) // 소유권 데이터 조회
                } else {
                    Log.e("BattleFragment", "Failed to initialize battle flags: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("BattleFragment", "Error initializing battle flags", t)
            }
        })
    }


    // Fragment가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        stopOwnershipSync() // 주기적 작업 종료
        _binding = null
        fusedLocationClient.removeLocationUpdates(locationCallback) // 위치 업데이트 중지
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val REQUEST_CODE_PERSONAL_END = 1001
    }
}