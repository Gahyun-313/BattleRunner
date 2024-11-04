package com.example.battlerunner.ui.shared

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityMainBinding
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.databinding.FragmentMapBinding
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null // 뷰 바인딩을 위한 변수
    private val binding get() = _binding!! // 바인딩 객체 접근용
    lateinit var googleMap: GoogleMap // 구글 맵 객체
    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위치 제공자 클라이언트
    private var onMapReadyCallback: (() -> Unit)? = null
    private var cameraPosition: CameraPosition? = null // 카메라 위치 저장 변수

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 위치 제공자 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 지도 프래그먼트 설정 및 콜백
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer)
                as? SupportMapFragment ?: SupportMapFragment.newInstance().also {
            childFragmentManager.beginTransaction().replace(R.id.mapFragmentContainer, it).commitNow()
        }
        mapFragment.getMapAsync(this) // Map 준비가 완료되면 onMapReady 호출

        // custom 내 위치 버튼 리스너
        binding.customLocationButton.setOnClickListener{
            moveToCurrentLocationImmediate()
        }
    }

    // 구글 맵이 준비되었을 때 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map // 구글 맵 초기화

        if (!isAdded) return  // Fragment가 Activity에 연결되었는지 확인

        googleMap.uiSettings.isMyLocationButtonEnabled = false // 기본 내 위치 버튼 숨기기

        if (LocationUtils.hasLocationPermission(requireContext())) {
            enableMyLocation() // 내 위치 활성화
            moveToCurrentLocationImmediate()
        } else {
            // 기본 위치 = 명지대 5공학관
            val defaultLocation = LatLng(37.222101, 127.187709)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

            // 위치 권한 요청
            LocationUtils.requestLocationPermission(this)
        }

        // 카메라 이동이 완료될 때마다 현재 위치를 저장하도록 리스너 설정
        googleMap.setOnCameraIdleListener {
            cameraPosition = googleMap.cameraPosition // ★ 현재 카메라 위치 저장
        }

        // Map이 준비되었음을 알림
        onMapReadyCallback?.invoke()
    }

    // 내 위치 활성화 메서드
    private fun enableMyLocation() {
        if (LocationUtils.hasLocationPermission(requireContext())) { // ★ 권한 확인
            try {
                googleMap.isMyLocationEnabled = true // 내 위치 표시 활성화
            } catch (e: SecurityException) { // ★ SecurityException 예외 처리
                Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            LocationUtils.requestLocationPermission(this) // 권한이 없으면 요청
        }
    }

    // 현재 위치로 카메라를 이동하는 메서드
    fun moveToCurrentLocationImmediate() {
        if (LocationUtils.hasLocationPermission(requireContext())) { // ★ 권한을 먼저 확인
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)) // 애니메이션 없이 즉시 이동
                        } ?: Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: SecurityException) { // ★ 권한 예외를 명시적으로 처리
                e.printStackTrace()
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            LocationUtils.requestLocationPermission(this) // 권한이 없으면 요청
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MapUtils.stopLocationUpdates(fusedLocationClient)
        _binding = null
    }

    // moveToCurrentLocation을 안전하게 호출하기 위한 콜백 설정 메서드
    fun setOnMapReadyCallback(callback: () -> Unit) {
        onMapReadyCallback = callback
    }

    // 경로를 그리는 메서드
    fun drawPath(pathPoints: List<LatLng>) {
        val polylineOptions = MapUtils.createPolylineOptions(pathPoints)
        googleMap.addPolyline(polylineOptions)
    }
}
