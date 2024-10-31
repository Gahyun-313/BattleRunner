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
import com.example.battlerunner.databinding.FragmentMapBinding
import com.example.battlerunner.utils.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null // 뷰 바인딩을 위한 변수
    private val binding get() = _binding!! // 바인딩 객체 접근용
    private lateinit var googleMap: GoogleMap // 구글 맵 객체
    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위치 제공자 클라이언트

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 바인딩 초기화 및 레이아웃 반환
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 위치 제공자 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 지도 프래그먼트 설정 및 콜백
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 현재 위치 버튼 클릭 리스너
        binding.btnCurrentLocation.setOnClickListener {
            if (::googleMap.isInitialized) { // 지도 준비 여부 확인
                moveToCurrentLocation() // 현재 위치로 이동
            } else {
                Toast.makeText(requireContext(), "지도가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map // 구글 맵 초기화

        if (LocationUtils.hasLocationPermission(requireContext())) {
            enableMyLocation() // 권한이 있으면 내 위치 활성화
        } else {
            LocationUtils.requestLocationPermission(this) // 권한 요청
        }

        // 기본 위치(명지대학교) 설정
        val defaultLocation = LatLng(37.222101, 127.187709)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
    }

    private fun enableMyLocation() {
        // 내 위치를 지도에 표시하는 메서드
        try {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                googleMap.isMyLocationEnabled = true // 내 위치 표시 활성화
            }
        } catch (e: SecurityException) {
            Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveToCurrentLocation() {
        // 현재 위치로 카메라 이동
        if (LocationUtils.hasLocationPermission(requireContext())) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        // 위치가 있으면 지도 카메라를 현재 위치로 이동
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    } ?: run {
                        Log.e("MapFragment", "현재 위치를 가져올 수 없습니다.")
                        Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            LocationUtils.requestLocationPermission(this) // 권한 요청
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 메모리 누수 방지
    }
}
