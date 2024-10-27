package com.example.battlerunner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val gridList = mutableListOf<Polygon>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // SupportMapFragment 사용
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // 현재 위치로 이동하는 버튼 클릭 리스너
        binding.btnCurrentLocation.setOnClickListener {
            if (::googleMap.isInitialized) {
                moveToCurrentLocation()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 위치 권한 확인
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

        // 기본 위치 설정 - 명지대학교 5공학관
        val seoul = LatLng(37.222101, 127.187709)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))

        // 격자 생성
        createGrid()
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        try {
            if (hasLocationPermission()) {
                googleMap.isMyLocationEnabled = true
            }
        } catch (e: SecurityException) {
            // 권한 없이 위치 서비스를 사용하려고 시도한 경우 처리
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // 현재 위치로 카메라 이동
    private fun moveToCurrentLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    }
                }
            } catch (e: SecurityException) {
                // 권한 없이 위치 서비스를 사용하려고 시도한 경우 처리
            }
        } else {
            requestLocationPermission()  // 권한 요청
        }
    }

    // 격자 생성 함수
    private fun createGrid() {
        val startLat = 37.0
        val startLng = 127.0
        val endLat = 38.0
        val endLng = 128.0
        val gridSize = 0.0025  // 약 250m 정도에 해당하는 값

        var currentLat = startLat
        while (currentLat < endLat) {
            var currentLng = startLng
            while (currentLng < endLng) {
                val bounds = LatLngBounds(
                    LatLng(currentLat, currentLng),
                    LatLng(currentLat + gridSize, currentLng + gridSize)
                )
                val polygon = googleMap.addPolygon(
                    PolygonOptions()
                        .add(
                            LatLng(bounds.southwest.latitude, bounds.southwest.longitude),
                            LatLng(bounds.southwest.latitude, bounds.northeast.longitude),
                            LatLng(bounds.northeast.latitude, bounds.northeast.longitude),
                            LatLng(bounds.northeast.latitude, bounds.southwest.longitude)
                        )
                        .strokeWidth(2f)
                        .strokeColor(android.graphics.Color.BLACK)
                        .fillColor(android.graphics.Color.TRANSPARENT)
                )
                gridList.add(polygon)
                currentLng += gridSize
            }
            currentLat += gridSize
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
