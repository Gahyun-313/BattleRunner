package com.example.battlerunner.ui.battle // 패키지 선언

import android.annotation.SuppressLint // 특정 린트 경고를 무시하기 위한 어노테이션
import android.content.Intent // 인텐트 사용을 위한 임포트
import android.location.Location // 위치 정보를 가져오기 위한 클래스 임포트
import android.os.Bundle // 액티비티 상태를 저장하고 복원하는 번들 임포트
import android.os.Handler // 주기적인 업데이트를 위한 핸들러
import android.os.Looper // 메인 루퍼 사용을 위한 임포트
import android.view.LayoutInflater // 레이아웃 인플레이터를 위한 임포트
import android.view.View // 뷰 클래스 임포트
import android.view.ViewGroup // 뷰 그룹 임포트
import androidx.fragment.app.Fragment // 프래그먼트 클래스 임포트
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentBattleBinding // 뷰 바인딩을 위한 FragmentBattleBinding 임포트
import com.example.battlerunner.utils.LocationUtils // 위치 권한 확인을 위한 LocationUtils 임포트
import com.google.android.gms.location.FusedLocationProviderClient // 위치 제공자를 위한 클래스 임포트
import com.google.android.gms.location.LocationCallback // 위치 콜백을 위한 클래스 임포트
import com.google.android.gms.location.LocationRequest // 위치 요청 설정을 위한 클래스 임포트
import com.google.android.gms.location.LocationResult // 위치 결과를 위한 클래스 임포트
import com.google.android.gms.location.LocationServices // 위치 서비스 임포트
import com.google.android.gms.maps.CameraUpdateFactory // 지도 카메라 업데이트를 위한 클래스 임포트
import com.google.android.gms.maps.GoogleMap // GoogleMap 객체 임포트
import com.google.android.gms.maps.OnMapReadyCallback // 지도 준비 콜백 인터페이스 임포트
import com.google.android.gms.maps.SupportMapFragment // 지원 맵 프래그먼트 임포트
import com.google.android.gms.maps.model.LatLng // 좌표 객체 임포트
import com.google.android.gms.maps.model.PolylineOptions // 폴리라인 옵션 임포트

class BattleFragment : Fragment(R.layout.fragment_battle), OnMapReadyCallback { // BattleFragment 클래스 선언 및 OnMapReadyCallback 구현

    private var _binding: FragmentBattleBinding? = null // 뷰 바인딩을 위한 변수 선언
    private val binding get() = _binding!! // 바인딩 객체에 안전하게 접근하기 위한 getter
    private var isRunning = false // 타이머 실행 여부를 확인하는 변수 선언
    private var startTime: Long = 0 // 타이머 시작 시간을 저장할 변수 선언
    private var elapsedTime: Long = 0 // 경과 시간을 저장할 변수 선언
    private val handler = Handler(Looper.getMainLooper()) // 타이머 UI 업데이트를 위한 핸들러 선언
    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위치 제공자 초기화 설정
    private lateinit var googleMap: GoogleMap // GoogleMap 객체 초기화 설정
    private val pathPoints = mutableListOf<LatLng>() // 경로를 저장할 리스트 선언

    // 타이머를 주기적으로 업데이트하기 위한 Runnable
    private val timerRunnable = object : Runnable {
        @SuppressLint("DefaultLocale") // 기본 로케일 경고를 무시
        override fun run() {
            if (isRunning) { // 타이머가 실행 중일 때만 실행
                elapsedTime = System.currentTimeMillis() - startTime // 현재 시간에서 시작 시간을 빼서 경과 시간 계산
                val seconds = (elapsedTime / 1000) % 60 // 초 계산
                val minutes = (elapsedTime / (1000 * 60)) % 60 // 분 계산
                val hours = (elapsedTime / (1000 * 60 * 60)) // 시간 계산

                binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds) // 경과 시간을 시:분:초 형식으로 표시
                handler.postDelayed(this, 1000) // 1초마다 업데이트
            }
        }
    }

    // 프래그먼트의 뷰를 생성하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false) // 바인딩 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity()) // FusedLocationProviderClient 초기화
        return binding.root // 바인딩된 뷰 반환
    }

    // 뷰가 생성된 후 호출되는 메서드, 주요 초기화 작업 수행
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // 부모 클래스의 onViewCreated 호출

        binding.startBtn.setOnClickListener { // 시작 버튼 클릭 리스너 설정
            startTimer() // 타이머 시작
            startLocationUpdates() // 위치 추적 시작
        }

        binding.finishBtn.setOnClickListener { // 종료 버튼 클릭 리스너 설정
            stopTimer() // 타이머 멈춤
        }

        binding.BattlefinishBtn.setOnClickListener { // Battle 종료 버튼 클릭 시 리스너 설정
            val intent = Intent(requireActivity(), BattleEndActivity::class.java) // BattleEndActivity로 이동할 인텐트 생성
            //intent.putExtra("elapsedTime", elapsedTime) // 경과 시간을 인텐트에 추가
            startActivity(intent) // BattleEndActivity 시작
        }

        val supportMapFragment = SupportMapFragment() // 지도 프래그먼트 초기화
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, supportMapFragment) // mapFragmentContainer에 지도 프래그먼트 설정
            .commit() // 트랜잭션 커밋

        supportMapFragment.getMapAsync(this) // 맵 준비되면 콜백 호출
    }

    // 타이머 시작 함수
    private fun startTimer() {
        if (!isRunning) { // 타이머가 실행 중이 아닐 때만 시작
            startTime = System.currentTimeMillis() - elapsedTime // 현재 시간에서 경과 시간을 뺀 값을 시작 시간으로 설정
            isRunning = true // 타이머 실행 상태로 변경
            handler.post(timerRunnable) // 타이머 실행
        }
    }

    // 타이머 종료 함수
    private fun stopTimer() {
        if (isRunning) { // 타이머가 실행 중일 때만 실행
            isRunning = false // 타이머 멈춤
            handler.removeCallbacks(timerRunnable) // 타이머 콜백 제거
        }
    }

    // GoogleMap 준비되면 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map // GoogleMap 객체 초기화

        if (LocationUtils.hasLocationPermission(requireContext())) { // 위치 권한 확인
            enableMyLocation() // 권한이 있을 때 내 위치 버튼 활성화
        } else {
            LocationUtils.requestLocationPermission(this) // 권한이 없을 경우 요청
        }

        startLocationUpdates() // 지도 초기화 후 위치 추적 시작
    }

    // 내 위치를 지도에 표시하는 메서드
    private fun enableMyLocation() {
        if (LocationUtils.hasLocationPermission(requireContext())) { // 위치 권한이 있는지 확인
            try {
                googleMap.isMyLocationEnabled = true // 권한이 있다면 내 위치 표시 활성화
            } catch (e: SecurityException) { // 예외 처리
                e.printStackTrace() // 예외 발생 시 스택 트레이스 출력
            }
        } else {
            LocationUtils.requestLocationPermission(this) // 권한이 없으면 요청
        }
    }

    // 위치 업데이트 시작하는 메서드
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000  // 위치 업데이트 간격 (2초)
            fastestInterval = 1000  // 가장 빠른 위치 업데이트 간격 (1초)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 높은 정확도 우선
        }

        if (LocationUtils.hasLocationPermission(requireContext())) { // 권한 확인
            try {
                // 권한이 있는 경우 위치 업데이트 요청
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (e: SecurityException) {
                // SecurityException 발생 시 예외 처리
                e.printStackTrace()
            }
        } else {
            // 권한이 없으면 요청
            LocationUtils.requestLocationPermission(this)
        }
    }


    // 위치 업데이트 콜백 설정
    private val locationCallback = object : LocationCallback() { // 위치 결과 수신 시 호출되는 콜백 설정
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location -> // 위치 리스트 반복
                updateLocationUI(location) // 각 위치에 대해 UI 업데이트
            }
        }
    }

    // 위치 정보를 사용해 UI를 실시간으로 갱신하는 메서드
    private fun updateLocationUI(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude) // 위치 정보를 LatLng 객체로 변환
        pathPoints.add(latLng) // 경로 리스트에 현재 위치 좌표 추가

        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints) // 경로를 지도에 표시
                .color(android.graphics.Color.BLUE) // 경로 색상 지정
                .width(5f) // 경로 너비 지정
        )

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f)) // 현재 위치로 카메라 이동 (줌 레벨 18)
    }

    // 프래그먼트가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView() // 부모 클래스의 onDestroyView 호출
        _binding = null // 메모리 누수를 방지하기 위해 바인딩 객체 해제
        handler.removeCallbacks(timerRunnable) // 타이머 콜백 제거
    }
}

